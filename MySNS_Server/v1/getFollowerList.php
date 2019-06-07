<?php

include_once "../includes/Users.php";

$response = array();

if(isset($_SERVER['REQUEST_METHOD']) == "POST") {

    if(isset($_POST['id']) &&
            isset($_POST['my_id'])) {

        $id = $_POST['id'];
        $id = (int) $id;

        $my_id = $_POST['my_id'];
        $my_id = (int) $my_id;

        $user = new Users();
        $follower_list = $user->getFollowers($id, $my_id);

        $response['error'] = false;
        $response['message'] = "팔로우 목록 패치 성공";
        $response['follower_result'] = $follower_list;

    } else {

        $response['error'] = true;
        $response['message'] = "NO post request";

    }
} else {

    $response['error'] = true;
    $response['message'] = "wrong request";
}

echo json_encode($response);