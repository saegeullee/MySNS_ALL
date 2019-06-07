<?php

include_once "../includes/Post.php";

$response = array();

/**
 * 현재 사용자가 게시물의 좋아요를 취소하면 이를 DB 에 적용하고
 * 해당 게시물의 좋아요 개수를 클라이언트로 보내준다.
 */

if(isset($_SERVER['REQUEST_METHOD']) == "POST") {

    if(isset($_POST['id']) &&
        isset($_POST['post_id'])) {

        $id = $_POST['id'];
        $post_id = $_POST['post_id'];

        $id = (int) $id;
        $post_id = (int) $post_id;

        $post = new Post();
        $result = $post->unLikePost($id, $post_id);

        /**
         * 좋아요의 개수를 얻어온다.
         */

        $likes_num = $post->getLikesOfPost($post_id);

        if($result) {
            $response['error'] = false;
            $response['message'] = "post unlike success";
            $response['likes_num'] = $likes_num;

        } else {
            $response['error'] = true;
            $response['message'] = "post unlike fail";
        }

    } else {

        $response['error'] = true;
        $response['message'] = "post request not set";
    }
} else {

    $response['error'] = true;
    $response['message'] = "wrong request";

}

echo json_encode($response);