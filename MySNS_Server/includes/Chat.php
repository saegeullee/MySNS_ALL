<?php
/**
 * Created by PhpStorm.
 * User: saegeullee
 * Date: 2019-01-18
 * Time: 오후 6:45
 */

class Chat
{

    private $con;
    private $user;

    public function __construct()
    {

        require_once dirname(__FILE__) . '/DBConnect.php';
        require_once dirname(__FILE__) . '/Users.php';

        $db = new DbConnect();

        $this->con = $db->connect();
        $this->user = new Users();

    }

    public function createChatRoom($my_id, $friend_id)
    {

        $sql = "INSERT INTO chatroom (name)
                VALUES (?)";

        $stmt = $this->con->prepare($sql);

        $user_list = $my_id . "|" . $friend_id;

        $stmt->bind_param("s", $user_list);

        if ($stmt->execute()) {

            $chatRoomId = mysqli_insert_id($this->con);

            if (self::putParticipants($my_id, $chatRoomId) &&
                self::putParticipants($friend_id, $chatRoomId)) {

                return $chatRoomId;
            }
            return false;
        }
    }

    public function putParticipants($user_id, $room_id)
    {

        $sql = "INSERT INTO participants(user_id, room_id) 
                VALUES(?, ?)";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $user_id, $room_id);

        if ($stmt->execute()) {
            return true;
        }
        return false;
    }

    public function getChatRoom($my_id, $friend_id)
    {

        /**
         * my_id 로 참여하고 있는 모든 participants 테이블의 항목을 가져오고 그 항목의 room_id 와
         * friend_id 의 항목과 같은 room_id 가 있다면 이 둘은 같은 채팅방에 있다.
         */

        $sql = "SELECT * FROM participants 
                WHERE user_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $my_id);

