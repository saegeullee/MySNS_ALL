<?php

include_once "../includes/Users.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['follower_id']) &&
        isset($_POST['followee_id'])) {

        /**
         * follower_id => 현재 사용자의 아이디
         * followee_id => 현재 사용자가 언팔로우 하고 싶은 사람의 아이디
         * followee_id의 num_row 숫자를 쿼리해서 클라이언트로 보내줘야 한다.
         */

        $follower_id = $_POST['follower_id'];
        $followee_id = $_POST['followee_id'];

        $user = new Users();
        $result = $user->unfollow($follower_id, $followee_id);

        /**
         * 팔로워 언팔로워 숫자를 가져옴
         */
        $following_info = $user->getFollowInfo($followee_id);

        if($result) {

            $response['error'] = false;
            $response['message'] = "언팔로우 성공";
            $response['following_info'] = $following_info;
        } else {

            $response['error'] = true;
            $response['message'] = "언팔로우 실패";

        }

        } else {

        $response['error'] = true;
        $response['message'] = "No follower id or followee id SET";
    }

} else {
    $response['error'] = true;
    $response['message'] = "wrong request";
}

echo json_encode($response);