<?php

include_once "../includes/Chat.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['post'])) {

        $chat = new Chat();
        $result = $chat->send_notification_to_whom();

        if ($result) {

            $response['error'] = false;
            $response['message'] = "push notification success";
            $response['result'] = $result;

        } else {

            $response['error'] = false;
            $response['message'] = "push notification fail";
        }
    } else {

        $response['error'] = false;
        $response['message'] = "no post request";
    }

} else {

    $response['error'] = true;
    $response['message'] = "wrong request";
}

echo json_encode($response);