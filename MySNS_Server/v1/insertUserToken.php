<?php

include_once "../includes/Users.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['id']) && isset($_POST['token'])) {

        $id = $_POST['id'];
        $token = $_POST['token'];

        $id = (int) $id;

        $user = new Users();
        $result = $user->insertUserToken($id, $token);

        if($result) {

            $response['error'] = false;
            $response['message'] = "insert user token success";

        } else {

            $response['error'] = false;
            $response['message'] = "insert user token fail";
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