<?php

require_once "../includes/Post.php";

/**
 * Created by PhpStorm.
 * User: saegeullee
 * Date: 2018-12-24
 * Time: 오후 11:08
 */

$response = array();

if($_SERVER["REQUEST_METHOD"] == "POST") {

    if(isset($_POST['description']) && isset($_POST['post_id'])) {

        $post_id = $_POST['post_id'];
        $description = $_POST['description'];

        $post_id = (int) $post_id;

        $post = new Post();

        $result = $post->editPost($post_id, $description);

        if($result) {

            $response['error'] = false;
            $response['message'] = "포스트 업데이트 성공";
        } else {

            $response['error'] = true;
            $response['message'] = "포스트 업데이트 실패";
        }
    } else {
        $response['error'] = true;
        $response['message'] = "문구를 채워주세요 또는 post id가 없습니다.";
    }


} else {
    $response['error'] = true;
    $response['message'] = "잘못된 요청입니다.";
}

/**
 * 12/24 11:28pm 에러발생
 * json_encode 응답을 클라로 안보내줬잖아
 * 인간은 같은 실수를 반복한다.
 */

echo json_encode($response);
