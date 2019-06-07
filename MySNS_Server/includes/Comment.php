<?php

class Comment {

    private $con;

    function __construct() {

        require_once dirname(__FILE__) . '/DBConnect.php';

        $db = new DbConnect();
        $this->con = $db->connect();

    }

    public function insertComment($id, $post_id, $comment) {

        $sql = "INSERT INTO comments (user_id, post_id, text)
                VALUES (?, ?, ?)";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("iis", $id, $post_id, $comment);
        if($stmt->execute()) {

            $comment_id = mysqli_insert_id($this->con);

            return $comment_id;
        }
        return false;
    }

    /**
     * @param $post_id
     * 댓글 내용, 댓글 시간
     * 댓글 단 사용자 아이디, 이미지
     */

    public function fetchAllComments($post_id) {

        $sql = "SELECT users.id, users.user_id, users.profile_image,
                comments.id AS comment_id, comments.text, comments.created_at
                FROM comments
                  INNER JOIN users
                    ON users.id = comments.user_id
                WHERE comments.post_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $post_id);
        if($stmt->execute()) {

            $comment_arr = array();
            $result = $stmt->get_result();

            if($result -> num_rows > 0) {

                while ($row = $result->fetch_assoc()) {

                    $id = $row['id'];
                    $user_id = $row['user_id'];
                    $comment_id = $row['comment_id'];
                    $profile_image = $row['profile_image'];
                    $text = $row['text'];
                    $created_at = $row['created_at'];

                    $comment_item = array();

                    $comment_item['id'] = $id;
                    $comment_item['user_id'] = $user_id;
                    $comment_item['profile_image'] = $profile_image;
                    $comment_item['text'] = $text;
                    $comment_item['created_at'] = $created_at;
                    $comment_item['comment_id'] = $comment_id;

                    array_push($comment_arr, $comment_item);
                }

                return $comment_arr;
            } else {

                return 0;
            }
        }
        return 1;
    }

    public function getCommentNumOfPosts($post_id) {

        $sql = "SELECT * FROM comments
                WHERE post_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $post_id);
        if($stmt->execute()) {

            $result = $stmt-> get_result();
            $post_num = $result->num_rows;
            return $post_num;
        }
        return false;

    }


    public function updateComment($comment_id, $comment_text) {

        $sql = "UPDATE comments SET
                text = ? WHERE id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("si", $comment_text, $comment_id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }

    public function deleteComment($comment_id) {

        $sql = "DELETE FROM comments WHERE id = ?";
        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $comment_id);
        if($stmt->execute()) {
            return true;
        } else {
            return false;
        }
    }





}