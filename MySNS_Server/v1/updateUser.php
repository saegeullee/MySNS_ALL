<?php

require_once '../includes/Users.php';

$response = array();

if($_SERVER['REQUEST_METHOD'] == "POST") {

    if(isset($_POST['id']) &&
        isset($_POST['user_id']) &&
        isset($_POST['user_name']) &&
            isset($_POST['user_email'])) {

        $id = $_POST['id'];
        $user_id = $_POST['user_id'];
        $user_name = $_POST['user_name'];
        $user_email = $_POST['user_email'];

        /**
         * 이미지 string 값과 이미지 이름을 클라이언트에서 받았다.
         * 사용자가 이미지를 반드시 저장할 필요는 없기 때문에
         * 위에서 isset 으로 잡지 않았다.
         * 대신 아래의 로직에서 사용자가 프로필 사진을 입력하고 저장했을때와
         * 그렇지 않았을 때를 나눠서 처리했다.
         */

        // 클라이언트에서 string 값으로 id 가 들어왔기 때문에 다시 int 로 바꿔준다.
        $id = (int) $id;

        $users = new Users();

        if(isset($_POST['profile_image']) && isset($_POST['profile_image_name'])) {

            $profile_image = $_POST['profile_image'];
            $profile_image_name = $_POST['profile_image_name'];

            $updateUser = $users->updateUserWithProfileImage($id, $user_id, $user_name, $user_email, $profile_image, $profile_image_name);
        } else {

            $updateUser = $users->updateUser($id, $user_id, $user_name, $user_email);

        }

        $user = $users -> getUserById($id);

        if($updateUser) {
            $response['error'] = false;
            $response['message'] = '프로필 수정이 완료되었습니다.';

            $response['id'] = $user['id'];
            $response['user_id'] = $user['user_id'];
            $response['user_name'] = $user['user_name'];
            $response['user_email'] = $user['user_email'];

            if($user['profile_image']) {
                $response['profile_image'] = $user['profile_image'];
            }

        } else {
            $response['error'] = true;
            $response['message'] = 'Something went wrong';
        }

    } else {
        $response['error'] = true;
        $response['message'] = 'fill all the form';
    }

} else {
    $response['error'] = true;
    $response['message'] = 'Invalid Request';
}

echo json_encode($response);