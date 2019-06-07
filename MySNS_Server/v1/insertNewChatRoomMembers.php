<?php

include_once "../includes/Chat.php";

$response = array();
if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['chat_room_id']) &&
        isset($_POST['id']) &&
            isset($_POST['chat_room_add_user_list'])) {

        $chat_room_id = $_POST['chat_room_id'];
        $id = $_POST['id'];
        $chat_room_add_user_list = (string) $_POST['chat_room_add_user_list'];

        $chat_room_id = (int) $chat_room_id;
        $id = (int) $id;

        $chat = new Chat();

        $result = $chat->insertNewChatRoomMembers($chat_room_id, $id, $chat_room_add_user_list);

        if($result) {

            $response['error'] = false;
            $response['message'] = "insert new chat room members success";

        } else {
            $response['error'] = true;
            $response['message'] = "insert new chat room members fail";
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