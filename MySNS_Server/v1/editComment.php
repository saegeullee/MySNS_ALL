<?php

include_once "../includes/Comment.php";

$response = array();

if(isset($_SERVER['REQUEST_METHOD']) == "POST") {

    if(isset($_POST['post_comment']) &&
        isset($_POST['post_comment_id'])) {

        $post_comment = $_POST['post_comment'];
        $post_comment_id = $_POST['post_comment_id'];

        $post_comment_id = (int)$post_comment_id;

        $comment = new Comment();
        $result = $comment->updateComment($post_comment_id, $post_comment);

        if($result) {
            $response['error'] = false;
            $response['message'] = "comment update success";

        } else {

            $response['error'] = true;
            $response['message'] = "comment update fail";
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