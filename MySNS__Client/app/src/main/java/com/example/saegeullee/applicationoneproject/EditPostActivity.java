package com.example.saegeullee.applicationoneproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Models.Post;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {

    private static final String TAG = "EditPostActivity";

    private ImageView uploadPostImage;
    private EditText description, tag;
    private Button editPostBtn;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        initUI();
        getPostObject();

    }


    private void getPostObject() {

        if(getIntent().hasExtra(getString(R.string.intent_post_edit))) {

            post = getIntent().getParcelableExtra(getString(R.string.intent_post_edit));
            int position = getIntent().getIntExtra("position", 1);

            Log.d(TAG, "getPostObject: post: " + post.toString());
            Log.d(TAG, "getPostObject: position : " + position);
        }

        String image_url = Constants.ROOT_URL_POST_IMAGE + post.getPostId() + "/" + post.getPostImage();
        ImageLoader.getInstance().displayImage(image_url, uploadPostImage);

        description.setText(post.getDescription());

    }

    private void initUI() {

        uploadPostImage = findViewById(R.id.uploadPostImage);
        description = findViewById(R.id.descriptionET);
        tag = findViewById(R.id.tagET);
        editPostBtn = findViewById(R.id.editPostBtn);
        progressDialog = new ProgressDialog(this);

        toolbar = findViewById(R.id.edit_post_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("게시물 수정");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        editPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editPost();
            }
        });

    }

    private void editPost() {

        Log.d(TAG, "editPost: start");

        final String descriptionString = description.getText().toString().trim();
        String tagString = tag.getText().toString().trim();

        /**
         * 이미지와 내용 모두 입력해야 포스트 업로드를 진행한다.
         */

        if(!TextUtils.isEmpty(descriptionString)) {

            progressDialog.show();

            StringRequest stringRequest = new StringRequest(

                    Request.Method.POST,
                    Constants.URL_EDIT_POST,

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            progressDialog.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                Toast.makeText(EditPostActivity.this,
                                        jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(EditPostActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.hide();

                }
            }
            ) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<>();

                    params.put(getString(R.string.db_post_description), descriptionString);

                    /**
                     * 12/23 6:47am
                     * 포스트를 수정하려면 포스트의 id 를 서버로 보내야 한다.
                     *
                     */
                    params.put(getString(R.string.db_post_id), String.valueOf(post.getPostId()));
                    params.put(getString(R.string.db_post_description), descriptionString);

                    return params;
                }
            };

            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        } else {

            Toast.makeText(this, "문구를 입력하세요", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
