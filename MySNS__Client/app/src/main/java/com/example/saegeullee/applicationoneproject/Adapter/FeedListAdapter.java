package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.Models.Post;
import com.example.saegeullee.applicationoneproject.PostCommentActivity;
import com.example.saegeullee.applicationoneproject.R;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.example.saegeullee.applicationoneproject.feedUserDetailActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.ViewHolder> {

    private static final String TAG = "FeedListAdapter";

    /**
     * 게시물의 메뉴 클릭 리스너
     */

    private OnMenuItemCLickListener onMenuItemCLickListener;

    public interface OnMenuItemCLickListener {
        void onUpdateMenuClicked(int position);
        void onDeleteMenuClicked(int position);
    }

    public void setOnItemCLickListener(OnMenuItemCLickListener listener) {
        this.onMenuItemCLickListener = listener;
    }

    /**
     * 게시물 아이디 클릭 리스너
     */

    private OnIdItemClickListener onIdItemClickListener;

    public interface OnIdItemClickListener {
        void onIdLinkClicked(int position);
    }

    public void setOnIdItemClickListener(OnIdItemClickListener listener) {
        this.onIdItemClickListener = listener;
    }

    private Context context;
    private List<Post> postList;

    private String post_id;

    public FeedListAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_feed, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Post post = postList.get(position);

        Log.d(TAG, "onBindViewHolder: post get like post : " + post.isLikePost());

        /**
         *  게시물의 사용자 상호작용 부분(좋아요 댓글 등)
         */

        if(Integer.parseInt(post.getLikes()) > 0) {
            holder.relativeLayout.setVisibility(View.VISIBLE);
            holder.likeCountText.setText("좋아요 " + post.getLikes() + "개");
        } else {
            holder.relativeLayout.setVisibility(View.GONE);
            holder.likeCountText.setText("");
        }

        /**
         * 12/30 11:52am 에러발생
         * 서버에서 object.getString("post_comment_num") 으로 받아서
         * Post 의 commentNumber 멤버의 자료형을 String 으로 주고 Set 해주고
         * 어댑터에서 Integer.parseInt(post.getCommentNumber()) 로 받아주니 계속
         * java.lang.NumberFormatException: s == null 에러가 발생했다.
         * -> 해결책 서버에서 데이터를 받는 자료형을 int 로 받아서 처리했다.
         *
         */
        if(post.getCommentNumber() > 0) {
            holder.showAllCommentText.setText("댓글 " + post.getCommentNumber() + "개 모두보기");
        } else {
            holder.showAllCommentText.setText("");
        }

        Log.d(TAG, "onBindViewHolder: post comment number : " + post.getCommentNumber());
        Log.d(TAG, "onBindViewHolder: getLikePost : " + post.isLikePost());

        if(post.isLikePost()) {
            holder.likeBtn.setVisibility(View.INVISIBLE);
            holder.likeRedBtn.setVisibility(View.VISIBLE);
        } else {
            holder.likeBtn.setVisibility(View.VISIBLE);
            holder.likeRedBtn.setVisibility(View.INVISIBLE);
        }

        /**
         * 이 게시물이 내 것인가 다른 사람의 것인가?
         */

        Log.d(TAG, "onBindViewHolder: post user id : " + post.getUser().getId());
        Log.d(TAG, "onBindViewHolder: shared user id : " + SharedPrefManager.getInstance(context).getId());

        if(post.getUser().getId() == SharedPrefManager.getInstance(context).getId()) {
            String profile_image_name = SharedPrefManager.getInstance(context).getUserProfileImage();
            String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + profile_image_name;

            ImageLoader.getInstance().displayImage(image_url, holder.profileImage);
            holder.userId.setText(SharedPrefManager.getInstance(context).getUserId());

        } else {

            String profile_image_name = post.getUser().getProfile_image();
            String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + profile_image_name;

            ImageLoader.getInstance().displayImage(image_url, holder.profileImage);
            holder.userId.setText(post.getUser().getUser_id());

        }

        holder.description.setText(post.getDescription());
        holder.postDate.setText(post.getDate());

        /**
         * 댓글 창의 프로필 이미지는 항상 내 것으로
         */

        String profile_image_name = SharedPrefManager.getInstance(context).getUserProfileImage();
        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + profile_image_name;
        ImageLoader.getInstance().displayImage(image_url, holder.profileImageBottom);

        String post_url = Constants.ROOT_URL_POST_IMAGE + post.getPostId() + "/" + post.getPostImage();
        ImageLoader.getInstance().displayImage(post_url, holder.postImage);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.update:
                                onMenuItemCLickListener.onUpdateMenuClicked(position);
                                return true;

                            case R.id.delete:
                                onMenuItemCLickListener.onDeleteMenuClicked(position);
                                return true;
                        }
                        return false;
                    }
                });

