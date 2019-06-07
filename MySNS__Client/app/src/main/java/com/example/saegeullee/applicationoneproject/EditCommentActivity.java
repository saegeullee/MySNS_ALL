package com.example.saegeullee.applicationoneproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Models.Comment;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditCommentActivity extends AppCompatActivity {

    private static final String TAG = "EditCommentActivity";

    private CircleImageView profileImage;
    private TextView user_id;
    private EditText editComment;
    private Button updateBtn, cancelBtn;

    private Comment comment;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);

        getComment();
        initUI();
    }

    private void getComment() {

        /**
         * 댓글 업데이트 할 때 필요한 데이터인 commentID 가 널 값이네..
         */

        if(getIntent().hasExtra(getString(R.string.db_post_comment))) {
            comment = getIntent().getParcelableExtra(getString(R.string.db_post_comment));
            Log.d(TAG, "getComment: comment : " + comment.toString());
        }


    }

    private void initUI() {

        profileImage = findViewById(R.id.profile_image);
        user_id = findViewById(R.id.user_name);
        editComment = findViewById(R.id.editComment);
        updateBtn = findViewById(R.id.updateBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        user_id.setText(comment.getUser().getUser_id());
        editComment.setText(comment.getCommentText());

        toolbar = findViewById(R.id.edit_comment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("댓글 수정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + comment.getUser().getProfile_image();
        ImageLoader.getInstance().displayImage(image_url, profileImage);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateComment();
            }
        });

    }

    private void updateComment() {

        final String updateCommentText = editComment.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_EDIT_COMMENT,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

//                            Toast.makeText(EditCommentActivity.this,
//                                    jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditCommentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                /**
                 * 업데이트 할 댓글 내용과 댓글 아이디를 서버로 보낸다.
                 */

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_post_comment), updateCommentText);
                params.put(getString(R.string.db_post_comment_id), comment.getCommentId());

                return params;
            }
        };
        RequestHandler.getInstance(EditCommentActivity.this).addToRequestQueue(stringRequest);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
