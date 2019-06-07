<?php

include_once "../includes/Users.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == 'POST') {

    if(isset($_POST['id'])) {

        $id = $_POST['id'];
        $id = (int) $id;

        $user = new Users();
        $result = $user->getAllUserList($id);

        if ($result) {

            $response['error'] = false;
            $response['message'] = "get all users success";
            $response['all_users_list'] = $result;

        } else {

            $response['error'] = true;
            $response['message'] = "get all user fail";
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
