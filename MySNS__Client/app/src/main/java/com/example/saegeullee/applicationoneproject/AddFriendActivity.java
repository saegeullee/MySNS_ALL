package com.example.saegeullee.applicationoneproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.UsersListAdapter;
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

public class AddFriendActivity extends AppCompatActivity implements UsersListAdapter.OnAddFriendListener {

    private static final String TAG = "AddFriendActivity";

    @Override
    public void addedFriend(int position) {

        /**
         * 친구 추가를 하면 모든 친구 목록에서 삭제
         */

        allUserList.remove(position);
        adapter.notifyItemRemoved(position);

    }

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private UsersListAdapter adapter;

    private List<User> allUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        initUI();
        setDataToView();
    }

    private void setDataToView() {

        allUserList = getAllUsers();
        adapter = new UsersListAdapter(this,allUserList);
        adapter.setOnAddFriendListener(this);
        recyclerView.setAdapter(adapter);

    }


    private void initUI() {

        toolbar = findViewById(R.id.addfriend_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("친구 추가");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allUserList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(AddFriendActivity.this, 1));
        recyclerView.setNestedScrollingEnabled(false);

    }


    private List<User> getAllUsers() {

        allUserList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_ALL_USER_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object = new JSONObject(response);

                            Log.d(TAG, "onResponse: message : " + object.getString("message"));

                            JSONArray jsonArray = object.getJSONArray("all_users_list");

                            for(int i = 0 ; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                Log.d(TAG, "onResponse: obj" + obj.toString());

                                User user = new User(obj, AddFriendActivity.this);

                                if(obj.getString("is_friend").equals("true")) {
                                    user.setIs_friend(true);
                                } else {
                                    user.setIs_friend(false);
                                }

                                Log.d(TAG, "onResponse: user : " + user.toString());

                                allUserList.add(user);

                            }

                            adapter.notifyDataSetChanged();

                            Log.d(TAG, "onResponse: allUserList : " + allUserList.size());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddFriendActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                /**
                 * 현재 사용자의 id 를 보내줘서 모든 사용자 목록중에 각 사용자가 현재 사용자와
                 * 친구인지의 여부에 대한 데이터도 가져온다.
                 */

                int id = SharedPrefManager.getInstance(AddFriendActivity.this).getId();

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_users_id) , String.valueOf(id));

                return params;
            }
        };
        RequestHandler.getInstance(AddFriendActivity.this).addToRequestQueue(stringRequest);

        return allUserList;
    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
