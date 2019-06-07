<?php

include_once "../includes/Comment.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['post_id'])) {

        $post_id = $_POST['post_id'];
        $post_id = (int) $post_id;

        $comment = new Comment();

        $comment_arr = $comment->fetchAllComments($post_id);

        if($comment_arr == 0) {
            $response['error'] = false;
            $response['message'] = "fetch all comment success : no comment";

        } else if($comment_arr == 1) {
            $response['error'] = true;
            $response['message'] = "fetch all comment fail";

        } else {
            $response['error'] = false;
            $response['message'] = "fetch all comment success";
            $response['comment_data'] = $comment_arr;

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