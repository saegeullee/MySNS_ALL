package com.example.saegeullee.applicationoneproject;

public class Constants {

    /**
     * LOCAL TEST
     */

    private static final String ROOT_URL = "http://192.168.3.21/myproject_chat/v1/";
    public static final String ROOT_URL_USER_PROFILE_IMAGE = "http://192.168.3.21/myproject_chat/uploads/users/";
    public static final String ROOT_URL_POST_IMAGE = "http://192.168.3.21/myproject_chat/uploads/posts/";

    /**
     * AWS SERVER
     */
//    private static final String ROOT_URL = "http://ec2-13-209-7-57.ap-northeast-2.compute.amazonaws.com/myproject/v2/";
//    public static final String ROOT_URL_USER_PROFILE_IMAGE = "http://ec2-13-209-7-57.ap-northeast-2.compute.amazonaws.com/myproject/uploads/users/";
//    public static final String ROOT_URL_POST_IMAGE = "http://ec2-13-209-7-57.ap-northeast-2.compute.amazonaws.com/myproject/uploads/posts/";

    public static final String URL_REGISTER = ROOT_URL + "registerUser.php";
    public static final String URL_LOGIN = ROOT_URL + "userLogin.php";
    public static final String URL_UPDATE_USER = ROOT_URL + "updateUser.php";

    public static final String URL_ADD_POST = ROOT_URL + "addPost.php";
    public static final String URL_EDIT_POST = ROOT_URL + "editPost.php";
    public static final String URL_DELETE_POST = ROOT_URL + "deletePost.php";

    public static final String URL_FOLLOW_USER = ROOT_URL + "follow.php";
    public static final String URL_UNFOLLOW_USER = ROOT_URL + "unfollow.php";

    public static final String URL_GET_FOLLOWER_LIST = ROOT_URL + "getFollowerList.php";
    public static final String URL_GET_ALL_USER_LIST = ROOT_URL + "getAllUserList.php";
    public static final String URL_ADD_FRIEND = ROOT_URL + "addFriend.php";
    public static final String URL_GET_FRIEND_LIST = ROOT_URL + "getFriendList.php";
    public static final String URL_GET_FRIEND_LIST_FOR_CHATROOM = ROOT_URL + "getFriendListForChatRoom.php";


    public static final String URL_LIKE_POST = ROOT_URL + "likePost.php";
    public static final String URL_UNLIKE_POST = ROOT_URL + "unLikePost.php";

    public static final String URL_DISPLAY_INDIVIDUAL_POST = ROOT_URL + "displayIndividualPost.php";
    public static final String URL_FEED_USER_DETAIL = ROOT_URL + "feedUserDetail.php";

    public static final String URL_INSERT_COMMENT_TO_POST = ROOT_URL + "insertPostComment.php";
    public static final String URL_GET_ALL_COMMENTS = ROOT_URL + "getAllComments.php";
    public static final String URL_EDIT_COMMENT = ROOT_URL + "editComment.php";
    public static final String URL_DELETE_COMMENT = ROOT_URL + "deleteComment.php";

    public static final String URL_DISPLAY_ALL_POST = ROOT_URL + "displayAllPost.php";

    public static final String URL_CREATE_CHAT_ROOM = ROOT_URL + "createChatRoom.php";
    public static final String URL_CREATE_CHAT_ROOM_FOR_GROUP_CHAT = ROOT_URL + "createChatRoomForGroupChat.php";
    public static final String URL_GET_CHAT_ROOM = ROOT_URL + "getChatRoom.php";
    public static final String URL_GET_CHAT_ROOM_2 = ROOT_URL + "getChatRoom2.php";


    public static final String URL_GET_CHAT_ROOM_LIST = ROOT_URL + "getChatRoomList.php";
    public static final String URL_GET_CHAT_ROOM_MESSAGES = ROOT_URL + "getChatRoomMessages.php";
    public static final String URL_GET_CHAT_ROOM_MEBMER = ROOT_URL + "getChatRoomMember.php";
    public static final String URL_INSERT_NEW_CHAT_ROOM_MEMBERS = ROOT_URL + "insertNewChatRoomMembers.php";

    public static final String URL_INSERT_USER_TOKEN = ROOT_URL + "insertUserToken.php";
    public static final String URL_PUSH_NOTIFICATIONS = ROOT_URL + "push_notification.php";

    public static final String URL_GET_NAVER_NEWS = "https://openapi.naver.com/v1/search/news.json?query=빅뱅승리";



}
