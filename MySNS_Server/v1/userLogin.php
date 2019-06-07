<?php

require_once '../includes/Users.php';

$response = array();

if($_SERVER['REQUEST_METHOD'] == 'POST') {

    if(isset($_POST['user_id'])
        && isset($_POST['password'])) {

        $user_id = $_POST['user_id'];
        $password = $_POST['password'];

        $user = new Users();
        if($user->userLogin($user_id, $password)) {

            $user = $user->getUserByUsername($user_id);

            $response['error'] = false;
            $response['message'] = "로그인 성공";

            $response['id'] = $user['id'];
            $response['user_id'] = $user['user_id'];
            $response['user_name'] = $user['user_name'];
            $response['user_email'] = $user['user_email'];
            $response['profile_image'] = $user['profile_image'];

        } else {
            $response['error'] = true;
            $response['message'] = "사용자가 없습니다. 회원가입을 진행하세요";
        }

    } else {

        $response['error'] = true;
        $response['message'] = "아이디와 비밀번호를 입력해주세요";
    }
}

echo json_encode($response);