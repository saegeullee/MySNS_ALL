<?php

require_once "../includes/Post.php";

$response = array();

/**
 * 클라이언트 -> feedFragment
* 현재 피드의 모든 게시물의 사용자 프로필 사진, 이름, 게시물 이미지, 설명과 함께
* 좋아요 개수, 댓글, 대댓글도 받아와야 한다.
 * 현재 fetchAllPost() 메서드는 너무 무겁기 때문에
* 좋아요 개수와 댓글을 가져오는 메서드를 따로 작성하자. -> 따로가 아니고 fetchAllPost 메서드
* 내부에서 하나의 게시물을 읽어오는 while 문 안에서 해당 게시물에 대한 좋아요를 읽어오는
* 메서드를 작성해야 한다.
 * -> $post_likes = self::getLikesOfPost($post_id);
 */

/**
 * 현재 사용자가 해당 게시물의 좋아요를 눌렀는지 안눌렀는지에 대한 정보도 가져와야 하기 때문에
 * 클라이언트로 부터 현재 사용자의 아이디를 받아온다.
 */

if(isset($_SERVER['REQUEST_METHOD']) == 'POST') {

    /**
     * 현재 사용자가 모든 게시물의 좋아요를 눌렀는지 안눌렀는지에 대한 정보를 가져오기 위해
     * 클라이언트로 부터 사용자의 아이디를 받아와서 fetchAllPost() 메서드의 인자로 넣어줘서
     * 메서드에서 해당 데이터를 추가로 뽑아온다.
     */

    if(isset($_POST['id'])) {

        $id = $_POST['id'];
        $id = (int)$id;

        $post = new Post();
        $result = $post->fetchAllPost($id);

        if ($result) {

            $post_arr = $result;
            $response['error'] = false;
            $response['message'] = 'Load all post success';
            $response['data'] = $post_arr;

        } else {
            $response['error'] = true;
            $response['message'] = "Load all post fail";
        }

    } else {

        $response['error'] = true;
        $response['message'] = "no post request";
    }

} else {

    $response['error'] = true;
    $response['message'] = "잘못된 요청입니다.";
}

echo json_encode($response);