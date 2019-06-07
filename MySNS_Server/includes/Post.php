<?php

include_once "Comment.php";

class Post {

    private $con;

    function __construct() {

        require_once dirname(__FILE__) . '/DBConnect.php';

        $db = new DbConnect();
        $this->con = $db->connect();

    }

    public function createPost($user_id, $image, $image_name, $description) {

        $stmt = $this->con->prepare("INSERT INTO posts (user_id, description)
                                          VALUES ( ?, ? ); ");
        $stmt->bind_param("is", $user_id, $description);

        if($stmt->execute()) {

            /**
             * 위의 INSERT 쿼리문을 통해 생성된 post 의 auto increment 된 id를 가져온다.
             * 아래의 쿼리문에서 이 아이디로 post_image 테이블에 이미지 경로를 저장한다.
             */
            $post_id = mysqli_insert_id($this->con);

            $post_image_upload_folder_path = "../uploads/posts/$post_id/";
            $post_image_upload_path = $post_image_upload_folder_path . $image_name;
            $image_name= "$image_name";

            /**
             * 12/22 5:30pm 에러 발생
             * post_image 테이블에 order 칼럼이 있는데 일단 이미지 한개만 처리하기 때문에
             * 디폴트로 $stmt_image->bind_param("sii", $image_name, $post_id, 1);
             * 이런식으로 인자를 넣어주니 변수를 bind_param 메서드에 넣으라고 징징댐
             * <b>Notice</b>:  Only variables should be passed by reference in
                <b>/var/www/html/myproject/includes/Post.php</b> on line
                <b>38</b>
             */

            $stmt_image = $this->con->prepare("INSERT INTO post_image (image, post_id, image_order)
                                                  VALUES (?,?,?)");

            $a = 1;
            $stmt_image->bind_param("sii", $image_name, $post_id, $a);

            if($stmt_image->execute()){

                if (!file_exists($post_image_upload_folder_path)) {
                    mkdir($post_image_upload_folder_path, 0777, true);
                }

                file_put_contents($post_image_upload_path , base64_decode($image));
                return true;

            } else {
                return false;
            }

        } else {
                return false;
            }
    }


    public function fetchIndividualPostById($id, $current_user_id) {

        $sql = "SELECT posts.id AS post_id, posts.user_id, posts.description, 
                posts.created_at AS date, post_image.image, post_image.image_order 
                FROM posts
                INNER JOIN post_image
                ON posts.id = post_image.post_id
                WHERE user_id = ? 
                ORDER BY posts.created_at DESC;";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $id);

        $indivPostArray = array();

        if($stmt->execute()) {

            $result = $stmt->get_result();

            while($row = $result->fetch_assoc()) {

                $post_id = $row['post_id'];
                $user_id = $row['user_id'];
                $description = $row['description'];
                $date = $row['date'];
                $image = $row['image'];
                $image_order = $row['image_order'];

                $post_likes = self::getLikesOfPost($post_id);
                $is_like_post = self::isLikePost($current_user_id, $post_id);

                $post_item = array(
                    'id' => $id,
                    'user_id' => $user_id,
                    'image' => $image,
                    'post_id' => $post_id,
                    'description' => $description,
                    'date' => $date,
                    'image_order' => $image_order,
                    'post_likes' => $post_likes,
                    'is_like_post' => $is_like_post
                );

                array_push($indivPostArray, $post_item);
            }

            return $indivPostArray;

        } else {

            return false;
        }
    }

        public function fetchAllPost($current_user_id) {

        $sql = "SELECT users.id, users.user_id, users.profile_image, 
                posts.id as post_id, posts.description, posts.created_at,
                post_image.image as post_image
                FROM posts
                  INNER JOIN post_image
                    ON posts.id = post_image.post_id
                  INNER JOIN users
                    ON users.id = posts.user_id 
                ORDER BY created_at DESC";

        $stmt = $this->con->prepare($sql);

        if($stmt->execute()) {

            $result = $stmt->get_result();

            $post_arr = array();

            while($row = $result->fetch_assoc()) {

                $id = $row['id'];
                $user_id = $row['user_id'];
                $profile_image = $row['profile_image'];
                $post_id = $row['post_id'];
                $description = $row['description'];
                $created_at = $row['created_at'];
                $post_image = $row['post_image'];

                $post_likes = self::getLikesOfPost($post_id);
                $is_like_post = self::isLikePost($current_user_id, $post_id);

                $comment = new Comment();
                $post_comment_num = $comment->getCommentNumOfPosts($post_id);

                $post_item = array(
                    'id' => $id,
                    'user_id' => $user_id,
                    'profile_image' => $profile_image,
                    'post_id' => $post_id,
                    'description' => $description,
                    'created_at' => $created_at,
                    'post_image' => $post_image,
                    'post_likes' => $post_likes,
                    'is_like_post' => $is_like_post,
                    'post_comment_num' => $post_comment_num
                );

                array_push($post_arr, $post_item);

            }

            return $post_arr;
        } else {

            return false;
        }

    }

    public function editPost($post_id, $description) {

        $sql = "UPDATE posts SET description = ? WHERE id = ?";
        $stmt = $this->con->prepare($sql);
        $stmt->bind_param('si', $description, $post_id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }

    public function deletePost($post_id) {

        /**
         * posts table 에서 포스트를 삭제하면 post_image 테이블의
         * 해당 post_image 도 삭제된다.
         * (테이블 생성시 ON DELETE CASCADE 걸어줬기 때문에 가능)
         * 또한 포스트 이미지 경로에 있는 이미지도 삭제해야 한다.
         */


        $image_upload_folder_path = "../uploads/posts/";
        $image_upload_path = $image_upload_folder_path . $post_id;

        $this->deleteDir($image_upload_path);

        $sql = "DELETE FROM posts WHERE id = ?";
        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $post_id);
        if($stmt->execute()) {
            return true;
        }
        return false;

    }

    public function deleteDir($dirPath) {
        if (! is_dir($dirPath)) {
            throw new InvalidArgumentException("$dirPath must be a directory");
        }
        if (substr($dirPath, strlen($dirPath) - 1, 1) != '/') {
            $dirPath .= '/';
        }
        $files = glob($dirPath . '*', GLOB_MARK);
        foreach ($files as $file) {
            if (is_dir($file)) {
                self::deleteDir($file);
            } else {
                unlink($file);
            }
        }
        rmdir($dirPath);
    }

    public function likePost($id, $post_id) {

        $sql = "INSERT INTO likes(user_id, post_id)
                VALUES (?, ?)";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $id, $post_id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }

    public function unLikePost($id, $post_id) {

        $sql = "DELETE FROM likes
                WHERE user_id = ? AND post_id = ?
                ";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $id, $post_id);
        if($stmt->execute()) {
            return true;
        }
        return false;
    }


    /**
     * @param $post_id
     * @return bool|int
     * likePost.php 에서도 이 메서드 사용하기 때문에 publics
     */

    public function getLikesOfPost($post_id) {

        $sql = "SELECT *
                FROM likes
                WHERE post_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("i", $post_id);

        if($stmt->execute()) {

            $result = $stmt->get_result();
            $likes_num = $result->num_rows;

            return $likes_num;
        }

        return false;

    }

    public function isLikePost($id, $post_id) {

        $sql = "SELECT * FROM likes
                WHERE user_id = ? AND post_id = ?";

        $stmt = $this->con->prepare($sql);
        $stmt->bind_param("ii", $id, $post_id);
        if($stmt->execute()) {
            $result = $stmt->get_result();
            if($result->num_rows > 0) {
                return true;
            }
            return false;
        }
        return false;
    }


}

/**
    $stmt->bind_param('i',$id);

    /* execute query
    $stmt->execute();

    /* Get the result
    $result = $stmt->get_result();

    /* Get the number of rows
    $num_of_rows = $result->num_rows;

    while ($row = $result->fetch_assoc()) {
        echo 'ID: '.$row['id'].'<br>';
        echo 'First Name: '.$row['first_name'].'<br>';
        echo 'Last Name: '.$row['last_name'].'<br>';
        echo 'Username: '.$row['username'].'<br><br>';
    }

 */