//                popupMenu.inflate(R.menu.menu_feed_more_btn);
//                popupMenu.show();
            }
        });

        holder.topSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, feedUserDetailActivity.class);
                intent.putExtra(context.getString(R.string.db_users_object), post.getUser());
                context.startActivity(intent);

            }
        });


        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onLikeButtonClickListener.onLikeButtonClicked(position);
                likePost(position, holder);
                holder.likeBtn.setVisibility(View.INVISIBLE);
                holder.likeRedBtn.setVisibility(View.VISIBLE);
            }
        });


        holder.likeRedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                unLikePost(position, holder);
                holder.likeBtn.setVisibility(View.VISIBLE);
                holder.likeRedBtn.setVisibility(View.INVISIBLE);

            }
        });

        /**
         * 댓글 액티비티로 이동
         * 인텐트로 포스트 id 만 넘겨준다.
         */
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostCommentActivity.class);
                intent.putExtra(context.getString(R.string.intent_post_id), post);
                context.startActivity(intent);
            }
        });

        holder.showAllCommentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostCommentActivity.class);
                intent.putExtra(context.getString(R.string.intent_post_id), post);
                context.startActivity(intent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeLayout;
        private CircleImageView profileImage, profileImageBottom;
        private ImageView postImage;
        private TextView userId, description, postDate;
        private ImageView moreBtn, commentBtn, shareBtn;
        private ImageView likeBtn, likeRedBtn;
        private RelativeLayout topSection;
        private TextView likeCountText, showAllCommentText, commentText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            topSection = itemView.findViewById(R.id.topSection);

            profileImage = itemView.findViewById(R.id.userProfileImage);
            profileImageBottom = itemView.findViewById(R.id.userProfileImageBottom);
            postImage = itemView.findViewById(R.id.postContentImage);

            postDate = itemView.findViewById(R.id.postDate);
            userId = itemView.findViewById(R.id.userId);
            description = itemView.findViewById(R.id.descriptionET);

            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeButton);
            likeRedBtn = itemView.findViewById(R.id.likeRedBtn);

            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);

            likeCountText = itemView.findViewById(R.id.likeCountText);
            showAllCommentText = itemView.findViewById(R.id.showAllCommentText);

            relativeLayout = itemView.findViewById(R.id.likeCountWrapper);

        }
    }

    private void likePost(int position, final ViewHolder holder) {

        post_id = postList.get(position).getPostId();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_LIKE_POST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "onResponse: like num : " + jsonObject.getString("likes_num"));

                            /**
                             * 사용자가 좋아요를 누르면 해당 게시물의 좋아요 개수를 서버로부터 받아와서
                             * 좋아요 개수를 업데이트 한다.
                             */
                            String likes_num = jsonObject.getString("likes_num");
//                            holder.likeCountText.setText(likes_num + context.getString(R.string.num_count));

                            if(Integer.parseInt(likes_num) > 0) {
                                holder.relativeLayout.setVisibility(View.VISIBLE);
                                holder.likeCountText.setText("좋아요 " + likes_num + "개");
                            } else {
                                holder.relativeLayout.setVisibility(View.GONE);
                                holder.likeCountText.setText("");
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

                /**
                 * 게시물에 좋아요를 하려면 현재 사용자가 몇번 게시물의 좋아요를 눌렀는지를
                 * 서버에 알려줘야 하기 때문에 현재 사용자의 아이디와 게시물의 아이디를 서버로 보내준다.
                 */

                int current_user_id = SharedPrefManager.getInstance(context).getId();

                Map<String, String> params = new HashMap<>();
                params.put(context.getString(R.string.db_users_id), String.valueOf(current_user_id));
                params.put(context.getString(R.string.db_post_id), post_id);

                return params;
            }
        };

        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }

    private void unLikePost(int position, final ViewHolder holder) {

        post_id = postList.get(position).getPostId();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_UNLIKE_POST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "onResponse: like num : " + jsonObject.getString("likes_num"));

                            /**
                             * 사용자가 좋아요를 취소하면 해당 게시물의 좋아요 개수를 서버로부터 받아와서
                             * 좋아요 개수를 업데이트 한다.
                             */
                            String likes_num = jsonObject.getString("likes_num");
//                            holder.likeCountText.setText(likes_num + context.getString(R.string.num_count));

                            if(Integer.parseInt(likes_num) > 0) {
                                holder.relativeLayout.setVisibility(View.VISIBLE);
                                holder.likeCountText.setText("좋아요 " + likes_num + "개");
                            } else {
                                holder.relativeLayout.setVisibility(View.GONE);
                                holder.likeCountText.setText("");
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

                /**
                 * 게시물에 좋아요를 하려면 현재 사용자가 몇번 게시물의 좋아요를 눌렀는지를
                 * 서버에 알려줘야 하기 때문에 현재 사용자의 아이디와 게시물의 아이디를 서버로 보내준다.
                 */

                int current_user_id = SharedPrefManager.getInstance(context).getId();

                Map<String, String> params = new HashMap<>();
                params.put(context.getString(R.string.db_users_id), String.valueOf(current_user_id));
                params.put(context.getString(R.string.db_post_id), post_id);

                return params;
            }
        };

        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }
}
