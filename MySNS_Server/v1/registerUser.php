<?php

require_once '../includes/Users.php';

$response = array();

if($_SERVER['REQUEST_METHOD']=='POST') {

    if(isset($_POST['user_id']) &&
            isset($_POST['password']) &&
                isset($_POST['user_name']) &&
                    isset($_POST['user_email'])) {

            $user_id = $_POST['user_id'];
            $password = $_POST['password'];
            $user_name = $_POST['user_name'];
            $user_email = $_POST['user_email'];

            $user = new Users();

            $result = $user -> createUser($user_id, $password, $user_name, $user_email);

            if($result == 1) {
                $response['error'] = false;
                $response['message'] = "회원가입 성공";

            } elseif($result == 2) {
                $response['error'] = true;
                $response['message'] = "Some error occurred please try again";

            } elseif($result == 0) {
                $response['error'] = true;
                $response['message'] = "아이디 또는 이메일이 이미 존재합니다.";
            }

        } else {
            $response['error'] = true;
            $response['message'] = "Required fields are missing";

        }

} else {
    $response['error'] = true;
    $response['message'] = 'Invalid Request';
}

echo json_encode($response);
