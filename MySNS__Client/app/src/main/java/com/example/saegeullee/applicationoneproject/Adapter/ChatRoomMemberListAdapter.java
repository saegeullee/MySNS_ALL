package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.R;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomMemberListAdapter extends RecyclerView.Adapter<ChatRoomMemberListAdapter.ViewHolder> {

    private static final String TAG = "ChatRoomMemberListAdapt";

    private Context context;
    private List<User> userList;

    public ChatRoomMemberListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_member_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final User user = userList.get(position);

        holder.userName.setText(user.getUser_id());

        if(!user.isIs_friend()) {
            if(user.getId() != SharedPrefManager.getInstance(context).getId())
                holder.addFriendBtn.setVisibility(View.VISIBLE);
        }

        holder.addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(user.getId(), holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName;
        private Button addFriendBtn;

        public ViewHolder(View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.user_name);
            addFriendBtn = itemView.findViewById(R.id.addFriendBtn);
        }
    }

    private void addFriend(final int user_id, final ViewHolder holder) {

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_ADD_FRIEND,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            Log.d(TAG, "onResponse: message : " + jsonObject.getString("message"));

                            if(jsonObject.getString("error").equals("false")) {

                                Log.d(TAG, "onResponse: error : " + jsonObject.getString("error"));

                                holder.addFriendBtn.setVisibility(View.INVISIBLE);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                /**
                 * 현재 사용자의 아이디와 친구를 맺고 싶은 상대방의 아이디를 보낸다.
                 */

                int my_id = SharedPrefManager.getInstance(context).getId();

                params.put(context.getString(R.string.db_users_friend_id), String.valueOf(user_id));
                params.put(context.getString(R.string.db_users_id), String.valueOf(my_id));

                return params;
            }
        };

        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }
}
