<?php

include_once "../includes/Post.php";

$response = array();

if($_SERVER["REQUEST_METHOD"] == "POST") {

    if(isset($_POST['post_id'])) {

        $post_id = $_POST['post_id'];
        $post_id = (int) $post_id;

        $post = new Post();
        $result = $post->deletePost($post_id);

        if($result) {

            $response['error'] = false;
            $response['message'] = "포스트 삭제 완료 ";

        } else {
            $response['error'] = true;
            $response['message'] = "포스트 삭제 실패 post delete fail";
        }
    } else {
        $response['error'] = true;
        $response['message'] = "해당 포스트가 없습니다. no post";
    }

} else {
    $response['error'] = true;
    $response['message'] = "wrong request";
}

echo json_encode($response);
