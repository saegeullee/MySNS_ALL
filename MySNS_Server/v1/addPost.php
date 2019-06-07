<?php

require_once "../includes/Post.php";

$response = array();

if($_SERVER['REQUEST_METHOD'] == 'POST') {

    if(isset($_POST['id']) &&
        isset($_POST['description']) &&
            isset($_POST['image']) &&
                isset($_POST['image_name'])) {

                $id = $_POST['id'];
                $description = $_POST['description'];
                $image = $_POST['image'];
                $image_name = $_POST['image_name'];

                $post = new Post();

                $result = $post->createPost($id, $image, $image_name, $description);

                if($result) {

                    $response['error'] = false;
                    $response['message'] = "포스트 업로드 성공";

                } else {
                    $response['error'] = true;
                    $response['message'] = "포스트 업로드 실패";
                }
        } else {

            $response['error'] = true;
            $response['message'] = "사진, 설명을 모두 채우세요";
        }

    } else {

        $response['error'] = true;
        $response['message'] = "잘못된 요청입니다.";

    }

/**
 * 12/22 5:42pm 에러발생
 * json_encode 응답을 클라로 안보내줬잖아.. Syntax 에러 발생하네
 * org.json.JSONException: End of input at character 0 of
 * 12-22  at org.json.JSONTokener.syntaxError(JSONTokener.java:449)
 */

echo json_encode($response);