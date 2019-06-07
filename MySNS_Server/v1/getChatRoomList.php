<?php

include_once "../includes/Chat.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['id'])) {

        $id = $_POST['id'];
        $id = (int) $id;

        $chat = new Chat();

        $result = $chat->getChatRoomList($id);

        if($result) {

            $response['error'] = false;
            $response['message'] = "get chat room list success";
            $response['chat_room_list'] = $result;

        } else {
            $response['error'] = true;
            $response['message'] = "get chat room list fail";
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