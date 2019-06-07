<?php

include_once "../includes/Users.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['id']) &&
            isset($_POST['friend_id'])) {

        $my_id = $_POST['id'];
        $friend_id = $_POST['friend_id'];

        $my_id = (int) $my_id;
        $friend_id = (int) $friend_id;

        $user = new Users();
        $result = $user->addFriend($my_id, $friend_id);

        if($result) {

            $response['error'] = false;
            $response['message'] = "add friend success";

        } else {

            $response['error'] = true;
            $response['message'] = "add friend fail";
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