<?php
/**
 * Created by PhpStorm.
 * User: saegeullee
 * Date: 2018-12-22
 * Time: 오후 5:50
 */

/**
 * 18/12/26 여기서 게시물에 대한 정보(댓글, 좋아요 등등..)와 함께
 * 1. 현재 사용자가 발행한 게시물의 개수
 * => 개시물의 개수는 클라이언트에서 게시물 object.length 로 획득
 * 2. 팔로워 숫자
 * 3. 팔로잉 숫자 도 같이 가져와야 한다.
 * 최대한 한번의 리퀘스트에 필요한 모든 정보를 가져올 수 있도록 구성하는 것이 좋지 않을까
 */

require_once "../includes/Post.php";
require_once "../includes/Users.php";

if($_SERVER["REQUEST_METHOD"] == "POST") {

    if(isset($_POST['id'])) {

        $user_id = $_POST['id'];
        $user_id = (int) $user_id;

        /**
         * 게시물 객체
         */

        $post = new Post();
        $result_post_data = $post->fetchIndividualPostById($user_id, $user_id);

        /**
         * 사용자의 팔로잉 정보
         */
        $user = new Users();
        $result_follow_data = $user->getFollowInfo($user_id);

        $result = array();
        $result['post_data'] = $result_post_data;
        $result['follow_data'] = $result_follow_data;

        echo json_encode($result);

    } else {


    }
}