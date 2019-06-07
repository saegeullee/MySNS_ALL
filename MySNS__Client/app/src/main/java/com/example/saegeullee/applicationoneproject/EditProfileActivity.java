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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private CircleImageView profileImage;
    private EditText user_id, username, user_email;
    private Button saveProfileBtn;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    private Uri mProfileResultUri;
    private Bitmap bitmap;
    String profile_image_name;

    public static final int CROP_PROFILE_IMAGE_ACTIVITY_REQUEST_CODE = 1;

    // DB users 테이블의 id
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initUI();

        setDataToView();
    }

    private void setDataToView() {

        id = SharedPrefManager.getInstance(this).getId();

        user_id.setText(SharedPrefManager.getInstance(this).getUserId());
        username.setText(SharedPrefManager.getInstance(this).getUserName());
        user_email.setText(SharedPrefManager.getInstance(this).getUserEmail());

        if(SharedPrefManager.getInstance(this).getUserProfileImage() != null) {

            String profile_image_name = SharedPrefManager.getInstance(this).getUserProfileImage();
            String image_url = Constants.ROOT_URL_USER_PROFILE_IMAGE + profile_image_name;

            Log.d(TAG, "setDataToView: profile_image_name : " + profile_image_name);

            ImageLoader.getInstance().displayImage(image_url, profileImage);

        }

    }

    private void initUI() {

        profileImage = findViewById(R.id.profileImage);
        user_id = findViewById(R.id.user_name);
        username = findViewById(R.id.username);
        user_email = findViewById(R.id.user_email);
        progressDialog = new ProgressDialog(this);

        toolbar = findViewById(R.id.edit_profile_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("프로필 수정");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveProfileBtn = findViewById(R.id.save_profile_btn);

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateUserInfo();

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent imageIntent = CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1, 1)
                        .setBorderLineColor(Color.RED)
                        .setGuidelinesColor(Color.GREEN)
                        .setBorderLineThickness(getResources().getDimensionPixelSize(R.dimen.thickness))
                        .getIntent(EditProfileActivity.this);
                startActivityForResult(imageIntent, CROP_PROFILE_IMAGE_ACTIVITY_REQUEST_CODE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CROP_PROFILE_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfileResultUri = result.getUri();
                profileImage.setImageURI(mProfileResultUri);

                /**
                 * 크롭퍼로 따온 이미지를 비트맵으로 변환
                 */

                try {

                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mProfileResultUri);
                    File f = new File(String.valueOf(mProfileResultUri));
                    profile_image_name = f.getName();
                    Log.d(TAG, "onActivityResult: image name : " + profile_image_name);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void updateUserInfo() {

        final String new_user_id = user_id.getText().toString().trim();
        final String new_user_name = username.getText().toString().trim();
        final String new_user_email = user_email.getText().toString().trim();

        if(!TextUtils.isEmpty(new_user_id) &&
                !TextUtils.isEmpty(new_user_name)&&
                !TextUtils.isEmpty(new_user_email)) {

            progressDialog.setMessage("프로필 업데이트 중입니다.");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(

                    Request.Method.POST,
                    Constants.URL_UPDATE_USER,

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            progressDialog.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                Log.d(TAG, "onResponse: " + jsonObject.toString());


                                /**
                                 * LoginActivity 에서와 마찬가지로 사용자가 프로필 정보를 업데이트 하면
                                 * 업데이트 된 사용자에 대한 모든 정보를 여기서 받아서 다시 SharedPrefManager
                                 * 에서도 업데이트를 해야한다.
                                 */

                                SharedPrefManager.getInstance(EditProfileActivity.this).userUpdate(jsonObject);

                                Toast.makeText(EditProfileActivity.this,
                                        jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    progressDialog.dismiss();

                }
            }
            ) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<>();

                    params.put(getString(R.string.db_users_id), String.valueOf(id));
                    params.put(getString(R.string.db_users_user_id), new_user_id);
                    params.put(getString(R.string.db_users_user_name), new_user_name);
                    params.put(getString(R.string.db_users_user_email), new_user_email);

                    /**
                     * 서버로 이미지 비트맵을 스트링으로 변환한 값과 안드로이드에서
                     * 딴 이미지의 이름을 같이 서버로 보내서 이 이름을 서버에 저장되는 이미지 이름으로 저장한다.
                     */

                    if(bitmap != null) {
                        params.put(getString(R.string.db_users_user_profile_image), imageToString(bitmap));
                        params.put(getString(R.string.db_users_user_profile_image_name), profile_image_name);

                    }

                    Log.d(TAG, "getParams: params :" + params.toString());

                    return params;
                }
            };

            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        } else {
            Toast.makeText(this, "모든 항목을 다 채우세요", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     *
     * @param bitmap
     * @return
     * 비트맵을 스트링으로 변환
     *
     */
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
