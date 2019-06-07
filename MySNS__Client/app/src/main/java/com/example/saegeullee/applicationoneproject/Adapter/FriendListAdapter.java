package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private static final String TAG = "FollowerListAdapter";

    public interface OnItemClickListener {
        void onItemClick(View view, User user, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private Context context;
    private List<User> friendList;

    public FriendListAdapter(Context context, List<User> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_firend, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final User user = friendList.get(position);

        holder.user_name.setText(user.getUser_name());

        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + user.getProfile_image();
        ImageLoader.getInstance().displayImage(image_url, holder.profile_image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, user, position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public User getUser(int position) {
        return friendList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profile_image;
        private TextView user_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.profile_image);
            user_name = itemView.findViewById(R.id.user_name);

        }
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
