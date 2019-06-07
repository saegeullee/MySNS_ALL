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
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {

    private static final String TAG = "FollowerListAdapter";

    public interface OnAddFriendListener {
        void addedFriend(int position);
    }

    public OnAddFriendListener onAddFriendListener;

    public void setOnAddFriendListener(OnAddFriendListener onAddFriendListener) {
        this.onAddFriendListener = onAddFriendListener;
    }

    private Context context;
    private List<User> friendList;

    public UsersListAdapter(Context context, List<User> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_all_people, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final User user = friendList.get(position);

        holder.user_name.setText(user.getUser_name());

        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + user.getProfile_image();
        ImageLoader.getInstance().displayImage(image_url, holder.profile_image);

        holder.addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(user.getId(), holder);
            }
        });

        if(user.isIs_friend()) {
            holder.addFriendBtn.setVisibility(View.INVISIBLE);
            holder.isFriendBtn.setVisibility(View.VISIBLE);
        } else {
            holder.addFriendBtn.setVisibility(View.VISIBLE);
            holder.isFriendBtn.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profile_image;
        private TextView user_name;
        private Button addFriendBtn, isFriendBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.profile_image);
            user_name = itemView.findViewById(R.id.user_name);
            addFriendBtn = itemView.findViewById(R.id.addFriendBtn);
            isFriendBtn = itemView.findViewById(R.id.isFriendBtn);

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
                                holder.isFriendBtn.setVisibility(View.VISIBLE);

                                /**
                                 * 친구 추가 버튼을 누르면 해당 친구를 모든 사용자 리스트에서 제거하기 위해
                                 * 리스너를 액티비티로 전달하여 리사이클러뷰에서 해당 사용자 제거
                                 */
//                                if(onAddFriendListener != null) {
//                                    Log.d(TAG, "onResponse: onAddFriendListener : " + onAddFriendListener);
//                                    onAddFriendListener.addedFriend(position);
//                                }
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


//    @Override
//    public Filter getFilter() {
//        return mFilter;
//    }
//
//    private Filter mFilter = new Filter() {
//        /**
//         * 이 메서드는 백그라운드 쓰레드에서 자동으로 동작한다.
//         */
//        @Override
//        protected FilterResults performFiltering(CharSequence charSequence) {
//            List<User> list = followerList;
//            List<User> result_list = new ArrayList<>(list.size());
//
//            if(charSequence == null || charSequence.length() == 0) {
//                result_list.addAll(followerList);
//            } else {
//                String query = charSequence.toString().toLowerCase().trim();
//                for(User user : followerList) {
//                    if(user.getUser_id().toLowerCase().contains(query)) {
//                        result_list.add(user);
//                    }
//                }
//            }
//
//            FilterResults results = new FilterResults();
//            results.values = result_list;
//            results.count = result_list.size();
//
//            return results;
//        }
//
//        /**
//         * Publish Results to UI Thread
//         */
//        @SuppressWarnings("unchecked")
//        @Override
//        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//
//            /**
//             * Original List 에 필터링 된 결과물을 적용해준다.
//             */
//
//            filteredList = (List<User>) filterResults.values;
//            notifyDataSetChanged();
//        }
//    };

}
