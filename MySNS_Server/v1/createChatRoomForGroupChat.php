<?php

include_once "../includes/Chat.php";


$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['create_group_chatroom'])) {

        $jsonData = $_POST['create_group_chatroom'];

        $json = json_decode($jsonData, true);

        $chat = new Chat();

        $result = $chat->createChatRoomForGroupChat($json);

        if($result) {

            $response['error'] = false;
            $response['message'] = "create group chat success";
            $response['room_id'] = $result;

        } else {

            $response['error'] = true;
            $response['message'] = "create group chat fail";
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


/**
 *
 *
 {
"add_user_list":
    [
        {
        "id":1,
        "userId":"user123"
        },
        {
        "id":2,
        "userId":"saegeul"
        },
        {
        "id":3,
        "userId":"user"
        },
        {
        "id":4,
        "userId":"user1"
    ]
}
 */