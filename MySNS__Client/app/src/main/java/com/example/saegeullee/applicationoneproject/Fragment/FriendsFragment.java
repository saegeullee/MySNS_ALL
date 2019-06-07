package com.example.saegeullee.applicationoneproject.Fragment;

import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.FriendListAdapter;
import com.example.saegeullee.applicationoneproject.AddFriendActivity;
import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.Dialog.FragmentBottomSheetDialogFull;
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

public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    private View view;
    private TextView textView;
    private RecyclerView recyclerView;
    private FriendListAdapter adapter;

    private List<User> friendList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_friends, container, false);

        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.friends_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setNestedScrollingEnabled(false);

        friendList = new ArrayList<>();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setDataToView();
    }

    private void setDataToView() {

        friendList = getFriendList();
        adapter = new FriendListAdapter(getActivity(), friendList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FriendListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, User user, int position) {

                FragmentBottomSheetDialogFull fragment = new FragmentBottomSheetDialogFull();
                fragment.setUser(user);
                fragment.show(getActivity().getSupportFragmentManager(), fragment.getTag());
            }
        });
    }

    private List<User> getFriendList() {

        friendList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_FRIEND_LIST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            Log.d(TAG, "onResponse: message : " + jsonObject.getString("message"));

                            JSONArray jsonArray = jsonObject.getJSONArray("friend_list");

                            for(int i = 0; i < jsonArray.length(); i++) {

                                JSONObject obj = jsonArray.getJSONObject(i);
                                Log.d(TAG, "onResponse: obj " + obj.toString());

                                User user = new User(obj, getActivity());
                                Log.d(TAG, "onResponse: user : " + user.toString());

                                friendList.add(user);


                            }

                            FragmentBottomSheetDialogFull fragment = new FragmentBottomSheetDialogFull();
                            fragment.setUser(adapter.getUser(0));
//                            fragment.show(getActivity().getSupportFragmentManager(), fragment.getTag());


                            Log.d(TAG, "onResponse: friendList size : " + friendList.size());
                            adapter.notifyDataSetChanged();

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

                /**
                 * 현재 사용자의 친구를 모두 불러온다.
                 */

                int id = SharedPrefManager.getInstance(getActivity()).getId();

                params.put(getActivity().getString(R.string.db_users_id), String.valueOf(id));

                return params;
            }
        };

        RequestHandler.getInstance(getActivity()).addToRequestQueue(stringRequest);
        return friendList;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_fragment_friends, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addFriend:
                Intent intent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
