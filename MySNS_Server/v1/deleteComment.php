<?php

include_once "../includes/Comment.php";

$response = array();

if(isset($_SERVER['REQUEST_METHOD']) == "POST") {

    if(isset($_POST['post_comment_id'])) {

        $comment_id = $_POST['post_comment_id'];
        $comment_id = (int)$comment_id;

        $comment = new Comment();
        $result = $comment->deleteComment($comment_id);
        if($result) {
            $response['error'] = false;
            $response['message'] = "delete comment success";
        } else {
            $response['error'] = true;
            $response['message'] = "delete comment fail";
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