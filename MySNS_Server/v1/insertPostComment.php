<?php

include_once "../includes/Comment.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['id']) &&
        isset($_POST['post_id']) &&
        isset($_POST['post_comment'])) {

        $current_user_id = $_POST['id'];
        $post_id = $_POST['post_id'];
        $post_comment = $_POST['post_comment'];

        $comment = new Comment();

        $result = $comment->insertComment($current_user_id, $post_id, $post_comment);

        if($result) {

            $response['error'] = false;
            $response['message'] = "insert comment success";
            $response['comment_id'] = $result;

        } else {

            $response['error'] = true;
            $response['message'] = "insert comment fail";
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