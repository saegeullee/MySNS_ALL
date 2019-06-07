package com.example.saegeullee.applicationoneproject;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.FeedListAdapter;
import com.example.saegeullee.applicationoneproject.Models.Post;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class feedUserDetailActivity extends AppCompatActivity {

    private static final String TAG = "feedUserDetailActivity";

    private CircleImageView profile_image;
    private TextView username;
    private TextView post_number, followee_number, following_number;
    private TextView post_text, following_text, followee_text;
    private Button edit_profile_btn, follow_btn, unfollow_btn;
    private Toolbar toolbar;

    private List<Post> postList;
    private String following_num, followee_num;
    private int total_post_num;

    private RecyclerView recyclerView;
    private FeedListAdapter feedListAdapter;

    //vars
    private User user;
    private boolean isFollowing = false;
    private boolean isMyProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_user_detail);

        getUserObject();
        initUI();
        setDataToView();

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: in");
        super.onStart();

    }

    private void getUserObject() {

        /**
         * User 객체를 FeedFragment 에서 받는다.
         * initUI: user_id User{id='13', user_id='user111', user_name='null',
         * user_email='null', profile_image='cropped1968078699340067587.jpg'}
         */

        if(getIntent().hasExtra(getString(R.string.db_users_object))) {
            user = getIntent().getParcelableExtra(getString(R.string.db_users_object));
            Log.d(TAG, "initUI: user : " + user.toString());
        }

    }

    private void setDataToView() {

        Log.d(TAG, "setDataToView: start");

        /**
         * 사용자가 발행한 포스트 목록 display
         */

        postList = getPostList();
        feedListAdapter = new FeedListAdapter(feedUserDetailActivity.this, postList);
        recyclerView.setAdapter(feedListAdapter);
        feedListAdapter.notifyDataSetChanged();


        username.setText(user.getUser_name());

        String profile_image_name = user.getProfile_image();
        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + profile_image_name;
        Uri uri = Uri.parse(image_url);

        Log.d(TAG, "setDataToView: profile_image_name : " + profile_image_name);
        Log.d(TAG, "setDataToView: uri : " + uri.toString());

        ImageLoader.getInstance().displayImage(image_url, profile_image);

    }

    private void initUI() {

        Log.d(TAG, "initUI: in");

        username = findViewById(R.id.username);
        profile_image = findViewById(R.id.profile_image);
        post_number = findViewById(R.id.post_number);
        followee_number = findViewById(R.id.followee_number);
        following_number = findViewById(R.id.following_number);

        post_text = findViewById(R.id.postText);
        following_text = findViewById(R.id.followerText);
        followee_text = findViewById(R.id.followeeText);

        edit_profile_btn = findViewById(R.id.edit_profile_button);
        follow_btn = findViewById(R.id.follow_button);
        unfollow_btn = findViewById(R.id.unfollow_button);

        /**
         * 내 계정의 상세 페이지를 보면 프로필 수정 버튼이 보이고
         * 다른 사람 계정의 상세 페이지를 보면 팔로우 버튼이 보이게 설정
         */

        if(user.getId() == SharedPrefManager.getInstance(feedUserDetailActivity.this).getId()) {
            isMyProfile = true;
            edit_profile_btn.setVisibility(View.VISIBLE);
        } else {
            isMyProfile = false;
        }

        profile_image = findViewById(R.id.profile_image);
        toolbar = findViewById(R.id.feedUserDetailToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(user.getUser_id());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setHasFixedSize(true);

        /**
         * 이 속성을 false 로 줬을 때 뻑뻑했던 스크롤 부드러워짐
         */
        recyclerView.setNestedScrollingEnabled(false);


        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(feedUserDetailActivity.this, EditProfileActivity.class));

            }
        });

        follow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followUser();
            }
        });

        unfollow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unfollowUser();
            }
        });

        following_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(feedUserDetailActivity.this, DisplayFollowerActivity.class);
                intent.putExtra(getString(R.string.intent_user_id), user.getId());
                startActivity(intent);

            }
        });

    }


    private void unfollowUser() {

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_UNFOLLOW_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(feedUserDetailActivity.this,
//                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();


                            if(!isMyProfile) {
                                follow_btn.setVisibility(View.VISIBLE);
                                unfollow_btn.setVisibility(View.INVISIBLE);
                            }


                            JSONObject object = jsonObject.getJSONObject("following_info");
                            Log.d(TAG, "onResponse: following : " +object.getString("following_num"));
                            Log.d(TAG, "onResponse: followee : " + object.getString("followee_num"));

                            followee_number.setText(object.getString("followee_num"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(feedUserDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                int follower_id = SharedPrefManager.getInstance(feedUserDetailActivity.this).getId();
                int followee_id = user.getId();

                params.put(getString(R.string.db_follower_id), String.valueOf(follower_id));
                params.put(getString(R.string.db_followee_id), String.valueOf(followee_id));

                return params;
            }
        };

        RequestHandler.getInstance(feedUserDetailActivity.this).addToRequestQueue(stringRequest);

    }

    private void followUser() {

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_FOLLOW_USER,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(feedUserDetailActivity.this,
//                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            /**
                             * 팔로우 버튼 누르고 나서 서버의 응답을 토대로
                             * 버튼 셋팅하기
                             */

                            if(!isMyProfile) {
                                follow_btn.setVisibility(View.INVISIBLE);
                                unfollow_btn.setVisibility(View.VISIBLE);
                            }

                            /**
                             * 현재 사용자의 팔로잉 숫자를 가져와서 1을 더한뒤 setText 한다.
                             * -> 에러발생
                             * 서버에서 팔로잉 숫자 1 는 걸 직접 받아와서 뿌려주자.
                             * followee 숫자를 setText 하면 됨
                             *
                             */
//                            following_number.setText(Integer.parseInt(following_number.getText().toString()) + 1);

                            JSONObject object = jsonObject.getJSONObject("following_info");
                            Log.d(TAG, "onResponse: following : " +object.getString("following_num"));
                            Log.d(TAG, "onResponse: followee : " + object.getString("followee_num"));

                            followee_number.setText(object.getString("followee_num"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(feedUserDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                int follower_id = SharedPrefManager.getInstance(feedUserDetailActivity.this).getId();
                int followee_id = user.getId();

                params.put(getString(R.string.db_follower_id), String.valueOf(follower_id));
                params.put(getString(R.string.db_followee_id), String.valueOf(followee_id));

                return params;
            }
        };

        RequestHandler.getInstance(feedUserDetailActivity.this).addToRequestQueue(stringRequest);

    }

    /**
     * 여기서 상단의 게시물 개수와 팔로워, 팔로잉 숫자도 fetch 해와야 한다.
     * 그리고 현재 사용자를 팔로잉 하고 있는지에 대한 여부도 가져와야 한다.
     * 팔로잉 중인지의 여부에 따라 버튼의 상태를 다르게 셋팅해야 한다.
     * 좋아요 숫자와 댓글도 가져와야되넼ㅋ
     */

    private List<Post> getPostList() {

        postList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_FEED_USER_DETAIL,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        /**
                         * 12/27 12:27 am 에러 발생
                         * Dichone 의 강의 처럼 JsonObjectRequest 로 리퀘스트를 줘서 JsonObject 로 받으려 했는데
                         * 계속 에러 발생. 지금처럼 StringRequest 로 바꿔서 리퀘스트를 쏘고 JsonObject 로 받으니 잘 동작한다.
                         */

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse: obj" + jsonObject.toString());

                            JSONObject object = jsonObject.getJSONObject("follow_data");
                            Log.d(TAG, "onResponse: follow data : " + object.toString());

                            String is_following = jsonObject.getString("is_following");

                            /**
                             * 현재 사용자가 해당 유저를 팔로잉 하고 있는지
                             * 팔로우 하고 있다면 팔로잉 버튼을 숨기고 언팔로우 버튼을 보여준다.
                             */

                            if(!isMyProfile) {
                                if (is_following.equals("true")) {
                                    isFollowing = true;
                                    follow_btn.setVisibility(View.INVISIBLE);
                                    unfollow_btn.setVisibility(View.VISIBLE);
                                } else {
                                    isFollowing = false;
                                    follow_btn.setVisibility(View.VISIBLE);
                                    unfollow_btn.setVisibility(View.INVISIBLE);
                                }
                            }

                            Log.d(TAG, "onResponse: isfollowing : " + isFollowing);


                            /**
                             * 팔로잉, 팔로워 정보 뷰에 셋팅
                             */
                            following_num = object.getString("following_num");
                            followee_num = object.getString("followee_num");

                            Log.d(TAG, "onResponse: following_number : " + following_number);
                            Log.d(TAG, "onResponse: followee_number : " + followee_num);

                            JSONArray postArray = jsonObject.getJSONArray("post_data");
                            Log.d(TAG, "onResponse: jsonArray : " + postArray.toString());

                            /**
                             * 모든 게시물 개수
                             */

                            total_post_num = postArray.length();

                            /**
                             * 뷰에 셋팅할 데이터를 모두 패치해 오고 난 뒤에 View 에 setText 를 한다.
                             */

                            followee_number.setText(followee_num);
                            following_number.setText(following_num);
                            post_number.setText(String.valueOf(total_post_num));

                            for(int i = 0; i < postArray.length(); i ++) {

                                JSONObject obj = postArray.getJSONObject(i);
                                Log.d(TAG, "onResponse:  item : " + "\n" +
                                        "post_id : " + obj.getString("post_id") + "\n" +
                                        "user_id : " + obj.getString("user_id") + "\n" +
                                        "description : " + obj.getString("description") + "\n" +
                                        "image : " + obj.getString("image") + "\n" +
                                        "date : " + obj.getString("date") + "\n" +
                                        "image_order : " + obj.getString("image_order") + "\n" + "\n" +
                                        "post_likes : " + obj.getString("post_likes") + '\n' +
                                        "is_like_post : " + obj.getString("is_like_post") + '\n'
                                );

                                Post post = new Post();

                                /**
                                 * 12/26 8:00pm 에러발생
                                 * 여기서 setUser 를 쉐어드로 가져와서 계속 정보가 잘못 뜸
                                 * SharedPrefManager.getInstance(feedUserDetailActivity.this).getUserObject()
                                 */

                                post.setUser(user);
                                post.setDescription(obj.getString(getString(R.string.db_post_description)));
                                post.setPostImage(obj.getString(getString(R.string.db_post_image)));
                                post.setDate(obj.getString(getString(R.string.db_post_date)));
                                post.setPostId(obj.getString(getString(R.string.db_post_id)));
                                post.setLikes(obj.getString("post_likes"));

                                if(obj.getString("is_like_post").equals("true")) {
                                    post.setLikePost(true);
                                } else {
                                    post.setLikePost(false);
                                }

                                postList.add(post);

                            }

                            Log.d(TAG, "onResponse: post list size : " + postList.size());
                            feedListAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(feedUserDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                /**
                 * 현재 내가(Current User) feedUserDetail 의 사용자를 팔로우 하고 있는지에 대한 정보를
                 * 가져오기 위해 나의 id 도 서버로 보내야 한다.
                 */

                int user_id = user.getId();
                int my_id = SharedPrefManager.getInstance(feedUserDetailActivity.this).getId();

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_users_id), String.valueOf(user_id));
                params.put(getString(R.string.db_users_my_id), String.valueOf(my_id));

                return params;
            }
        };

        RequestHandler.getInstance(feedUserDetailActivity.this).addToRequestQueue(stringRequest);

        return postList;
    }

    /**
     * 아래의 메서드를 오버라이드 하지 않아서 toolbar 뒤로가기 버튼이
     * 작동하지 않았음
     */

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
