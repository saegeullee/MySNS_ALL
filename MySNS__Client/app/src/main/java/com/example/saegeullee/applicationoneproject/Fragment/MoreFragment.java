package com.example.saegeullee.applicationoneproject.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.FeedListAdapter;
import com.example.saegeullee.applicationoneproject.AddPostActivity;
import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.EditPostActivity;
import com.example.saegeullee.applicationoneproject.EditProfileActivity;
import com.example.saegeullee.applicationoneproject.LoginActivity;
import com.example.saegeullee.applicationoneproject.Models.Post;
import com.example.saegeullee.applicationoneproject.R;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreFragment extends Fragment {

    private static final String TAG = "MoreFragment";

    private View view;
    private CircleImageView profile_image;
    private TextView username;
    private TextView post_number, followee_number, following_number;
    private Button edit_profile_btn;
    private List<Post> postList;

    private String following_num, followee_num;
    private int total_post_num;

    private RecyclerView recyclerView;
    private FeedListAdapter feedListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_more, container, false);

        initUI();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onStart() {

        Log.d(TAG, "onStart: in");
        super.onStart();

        setDataToView();

        Log.d(TAG, "onStart: out");

    }

    private void setDataToView() {

        Log.d(TAG, "setDataToView: start");

        username.setText(SharedPrefManager.getInstance(getActivity()).getUserName());

        String image_url2 = Constants.ROOT_URL_USER_PROFILE_IMAGE + "7.jpg";
        ImageLoader.getInstance().displayImage(image_url2, profile_image);

        if(SharedPrefManager.getInstance(getActivity()).getUserProfileImage() != null) {

            String profile_image_name = SharedPrefManager.getInstance(getActivity()).getUserProfileImage();
            String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + profile_image_name;
            Uri uri = Uri.parse(image_url);

            Log.d(TAG, "setDataToView: profile_image_name : " + profile_image_name);
            Log.d(TAG, "setDataToView: uri : " + uri.toString());

            ImageLoader.getInstance().displayImage(image_url, profile_image);

        }

        /**
         * 사용자가 발행한 포스트 목록 display
         */

        postList = getPostList();
        feedListAdapter = new FeedListAdapter(getActivity(), postList);
        recyclerView.setAdapter(feedListAdapter);
        feedListAdapter.notifyDataSetChanged();

        feedListAdapter.setOnItemCLickListener(new FeedListAdapter.OnMenuItemCLickListener() {
            @Override
            public void onUpdateMenuClicked(int position) {

                Intent intent = new Intent(getActivity(), EditPostActivity.class);
                intent.putExtra(getString(R.string.intent_post_edit), postList.get(position));
                intent.putExtra("position", position);
                startActivity(intent);

            }

            @Override
            public void onDeleteMenuClicked(final int position) {

                final String postId = postList.get(position).getPostId();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("게시물을 삭제하시겠어요?");
                builder.setMessage("삭제한 게시물은 복구가 불가능합니다.");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePost(postId, position);


                    }
                });
                builder.setNegativeButton("취소", null);
                builder.show();


            }
        });

        feedListAdapter.setOnIdItemClickListener(new FeedListAdapter.OnIdItemClickListener() {
            @Override
            public void onIdLinkClicked(int position) {
                Log.d(TAG, "onIdLinkClicked: position");
            }
        });

    }


    private void deletePost(final String postId, final int position) {

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_DELETE_POST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object = new JSONObject(response);

                            Toast.makeText(getActivity(), object.getString("message"), Toast.LENGTH_SHORT).show();

                            /**
                             * 12/25 9:29pm
                             * 포스트 삭제시 삭제한 게시물이 계속 리스트에 보였다.
                             * 아래의 두줄 코드로 게시물 삭제시 곧바로
                             * 현재 프래그먼트에서 삭제되는 것을 확인할 수 있다.
                             *
                             */
                            postList.remove(position);
                            feedListAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_post_id), postId);
                return params;
            }
        };

        RequestHandler.getInstance(getActivity()).addToRequestQueue(stringRequest);

    }

    private void initUI() {

        username = view.findViewById(R.id.username);
        profile_image = view.findViewById(R.id.profile_image);
        post_number = view.findViewById(R.id.post_number);
        followee_number = view.findViewById(R.id.followee_number);
        following_number = view.findViewById(R.id.following_number);

        edit_profile_btn = view.findViewById(R.id.edit_profile_button);
        profile_image = view.findViewById(R.id.profile_image);

        postList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setHasFixedSize(true);

        /**
         * 이 속성을 false 로 줬을 때 뻑뻑했던 스크롤 부드러워짐
         */
        recyclerView.setNestedScrollingEnabled(false);



        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getActivity(), EditProfileActivity.class));

            }
        });

    }

    private List<Post> getPostList() {

        postList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_DISPLAY_INDIVIDUAL_POST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            JSONObject object = jsonObject.getJSONObject("follow_data");
                            Log.d(TAG, "onResponse: follow data : " + object.toString());

                            /**
                             * 팔로잉, 팔로워 정보
                             */
                            following_num = object.getString("following_num");
                            followee_num = object.getString("followee_num");

                            Log.d(TAG, "onResponse: following_number : " + following_number);
                            Log.d(TAG, "onResponse: followee_number : " + followee_num);


                            JSONArray jsonArray = jsonObject.getJSONArray("post_data");

                            /**
                             * 모든 게시물 개수
                             */
                            total_post_num = jsonArray.length();

                            /**
                             * 뷰에 셋팅할 데이터를 모두 패치해 오고 난 뒤에 View 에 setText 를 한다.
                             */

                            followee_number.setText(followee_num);
                            following_number.setText(following_num);
                            post_number.setText(String.valueOf(total_post_num));

                            Log.d(TAG, "onResponse: jsonArray : " + jsonArray.toString());

                            for(int i = 0; i < jsonArray.length(); i ++) {

                                JSONObject obj = jsonArray.getJSONObject(i);
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

                                post.setUser(SharedPrefManager.getInstance(getActivity()).getUserObject());
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
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                int id = SharedPrefManager.getInstance(getActivity()).getId();

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_users_id), String.valueOf(id));

                return params;
            }
        };

        RequestHandler.getInstance(getActivity()).addToRequestQueue(stringRequest);

        return postList;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_fragment_more, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.logout:

                SharedPrefManager.getInstance(getActivity()).logOutUser();
                getActivity().finish();
                startActivity(new Intent(getActivity(), LoginActivity.class));

                break;

            case R.id.addPost:

                startActivity(new Intent(getActivity(), AddPostActivity.class));

                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: in");
        super.onResume();
        Log.d(TAG, "onResume: out");
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: in");
        super.onPause();
        Log.d(TAG, "onPause: out");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: in");
        super.onStop();
        Log.d(TAG, "onStop: out");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: in");
        super.onDestroy();
        Log.d(TAG, "onDestroy: out");
    }


}
