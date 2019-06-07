<?php

include_once "../includes/Chat.php";

$response = array();
if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['chat_room_id']) && isset($_POST['id'])) {

        $chat_room_id = $_POST['chat_room_id'];
        $id = $_POST['id'];

        $chat_room_id = (int) $chat_room_id;
        $id = (int) $id;

        $chat = new Chat();

        $result = $chat->getChatRoomMembers($chat_room_id, $id);

        if($result) {

            $response['error'] = false;
            $response['message'] = "get chat room members success";
            $response['chat_room_members'] = $result;

        } else {
            $response['error'] = true;
            $response['message'] = "get chat room members fail";
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