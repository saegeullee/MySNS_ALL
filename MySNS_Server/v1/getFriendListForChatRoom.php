<?php

include_once "../includes/Users.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['id']) && isset($_POST['chat_room_id'])) {

        $id = $_POST['id'];
        $chatRoomId = $_POST['chat_room_id'];
        $id = (int) $id;
        $chatRoomId = (int) $chatRoomId;

        $user = new Users();
        $result = $user->getFriendListForChatRoom($id , $chatRoomId);

        if($result) {

            $response['error'] = false;
            $response['message'] = "get friend list success";
            $response['friend_list'] = $result;

        } else {

            $response['error'] = false;
            $response['message'] = "get friend list success";
        }

    } else {

        $response['error'] = true;
        $response['message'] = "no post request";
    }

} else {

    $response['error'] = true;
    $response['message'] = "wrong request";
}

echo json_encode($response);