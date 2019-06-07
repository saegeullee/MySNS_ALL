<?php

include_once "../includes/Users.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['id'])) {

        $id = $_POST['id'];
        $id = (int) $id;

        $user = new Users();
        $result = $user->getFriendList($id);

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