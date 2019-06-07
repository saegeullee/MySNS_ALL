package com.example.saegeullee.applicationoneproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private static final String TAG = "AddPostActivity";

    private ImageView uploadPostImage;
    private EditText description;
    private Button addPostBtn;
    private Toolbar toolbar;

    private Bitmap bitmap;
    private String post_image_name;
    private ProgressDialog progressDialog;

    private static final int CROP_POST_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private Uri postImageResultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        initUI();
    }

    private void initUI() {

        uploadPostImage = findViewById(R.id.uploadPostImage);
        description = findViewById(R.id.descriptionET);
        addPostBtn = findViewById(R.id.addPostBtn);
        progressDialog = new ProgressDialog(this);

        toolbar = findViewById(R.id.add_post_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("게시물 추가");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uploadPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = CropImage.activity()
                        .setAspectRatio(3, 2)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .getIntent(AddPostActivity.this);
                startActivityForResult(intent, CROP_POST_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadPost();
            }
        });

    }

    private void uploadPost() {

        Log.d(TAG, "uploadPost: start");

        final String descriptionString = description.getText().toString().trim();

        /**
         * 이미지와 내용 모두 입력해야 포스트 업로드를 진행한다.
         */

        if(!TextUtils.isEmpty(descriptionString) && postImageResultUri != null) {

            progressDialog.setTitle("게시물 업로드중");
            progressDialog.setMessage("업로드 중입니다..");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(

                    Request.Method.POST,
                    Constants.URL_ADD_POST,

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            progressDialog.hide();

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                Toast.makeText(AddPostActivity.this,
                                        jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    progressDialog.hide();
                    Toast.makeText(AddPostActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            ) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<>();
                    int user_id = SharedPrefManager.getInstance(AddPostActivity.this).getId();

                    params.put(getString(R.string.db_post_description), descriptionString);
                    params.put(getString(R.string.db_users_id), String.valueOf(user_id));

                    if(bitmap != null) {
                        params.put(getString(R.string.db_post_image), imageToString(bitmap));
                        params.put(getString(R.string.db_post_image_name), post_image_name);
                    }

                    return params;
                }
            };

            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        } else {

            Toast.makeText(this, "이미지를 업로드하세요.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CROP_POST_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageResultUri = result.getUri();
                uploadPostImage.setImageURI(postImageResultUri);

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), postImageResultUri);
                    File f = new File(String.valueOf(postImageResultUri));
                    post_image_name = f.getName();
                    Log.d(TAG, "onActivityResult: post image name : " + post_image_name);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private String imageToString(Bitmap bitmap) {

        Log.d(TAG, "imageToString: in");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();

        String result = Base64.encodeToString(imgBytes, Base64.DEFAULT);

        Log.d(TAG, "imageToString: result " + result);

        return result;

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