        if ($stmt->execute()) {

            $result = $stmt->get_result();
            while ($row = $result->fetch_assoc()) {

                $room_id = $row['room_id'];
                $isOnlyTwoPeopleInChatRoom = self::checkIfOnlyTwoPeopleInChatRoom($room_id);
                if ($isOnlyTwoPeopleInChatRoom) {
                    if (self::checkIfInSameRoom($friend_id, $room_id)) {

                        return self::checkIfInSameRoom($friend_id, $room_id);
                    }
                } else {
                    return 1;
                }
            }
        }
        return 1;
    }

    public function checkIfOnlyTwoPeopleInChatRoom($room_id)
    {

        $sql = "SELECT * FROM participants
                WHERE room_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $room_id);
        if ($stmt->execute()) {

            $result = $stmt->get_result();
            $chatroomMemberNumber = $result->num_rows;
            if ($chatroomMemberNumber > 2) {
                return false;
            } else {
                return true;
            }
        }
        return 1;
    }

    public function checkIfInSameRoom($friend_id, $room_id)
    {

        $sql = "SELECT * FROM participants 
                WHERE user_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $friend_id);
        if ($stmt->execute()) {

            $result = $stmt->get_result();
            while ($row = $result->fetch_assoc()) {

                $friend_room_id = $row['room_id'];

                if ($friend_room_id === $room_id) {

                    return $friend_room_id;
                }
            }
        }
        return false;
    }

    /**
     * 채팅방에 속해있는 상대방의 정보를 가져와야 한다.
     */

    public function getChatRoomList($id)
    {

        $sql = "SELECT * FROM participants
                WHERE user_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $id);
        if ($stmt->execute()) {

            $result = $stmt->get_result();
            $chatRoomList = array();
            while ($row = $result->fetch_assoc()) {

                $room_id = $row["room_id"];
                $item = array();

                $item['room_id'] = $room_id;
                $item['room_user_info'] = self::getChatRoomUserInfo($id, $room_id);

                array_push($chatRoomList, $item);
            }

            return $chatRoomList;
        }
        return false;
    }

    public function getChatRoomUserInfo($id, $room_id)
    {

        $sql = "SELECT participants.user_id AS id, participants.room_id, users.user_id, users.profile_image
                FROM participants
                INNER JOIN users
                  ON participants.user_id = users.id
                WHERE participants.room_id = ? AND participants.user_id != ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $room_id, $id);

        if ($stmt->execute()) {
            $result = $stmt->get_result();
            $chatroom_array = array();

            while ($row = $result->fetch_assoc()) {

                $room_id = $row['room_id'];
                $id = $row['id'];
                $user_id = $row['user_id'];
                $profile_image = $row['profile_image'];

                $item = array();
                $item['id'] = $id;
                $item['room_id'] = $room_id;
                $item['user_id'] = $user_id;
                $item['profile_image'] = $profile_image;
                $item['last_message'] = self::getLastMessageFromChatRoom($room_id);

                array_push($chatroom_array, $item);
            }

            return $chatroom_array;
        }
        return false;
    }

    public function getLastMessageFromChatRoom($room_id)
    {

        $sql = "SELECT * FROM message 
                WHERE room_id = ? AND message_type = 'msg'
                ORDER BY created_at DESC";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $room_id);
        if ($stmt->execute()) {
            $result = $stmt->get_result();
            $row = $result->fetch_assoc();

            $message = $row['message'];
            $created_at = $row['created_at'];

            $message_row = array();

            $message_row['message'] = $message;
            $message_row['created_at'] = $created_at;

            return $message_row;
        }
        return false;
    }


    public function getChatRoomMessages($chat_room_id)
    {


        $sql = "SELECT message.user_id AS id, message, message_type, created_at, 
                users.user_id AS user_id, users.profile_image
                FROM message
                INNER JOIN users
                  ON message.user_id = users.id
                WHERE room_id = ?
                ORDER BY message.created_at ASC";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $chat_room_id);

        if ($stmt->execute()) {
            $result = $stmt->get_result();
            $itemArr = array();

            while ($row = $result->fetch_assoc()) {

                $id = $row['id'];
                $user_id = $row['user_id'];
                $message = $row['message'];
                $message_type = $row['message_type'];
                $created_at = $row['created_at'];
                $profile_image = $row['profile_image'];

                $item = array();

                $item['id'] = $id;
                $item['user_id'] = $user_id;
                $item['message'] = $message;
                $item['message_type'] = $message_type;
                $item['created_at'] = $created_at;
                $item['profile_image'] = $profile_image;

                if ($message_type == "invited") {
                    $token = strtok($message, "*");

                    $string_builder = "";

                    while ($token !== false) {

                        $user = $this->user->getUserById($token);
                        $temp_user_id = $user['user_id'];
                        $string_builder .= $temp_user_id;
                        $string_builder .= "*";

                        $token = strtok("*");
                    }

                    $item['invited_user_list'] = $string_builder;
                }

                array_push($itemArr, $item);
            }
            return $itemArr;
        }
        return false;
    }

    public function getChatRoomMembers($chat_room_id, $current_user_id)
    {

        $sql = "SELECT participants.user_id AS id, users.user_id, users.profile_image
                FROM participants
                INNER JOIN users
                  ON participants.user_id = users.id
                WHERE room_id = ? AND participants.user_id != ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $chat_room_id, $current_user_id);

        if ($stmt->execute()) {
            $result = $stmt->get_result();
            $members_arr = array();
            while ($row = $result->fetch_assoc()) {

                $id = $row['id'];
                $user_id = $row['user_id'];
                $profile_image = $row['profile_image'];

                $isFriend = $this->user->isFriend($current_user_id, $id);

                $item = array();

                $item['id'] = $id;
                $item['user_id'] = $user_id;
                $item['profile_image'] = $profile_image;
                $item['isFriend'] = $isFriend;

                array_push($members_arr, $item);
            }

            return $members_arr;
        }
        return false;
    }

    public function insertNewChatRoomMembers($chat_room_id, $id, $chat_room_add_user_list)
    {

        $sql = "INSERT INTO message (room_id, user_id, 'message', message_type)
q               VALUES ( ?, ?, ?, ? ) ";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param('iiss', $chat_room_id, $id, 'invited', 'invited');
        if ($stmt->execute()) {

            $message_id = mysqli_insert_id($this->con);
            $result = self::insertIntoInvitedUsersTable($message_id, $chat_room_add_user_list);
            if ($result) {
                return true;
            }
        }
        return false;
    }

    public function insertIntoInvitedUsersTable($message_id, $chat_room_add_user_list)
    {

        //어떤 사용자가 추가되었는가
        $sql = "INSERT INTO invited_users(message_id, user_id)
                VALUES (?, ?)";

        $token = strtok($chat_room_add_user_list, "|");
        while ($token !== false) {

            $add_user_id = $token;
            $stmt = $this->con->prepare($sql);
            $stmt->bind_param("ii", $message_id, $add_user_id);
            $stmt->execute();
            $token = strtok("|");
        }

        return true;
    }

    public function createChatRoomForGroupChat($json)
    {

        $sql = "INSERT INTO chatroom (name)
                VALUES (?)";

        $stmt = $this->con->prepare($sql);
        $name = "chatroom_name";
        $stmt->bind_param("s", $name);

        if ($stmt->execute()) {

            $chatRoomId = mysqli_insert_id($this->con);

            $result = false;
            foreach ($json['add_user_list'] as $user) {

                $id = $user['id'];
                $user_id = $user['userId'];
                $result = self::putParticipants($id, $chatRoomId);
            }

            if ($result) {
                return $chatRoomId;
            }
        }

        return false;
    }

    public function send_notification($tokens, $message)
    {

//        $url = 'https://fcm.googleapis.com/v1/projects/project-403272633423/messages:send';
        $url = 'https://fcm.googleapis.com/fcm/send';

        $fields = array(
            'registration_ids' => $tokens,
//            'to' => $tokens[0],
            'data' => $message,
            'priority' => 'high',
            'notification' => array(
                'title' => 'This is title',
                'body' => 'This is body'
            )
        );

        $headers = array(
            'Authorization:key = AIzaSyCV7Ez3KvYt2zx4fs8KQ8xQfB22vs1ONkE',
            'Content-Type: application/json'
        );

        /**
         * php build in curl library -> to send network request
         */

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

        /**
         * result returned from FIREBASE
         */
        $result = curl_exec($ch);

        if ($result === FALSE) {
            die("Curl failed : " . curl_error($ch));
        }
        curl_close($ch);
        return $result;
    }

    public function send_notification_to_whom() {

        $sql = "SELECT token FROM users";

        $stmt = $this->con->prepare($sql);
        if($stmt->execute()) {

            $tokens = array();

            $result = $stmt->get_result();
            while($row = $result->fetch_array()) {

                $tokens[] = $row['token'];

            }

            $message = array("message" => " FCM PUSH NOTI TEST MESSAGE");
            $message_status = self::send_notification($tokens, $message);

            return $message_status;
        }
        return false;
    }

}