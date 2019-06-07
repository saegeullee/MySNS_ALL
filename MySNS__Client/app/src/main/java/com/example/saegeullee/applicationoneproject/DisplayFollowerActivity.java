package com.example.saegeullee.applicationoneproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.FollowerListAdapter;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayFollowerActivity extends AppCompatActivity {

    private static final String TAG = "DisplayFollowerActivity";

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FollowerListAdapter adapter;

    private List<User> followerList;
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_follower);

        initUI();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setDataToView();

    }

    private void setDataToView() {

        followerList = getFollowerList();

        adapter = new FollowerListAdapter(DisplayFollowerActivity.this, followerList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void initUI() {

        getUserId();

        toolbar = findViewById(R.id.displayFollowerToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("팔로워");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(DisplayFollowerActivity.this, 1));
        recyclerView.setNestedScrollingEnabled(false);

        followerList = new ArrayList<>();

    }

    private void getUserId() {

        if(getIntent().hasExtra(getString(R.string.intent_user_id))) {
            user_id = getIntent().getIntExtra(getString(R.string.intent_user_id), 1);

            Log.d(TAG, "getUserId: id : " + user_id);
        }
    }

    private List<User> getFollowerList() {

        followerList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_FOLLOWER_LIST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(DisplayFollowerActivity.this,
//                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            JSONArray jsonArray = jsonObject.getJSONArray("follower_result");
                            Log.d(TAG, "onResponse: jsonArray : " + jsonArray.toString());

                            for(int i =0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);

                                Log.d(TAG, "onResponse: " + "\n" +
                                        "id : " + object.getString("id") + "\n" +
                                        "user_id : " + object.getString("user_id") + "\n" +                                        "id : " + object.getString("id") + "\n" +
                                        "profile_image : " + object.getString("profile_image") + "\n" +
                                        "is_following : " + object.getString("is_following")
                                );

                                User user = new User();
                                user.setId(Integer.parseInt(object.getString("id")));
                                user.setUser_id(object.getString("user_id"));
                                user.setProfile_image(object.getString("profile_image"));

                                /**
                                 * 현재 사용자가 다른 사용자의 팔로잉 목록의 사용자들을
                                 * 팔로우 하고 있는지에 대한 여부
                                 */

                                if(object.getString("is_following").equals("true")){
                                    user.setIs_following(true);
                                } else {
                                    user.setIs_following(false);
                                }

                                /**
                                 * 이 목록에 내가 팔로워로 있다면 굳이 목록에서까지 나를 보여줄 필요가 없다.
                                 */

                                if(user.getId() != SharedPrefManager.getInstance(DisplayFollowerActivity.this).getId()) {

                                    followerList.add(user);
                                }

                            }
                            /**
                             * 12/31 12:15 am
                             * 이 코드 안적어서 계속 리사이클러뷰 안뜸..
                             */
                            adapter.notifyDataSetChanged();

                            Log.d(TAG, "onResponse: follower List size : " + followerList.size());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(DisplayFollowerActivity.this,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                /**
                 * 현재 사용자가 다른 사람의 팔로우 목록을 보고 있고
                 * 현재 사용자가 그 목록의 사람들을 팔로우 하고 있는지에 대한 정보를 얻어오기
                 * 위해 현재 사용자의 id 도 서버로 같이 보낸다.
                 */
                Map<String, String> params = new HashMap<>();

                int current_user_id = SharedPrefManager.getInstance(DisplayFollowerActivity.this).getId();

                params.put(getString(R.string.db_users_id), String.valueOf(user_id));
                params.put(getString(R.string.db_users_my_id), String.valueOf(current_user_id));

                return params;
            }
        };

        RequestHandler.getInstance(DisplayFollowerActivity.this).addToRequestQueue(stringRequest);

        return followerList;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.follower_search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
