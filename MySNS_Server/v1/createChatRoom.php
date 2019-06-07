<?php

include_once "../includes/Chat.php";


$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['my_id']) && isset($_POST['friend_id'])) {

        $my_id = $_POST['my_id'];
        $friend_id = $_POST['friend_id'];
        $my_id = (int) $my_id;
        $friend_id = (int) $friend_id;

        $chat = new Chat();

        $result = $chat->createChatRoom($my_id, $friend_id);
        if($result) {

            $response['error'] = false;
            $response['message'] = "create chatroom success";
            $response['room_id'] = $result;
        } else {

            $response['error'] = true;
            $response['message'] = "create chatroom fail";
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