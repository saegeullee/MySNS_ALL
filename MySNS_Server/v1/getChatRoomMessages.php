<?php

include_once "../includes/Chat.php";

$response = array();
if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['chat_room_id'])) {

        $chat_room_id = $_POST['chat_room_id'];
        $chat_room_id = (int) $chat_room_id;

        $chat = new Chat();

        $result = $chat->getChatRoomMessages($chat_room_id);

        if($result) {

            $response['error'] = false;
            $response['message'] = "get chat room messages success";
            $response['chat_room_messages'] = $result;

        } else {
            $response['error'] = true;
            $response['message'] = "get chat room messages fail";
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