package com.example.saegeullee.applicationoneproject;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {

    //widget
    private Toolbar mToolbar;
    private EditText userIdInput, passwordInput, passwordCheckInput, usernameInput, userEmailInput;
    private Button signUpBtn;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initUI();

    }

    private void initUI() {

        mToolbar = findViewById(R.id.signup_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.signUp));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userIdInput = findViewById(R.id.id);
        passwordInput = findViewById(R.id.password);
        passwordCheckInput = findViewById(R.id.passwordCheck);
        usernameInput = findViewById(R.id.name);
        userEmailInput = findViewById(R.id.email);

        progressDialog = new ProgressDialog(this);
        signUpBtn = findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpUser();
            }
        });

        mAuth = FirebaseAuth.getInstance();

    }

    private void signUpUser() {

        final String userId = userIdInput.getText().toString().trim();
        final String password = passwordInput.getText().toString().trim();
        final String passwordCheck = passwordCheckInput.getText().toString().trim();
        final String username = usernameInput.getText().toString().trim();
        final String userEmail = userEmailInput.getText().toString().trim();

        if (!TextUtils.isEmpty(userId) &&
                !TextUtils.isEmpty(password) &&
                !TextUtils.isEmpty(passwordCheck) &&
                !TextUtils.isEmpty(username) &&
                !TextUtils.isEmpty(userEmail)) {

            if(password.equals(passwordCheck)) {

                progressDialog.setMessage("회원가입중입니다..");
                progressDialog.show();

                StringRequest stringRequest = new StringRequest(

                        Request.Method.POST,
                        Constants.URL_REGISTER,

                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();

                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    Toast.makeText(SignUpActivity.this,
                                            jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                                    if(jsonObject.getString("error").equals("false")) {

                                        finish();

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Toast.makeText(SignUpActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String, String> params = new HashMap<>();
                        params.put("user_id", userId);
                        params.put("password", password);
                        params.put("user_name", username);
                        params.put("user_email", userEmail);

                        return params;
                    }
                };

                RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

            } else {

                Toast.makeText(this, "비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show();
            }

        } else {

            // 회원가입 항목 다 채웠는지
            Toast.makeText(this, "모든 항목을 채우세요.", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
