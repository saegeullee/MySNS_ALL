<?php

class Users{

    private $con;

    function __construct(){

        require_once dirname(__FILE__). '/DBConnect.php';

        $db = new DbConnect();

        $this->con = $db->connect();
    }

    /* CRUD -> CREATE */

    public function createUser($user_id, $password, $user_name, $user_email) {

        if($this->isUserExist($user_id, $user_email)) {

            return 0;

        } else {

            //encrypt password
            $password = md5($password);

            $stmt = $this->con->prepare("INSERT INTO users (user_id, password, 
                  user_name, user_email) VALUES ( ?, ?, ?, ?);");

            $stmt->bind_param("ssss", $user_id, $password, $user_name, $user_email);

            if ($stmt->execute()) {
                return 1;

            } else {
                return 2;
            }
        }
    }

    public function updateUserWithProfileImage($id, $user_id, $user_name, $user_email, $profile_image, $profile_image_name) {

        /**
         * 새로운 이미지를 경로에 저장하기 전에 사용자가 전에 사용하던 이미지는 삭제 해아한다.
         * unlink('path/to/file.jpg');
         */

        $query = $this->con->prepare("SELECT profile_image FROM users WHERE id = ?");
        $query->bind_param('i', $id);
        $query->execute();
        $user_profile_image_assoc = $query->get_result()->fetch_assoc();
        $original_user_profile_image = $user_profile_image_assoc['profile_image'];

        $image_upload_folder_path = "../uploads/users/";

        /**
         * 12/21 5:48pm
         * 사용자가 최초 회원가입을 하고 프로필 수정에서 이미지 설정을 하면
         * 기존에 저장한 이미지가 없기 때문에 삭제할 이미지도 없기 때문에 if 문으로
         * 기존에 저장한 이미지가 있을 때만 삭제를 한다.
         */

        if($original_user_profile_image) {
            unlink($image_upload_folder_path . $original_user_profile_image);
        }

        $image_upload_path = $image_upload_folder_path . $profile_image_name;
        $image_name = "$profile_image_name";

        /** 12/21 12:24am
         * 이 쿼리에서 에러났었음 user_name = ? user_email = ? 이 둘 사이의 ,를 빼먹음
         */

        $stmt = $this->con->prepare("UPDATE users SET user_id = ?, 
                  user_name = ?, user_email = ?, profile_image = ? WHERE id = ?");

        if($stmt) {

            $stmt->bind_param('ssssi', $user_id, $user_name, $user_email, $image_name, $id);

            if($stmt->execute()) {
                file_put_contents($image_upload_path, base64_decode($profile_image));
                return true;

            } else {
                return false;
            }

        } else {
            $error = $this->con->errno . ' ' . $this->con->error;
            echo $error;
            return false;
        }
    }

    public function updateUser($id, $user_id, $user_name, $user_email) {

        $stmt = $this->con->prepare("UPDATE users SET user_id = ?, 
                  user_name = ?, user_email = ? WHERE id = ?");

        if($stmt) {

            $stmt->bind_param('sssi', $user_id, $user_name, $user_email, $id);

            if($stmt->execute()) {

                return true;

            } else {
                return false;
            }

        } else {
            $error = $this->con->errno . ' ' . $this->con->error;
            echo $error;
            return false;
        }
    }

    public function userLogin($user_id, $pass) {

        $password = md5($pass);
        $stmt = $this->con->prepare("SELECT id FROM users WHERE user_id = ? AND password = ?");
        $stmt->bind_param("ss", $user_id, $password);
        $stmt->execute();
        $stmt->store_result();
        return $stmt->num_rows > 0;

    }

    public function getUserByUsername($user_id) {
        $stmt = $this->con->prepare("SELECT * FROM users Where user_id = ?");
        $stmt->bind_param("s", $user_id);
        $stmt->execute();
        return $stmt->get_result()->fetch_assoc();
    }

    public function getUserById($id) {
        $stmt = $this->con->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        return $stmt->get_result()->fetch_assoc();
    }


    private function isUserExist($user_id, $user_email) {
        $stmt = $this->con->prepare("SELECT user_id FROM users WHERE user_id = ? OR user_email = ?");
        $stmt->bind_param("ss", $user_id, $user_email);
        $stmt->execute();
        $stmt->store_result();
        return $stmt->num_rows > 0;
    }

    public function follow($follower_id, $followee_id) {

        $sql = "INSERT INTO follows (follower_id, followee_id)
                VALUES (?, ?);";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $follower_id, $followee_id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }

    public function unfollow($follower_id, $followee_id) {

        $sql = "DELETE FROM follows
                WHERE follower_id = ? AND followee_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $follower_id, $followee_id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }

    public function getFollowInfo($id) {

        $sql_following =
                "SELECT *
                 FROM users
                 INNER JOIN follows
                 ON users.id = follows.follower_id
                 WHERE users.id = ?";

        $stmt_following = $this->con->prepare($sql_following);
        $stmt_following->bind_param("i", $id);
        $stmt_following->execute();
        $stmt_following->store_result();
        /**
         * 팔로잉 수
         */
        $following_num = $stmt_following->num_rows;

        $sql_followee =
                "SELECT *
                 FROM users
                 INNER JOIN follows
                 ON users.id = follows.followee_id
                 WHERE users.id = ?";

        $stmt_followee = $this->con->prepare($sql_followee);
        $stmt_followee->bind_param("i", $id);
        $stmt_followee->execute();
        $stmt_followee->store_result();
        /**
         * 팔로워 수
         */
        $followee_num = $stmt_followee->num_rows;

        $result = array();
        $result['following_num'] = $following_num;
        $result['followee_num'] = $followee_num;

        return $result;

    }


    public function isFollowing($my_id, $id) {

        $sql = "SELECT * 
                FROM follows
                WHERE follower_id = ? AND followee_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $my_id, $id);
        $stmt->execute();
        $stmt->store_result();
        if($stmt->num_rows > 0) {
            return true;
        }
        return false;
    }

    public function getFollowers($id, $my_id) {

        $sql = "SELECT users.id, users.user_id, users.profile_image,
                follows.follower_id, follows.followee_id
                FROM follows
                  INNER JOIN users
                    ON follows.follower_id = users.id
                WHERE followee_id = ? ";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $id);

        if($stmt->execute()) {

            $result = $stmt->get_result();
            $follower_array = array();

            while($row = $result->fetch_assoc()) {
                $id = $row['id'];
                $user_id = $row['user_id'];
                $profile_image = $row['profile_image'];

                $item = array();
                $item['id'] = $id;
                $item['user_id'] = $user_id;
                $item['profile_image'] = $profile_image;
                $item['is_following'] = self::isFollowing($my_id, $id);

                array_push($follower_array, $item);

            }

            return $follower_array;
        }
        return false;

    }

    /**
     * @return array|bool
     * 이미 친구인 사람은 모든 사용자 목록에 보여주지 않아도 된다.
     */

    public function getAllUserList($my_id) {

        $sql = "SELECT * FROM users 
                WHERE id != ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $my_id);

        $users_array = array();

        if($stmt->execute()) {

            $result= $stmt->get_result();

            while($row = $result->fetch_assoc()) {

                $id = $row['id'];
                $user_id = $row['user_id'];
                $user_name = $row['user_name'];
                $user_email = $row['user_email'];
                $profile_image = $row['profile_image'];
                $is_friend = self::isFriend($my_id, $id);

                $item = array(
                    'id' => $id,
                    'user_id' => $user_id,
                    'user_name' => $user_name,
                    'profile_image' => $profile_image,
                    'user_email' => $user_email,
                    'is_friend' => $is_friend
                );

                array_push($users_array, $item);

            }
            return $users_array;
        }

        return false;
    }

    public function addFriend($my_id, $friend_id) {

        $sql = "INSERT INTO friend(friend1_id, friend2_id)
                VALUES ( ?, ? )";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $my_id, $friend_id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }

    public function getFriendList($my_id) {

        $sql = "SELECT * FROM users
                INNER JOIN friend
                ON users.id = friend.friend2_id
                WHERE friend.friend1_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $my_id);
        $users_array = array();

        if($stmt->execute()) {

            $result = $stmt->get_result();
            while($row = $result->fetch_assoc()) {

                $id = $row['id'];
                $user_id = $row['user_id'];
                $user_name = $row['user_name'];
                $user_email = $row['user_email'];
                $profile_image = $row['profile_image'];

                $item = array(
                    'id' => $id,
                    'user_id' => $user_id,
                    'user_name' => $user_name,
                    'profile_image' => $profile_image,
                    'user_email' => $user_email
                );

                array_push($users_array, $item);
            }
            return $users_array;
        }

        return false;
    }

    public function getFriendListForChatRoom($my_id, $chatRoomId) {

        $sql = "SELECT * FROM users
                INNER JOIN friend
                ON users.id = friend.friend2_id
                WHERE friend.friend1_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $my_id);
        $users_array = array();

        if($stmt->execute()) {

            $result = $stmt->get_result();
            while($row = $result->fetch_assoc()) {

                $id = $row['id'];
                $user_id = $row['user_id'];
                $user_name = $row['user_name'];
                $user_email = $row['user_email'];
                $profile_image = $row['profile_image'];

                $isInChatRoom = self::isInChatRoom($id, $chatRoomId);

                $item = array(
                    'id' => $id,
                    'user_id' => $user_id,
                    'user_name' => $user_name,
                    'profile_image' => $profile_image,
                    'user_email' => $user_email,
                    'isInChatRoom' => $isInChatRoom
                );

                array_push($users_array, $item);
            }
            return $users_array;
        }

        return false;
    }

    public function isInChatRoom($id, $chatRoomId) {

        $sql = "SELECT * FROM participants 
                WHERE room_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $chatRoomId);
        if($stmt->execute()) {

            $result = $stmt->get_result();

            while($row = $result->fetch_assoc()) {
                $user_id = $row['user_id'];
                if($user_id == $id) {
                    return true;
                }
            }
        }
        return false;
    }

    public function isFriend($id, $friend_id) {

        $sql = "SELECT * FROM friend
                WHERE friend1_id = ? AND friend2_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $id, $friend_id);
        if($stmt->execute()) {
            $result = $stmt->get_result();
            if($result->num_rows > 0) {
                return true;
            }
            return false;
        }
        return false;
    }

    public function insertUserToken($id, $token) {

        $sql = "UPDATE users SET token = ?
                WHERE id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("si", $token, $id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }
}