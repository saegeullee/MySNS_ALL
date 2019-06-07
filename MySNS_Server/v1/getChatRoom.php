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

        $result = $chat->getChatRoom($my_id, $friend_id);

        if($result == 1) {

            $response['error'] = false;
            $response['message'] = "no chatroom exists";
            $response['room_id'] = "no_room";

        } else if($result == 0) {

            $response['error'] = false;
            $response['room_id'] = "no_room";
            $response['message'] = "no chatroom exists";

        } else {

            $response['error'] = false;
            $response['message'] = "get chatroom success";
            $response['room_id'] = $result;

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