package com.example.saegeullee.applicationoneproject.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.FeedListAdapter;
import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.Models.Post;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.R;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    private View view;
    private RecyclerView recyclerView;
    private FeedListAdapter feedListAdapter;
    private List<Post> postList;
//    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: in");

        view = inflater.inflate(R.layout.fragment_feed, container, false);

        setHasOptionsMenu(true);
        initUI();

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: in");
        setDataToView();

        super.onStart();
    }

    private void setDataToView() {
        Log.d(TAG, "setDataToView: in");

        postList = getAllPostList();
        feedListAdapter = new FeedListAdapter(getActivity(), postList);
        recyclerView.setAdapter(feedListAdapter);

        feedListAdapter.setOnItemCLickListener(new FeedListAdapter.OnMenuItemCLickListener() {
            @Override
            public void onUpdateMenuClicked(int position) {

            }

            @Override
            public void onDeleteMenuClicked(int position) {

            }
        });

        feedListAdapter.setOnIdItemClickListener(new FeedListAdapter.OnIdItemClickListener() {
            @Override
            public void onIdLinkClicked(int position) {
                Log.d(TAG, "onIdLinkClicked: clicked " + position);

            }
        });
    }

    private void initUI() {
        Log.d(TAG, "initUI: in");

        postList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

//        progressDialog = new ProgressDialog(getActivity());


    }

    /**
     * 모든 사용자의 게시물을 가져오려면 클라이언트에서 서버로 보내야 하는 정보가 없어도 되지만
     * 이건 임시적으로 이렇게 하는 것이고 추후에는 클라의 팔로워들의 게시물만 가져와야 하므로
     * 어쨋든 클라이언트의 id를 서버로 보내야한다.
     *
     * 좋아요와 댓글, 대댓글도 모두 가져와야 한다..
     */

    private List<Post> getAllPostList() {

        Log.d(TAG, "getAllPostList: in");

        postList.clear();
//        progressDialog.setMessage("불러오는 중..");
//        progressDialog.show();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_DISPLAY_ALL_POST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "onResponse: response : " + response);

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            JSONArray jsonArray = jsonObject.getJSONArray("data");

                            for(int i =0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);
//                                Log.d(TAG, "onResponse: object : " + object.toString());
//                                Log.d(TAG, "onResponse: id : " + object.getString("id"));

                                Log.d(TAG, "onResponse: item : " + '\n' +
                                        "id : " + object.getString("id") + '\n' +
                                        "user_id : " + object.getString("user_id") + '\n' +
                                        "profile_image : " + object.getString("profile_image") + '\n' +
                                        "post_id : " + object.getString("post_id") + '\n' +
                                        "description : " + object.getString("description") + '\n' +
                                        "post_image : " + object.getString("post_image") + '\n' +
                                        "created_at : " + object.getString("created_at") + '\n' +
                                        "post_likes : " + object.getString("post_likes") + '\n' +
                                        "is_like_post : " + object.getString("is_like_post") + '\n' +
                                        "post_comment_num : " + object.getString("post_comment_num") + '\n'

                                );

                                User user = new User();
                                Post post = new Post();

                                user.setId(Integer.parseInt(object.getString("id")));
                                user.setUser_id(object.getString("user_id"));
                                user.setProfile_image(object.getString("profile_image"));

                                post.setUser(user);
                                post.setPostId(object.getString("post_id"));
                                post.setDescription(object.getString("description"));
                                post.setDate(object.getString("created_at"));
                                post.setPostImage(object.getString("post_image"));
                                post.setLikes(object.getString("post_likes"));
                                post.setCommentNumber(object.getInt("post_comment_num"));

                                /**
                                 * 현재 사용자가 이 게시물의 좋아요를 눌렀는지 안눌렀는지의 여부
                                 */

                                if(object.getString("is_like_post").equals("true")) {
                                    post.setLikePost(true);
                                } else {
                                    post.setLikePost(false);
                                }

                                Log.d(TAG, "onResponse: post is like : " + post.isLikePost());

                                postList.add(post);

//                                progressDialog.hide();

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
                Log.d(TAG, "onErrorResponse: error : " + error.getMessage());

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                int id = SharedPrefManager.getInstance(getActivity()).getId();

                params.put(getString(R.string.db_users_id), String.valueOf(id));

                return params;
            }
        };

        Log.d(TAG, "getAllPostList: out");

        RequestHandler.getInstance(getActivity()).addToRequestQueue(stringRequest);
        
        return postList;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_fragment_friends, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Snackbar.make(view, item.getTitle() + "Clicked", Snackbar.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }


}
