package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
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
import com.example.saegeullee.applicationoneproject.feedUserDetailActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerListAdapter extends RecyclerView.Adapter<FollowerListAdapter.ViewHolder>
    implements Filterable {

    private static final String TAG = "FollowerListAdapter";

    private Context context;
    private List<User> followerList;
    private List<User> filteredList;

    public FollowerListAdapter(Context context, List<User> followerList) {
        this.context = context;
        this.followerList = followerList;
        this.filteredList = followerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final User user = filteredList.get(position);

        holder.user_id.setText(user.getUser_id());

        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + user.getProfile_image();
        ImageLoader.getInstance().displayImage(image_url, holder.profile_image);

        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, feedUserDetailActivity.class);
                intent.putExtra(context.getString(R.string.db_users_object), user);
                context.startActivity(intent);
            }
        });

        /**
         * 현재 사용자가 팔로우 목록의 사람들을 팔로우하고 있는지에 대한
         * 여부에 따라 버튼을 다르게 보여줌
         */

        if(user.getIs_following()) {
            holder.followBtn.setVisibility(View.INVISIBLE);
            holder.unfollowBtn.setVisibility(View.VISIBLE);
        } else {
            holder.followBtn.setVisibility(View.VISIBLE);
            holder.unfollowBtn.setVisibility(View.INVISIBLE);
        }


        holder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * 현재 Row 아이템에 있는 유저를 팔로우,
                 * follow 후 다시 버튼을 unfollow 로 업데이트 하기위해 holder 를 인자로 전달.
                 */

                followUser(user.getId(), holder);
            }
        });

        holder.unfollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unfollowUser(user.getId(), holder);
            }
        });

    }


    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profile_image;
        private TextView user_id;
        private ConstraintLayout item_layout;
        private Button followBtn, unfollowBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.profile_image);
            user_id = itemView.findViewById(R.id.user_name);
            item_layout = itemView.findViewById(R.id.item_layout);
            followBtn = itemView.findViewById(R.id.followBtn);
            unfollowBtn = itemView.findViewById(R.id.unfollowBtn);

        }
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private Filter mFilter = new Filter() {
        /**
         * 이 메서드는 백그라운드 쓰레드에서 자동으로 동작한다.
         */
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<User> list = followerList;
            List<User> result_list = new ArrayList<>(list.size());

            if(charSequence == null || charSequence.length() == 0) {
                result_list.addAll(followerList);
            } else {
                String query = charSequence.toString().toLowerCase().trim();
                for(User user : followerList) {
                    if(user.getUser_id().toLowerCase().contains(query)) {
                        result_list.add(user);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = result_list;
            results.count = result_list.size();

            return results;
        }

        /**
         * Publish Results to UI Thread
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            /**
             * Original List 에 필터링 된 결과물을 적용해준다.
             */

            filteredList = (List<User>) filterResults.values;
            notifyDataSetChanged();
        }
    };

    private void followUser(final int id, final ViewHolder holder) {

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_FOLLOW_USER,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            Toast.makeText(context,
                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            /**
                             * 팔로우 버튼 누르고 나서 서버의 응답을 토대로
                             * 버튼 셋팅하기
                             */

                            holder.followBtn.setVisibility(View.INVISIBLE);
                            holder.unfollowBtn.setVisibility(View.VISIBLE);


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

                int follower_id = SharedPrefManager.getInstance(context).getId();
                int followee_id = id;

                params.put(context.getString(R.string.db_follower_id), String.valueOf(follower_id));
                params.put(context.getString(R.string.db_followee_id), String.valueOf(followee_id));

                return params;
            }
        };

        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }

    private void unfollowUser(final int id, final ViewHolder holder) {

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_UNFOLLOW_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            Toast.makeText(context,
                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            holder.followBtn.setVisibility(View.VISIBLE);
                            holder.unfollowBtn.setVisibility(View.INVISIBLE);


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

                int follower_id = SharedPrefManager.getInstance(context).getId();
                int followee_id = id;

                params.put(context.getString(R.string.db_follower_id), String.valueOf(follower_id));
                params.put(context.getString(R.string.db_followee_id), String.valueOf(followee_id));

                return params;
            }
        };

        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }

}
