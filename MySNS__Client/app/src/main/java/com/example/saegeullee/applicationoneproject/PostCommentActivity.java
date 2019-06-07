package com.example.saegeullee.applicationoneproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.CommentListAdapter;
import com.example.saegeullee.applicationoneproject.Models.Comment;
import com.example.saegeullee.applicationoneproject.Models.Post;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostCommentActivity extends AppCompatActivity {

    private static final String TAG = "PostCommentActivity";

    public static interface ClickListener{
        public void onClick(View view,int position);
        public void onLongClick(View view,int position);
    }

    private Toolbar toolbar;
    private CircleImageView profileImage, profileImageBottom;
    private TextView userId, description, timeStamp;
    private RecyclerView recyclerView;
    private EditText commentET;
    private Button commentBtn;

    private Post post;

    private List<Comment> commentList;
    private CommentListAdapter adapter;
    private RelativeLayout commentToOtherNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);

        getPostId();
        initUI();
    }

    private void setDataToView() {

        Log.d(TAG, "setDataToView: post : " + post.toString());
        Log.d(TAG, "setDataToView: user : " + post.getUser().toString());


        commentList = getComment();

        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + post.getUser().getProfile_image();
        ImageLoader.getInstance().displayImage(image_url, profileImage);

        String my_image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + SharedPrefManager.getInstance(PostCommentActivity.this).getUserProfileImage();
        ImageLoader.getInstance().displayImage(my_image_url, profileImageBottom);

        userId.setText(post.getUser().getUser_id());
        description.setText(post.getDescription());
        timeStamp.setText(post.getDate());


//        adapter = new CommentListAdapter(this, commentList);
//        recyclerView.setAdapter(adapter);
//        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        adapter = new CommentListAdapter(this, commentList);

    }


    @Override
    protected void onStart() {
        super.onStart();
        setDataToView();

    }

    /**
     * 12/28 6:04pm 문제점 해결
     *
     * 원래 iniCommentList 메서드의 내용을 setDataToView() 메서드 하단에 작성했었다.
     * 이렇게 했을 때 댓글 목록을 불러올 때 맨 아래 글로 스크롤이 이동하지 않았다.
     * -> initCommentList 메서드를 작성하고 이 메서드를 모든 댓글을 불러오는 메서드인
     * getComment() 의 댓글 fetch 후 while 문 끝나는 곳에 넣어줬더니 스크롤이 이동이 되었다.
     *
     */

    private void initCommentList() {
        adapter = new CommentListAdapter(this, commentList);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

    }

    private void getPostId() {

        if(getIntent().hasExtra(getString(R.string.intent_post_id))) {
            post = getIntent().getParcelableExtra(getString(R.string.intent_post_id));
            Log.d(TAG, "getPostId: " + post.toString());
        }

    }

    private void initUI() {

        toolbar = findViewById(R.id.post_comment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("댓글");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        profileImage = findViewById(R.id.profile_image);
        userId = findViewById(R.id.user_name);
        description = findViewById(R.id.description);
        timeStamp = findViewById(R.id.timestamp);
        commentToOtherNotice = findViewById(R.id.commentToOtherNotice);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, final int position) {
//                Toast.makeText(PostCommentActivity.this, "long press on position : " + position, Toast.LENGTH_SHORT).show();

                /**
                 * 내가 쓴 댓글만 수정 또는 삭제 가능
                 */

                if(commentList.get(position).getUser().getId() == SharedPrefManager.getInstance(PostCommentActivity.this).getId()) {

                    PopupMenu popupMenu = new PopupMenu(PostCommentActivity.this, view);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            switch (menuItem.getItemId()) {
                                case R.id.update:

//                                    Toast.makeText(PostCommentActivity.this, "수정", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(PostCommentActivity.this, EditCommentActivity.class);
                                    intent.putExtra(getString(R.string.db_post_comment), commentList.get(position));
                                    startActivity(intent);
                                    return true;

                                case R.id.delete:

                                    AlertDialog.Builder builder = new AlertDialog.Builder(PostCommentActivity.this);
                                    builder.setTitle("댓글을 삭제하시겠습니까?");
                                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            Toast.makeText(PostCommentActivity.this, "삭제", Toast.LENGTH_SHORT).show();
                                            deleteComment(position);
                                        }
                                    });
                                    builder.setNegativeButton("취소", null);
                                    builder.show();

                                    return true;

                            }
                            return false;
                        }
                    });

                    popupMenu.inflate(R.menu.comment_option_menu);
                    popupMenu.show();

                }
            }
        }));

        commentList = new ArrayList<>();

        /**
         * 댓글 창
         */
        profileImageBottom = findViewById(R.id.profileImageBottom);
        commentET = findViewById(R.id.commentET);
        commentBtn = findViewById(R.id.commentBtn);

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                insertComment();
            }
        });

    }

    private void deleteComment(final int position) {

        final String commentId = commentList.get(position).getCommentId();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_DELETE_COMMENT,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(PostCommentActivity.this,
//                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            commentList.remove(position);
                            adapter.notifyItemRemoved(position);
//                            initCommentList();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PostCommentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_post_comment_id), commentId);
                return params;
            }
        };

        RequestHandler.getInstance(PostCommentActivity.this).addToRequestQueue(stringRequest);

    }

    private void insertComment() {

        final String commentText = commentET.getText().toString().trim();

        if (!TextUtils.isEmpty(commentText)) {

            StringRequest stringRequest = new StringRequest(

                    Request.Method.POST,
                    Constants.URL_INSERT_COMMENT_TO_POST,

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);

//                                Toast.makeText(PostCommentActivity.this,
//                                        jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                                /**
                                 * 사용자가 입력한 댓글을 서버로 부터 다시 받아서
                                 * 클라이언트의 화면에 더해준다.
                                 * -> fetch success 라면 굳이 다시 서버로 부터 받을 필요 있나?
                                 * error false 만 체크하고 클라이언트가 입력한 코멘트를 클라이언트에서 받은
                                 * 데이터로 즉시 코멘트 리사이클러 뷰에 추가해준다.
                                 * -> 현재 코멘트 작성 시간은 서버로부터 받아와서 입력하는구나..
                                 * -> 시간도 굳이 서버로 받아올 필요 없고 클라이언트에서 현재 시간을 측정해서 추가하면된다.
                                 *
                                 * -> 서버로부터 댓글을 입력하고 바로 클라이언트에서 입력된 댓글의 id를 가지고 있어야
                                 * 업데이트를 할 수 있기 때문에 commentID를 가져와라
                                 */

                                if(jsonObject.getString("error").equals("false")) {

                                    String comment_id = jsonObject.getString("comment_id");

                                    Comment comment = new Comment();
                                    User user = new User();
                                    user.setProfile_image(SharedPrefManager.getInstance(PostCommentActivity.this).getUserProfileImage());
                                    user.setId(SharedPrefManager.getInstance(PostCommentActivity.this).getId());
                                    user.setUser_id(SharedPrefManager.getInstance(PostCommentActivity.this).getUserId());
                                    comment.setUser(user);
                                    comment.setCommentText(commentText);
                                    comment.setDate(getTimeStamp());
                                    comment.setCommentId(comment_id);

                                    commentList.add(comment);
                                    initCommentList();
                                    adapter.notifyDataSetChanged();

                                }

                                commentET.setText("");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(PostCommentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    /**
                     * 현재 사용자가 어떤 게시물에 어떤 내용의 댓글을 남겼는가?
                     */

                    Map<String, String> params = new HashMap<>();

                    int current_user_id = SharedPrefManager.getInstance(PostCommentActivity.this).getId();

                    params.put(getString(R.string.db_users_id), String.valueOf(current_user_id));
                    params.put(getString(R.string.db_post_id), post.getPostId());
                    params.put(getString(R.string.db_post_comment), commentText);

                    return params;
                }
            };

            RequestHandler.getInstance(PostCommentActivity.this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private List<Comment> getComment() {

        commentList.clear();

        Log.d(TAG, "getComment: in");

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_ALL_COMMENTS,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(PostCommentActivity.this,
//                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            JSONArray jsonArray = jsonObject.getJSONArray("comment_data");

                            for(int i=0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);

                                Log.d(TAG, "onResponse: comment_data : " + '\n' +
                                    "id : " + object.getString("id") + '\n' +
                                    "comment_id : " + object.getString("comment_id") + '\n' +

                                    "user_id : " + object.getString("user_id") + '\n' +
                                    "profile_image : " + object.getString("profile_image") + '\n' +
                                    "text : " + object.getString("text") + "\n" +
                                    "created_at : " + object.getString("created_at") + '\n'
                                );

                                Comment comment = new Comment();

                                comment.setCommentText(object.getString("text"));
                                comment.setDate(object.getString("created_at"));
                                comment.setCommentId(object.getString("comment_id"));

                                User user = new User();
                                user.setId(Integer.parseInt(object.getString("id")));
                                user.setUser_id(object.getString("user_id"));
                                user.setProfile_image(object.getString("profile_image"));

                                comment.setUser(user);

                                commentList.add(comment);

                            }

                            Log.d(TAG, "onResponse: commentList size : " + commentList.size());
                            initCommentList();
                            adapter.notifyDataSetChanged();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(PostCommentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                /**
                 * 모든 댓글 패치
                 * 어떤 포스트의 댓글을 가져올 것인가?
                 */

                Map<String, String> params = new HashMap<>();

                params.put(getString(R.string.db_post_id), post.getPostId());

                return params;
            }
        };

        RequestHandler.getInstance(PostCommentActivity.this).addToRequestQueue(stringRequest);

        return commentList;

    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(new Date());
    }


    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}
