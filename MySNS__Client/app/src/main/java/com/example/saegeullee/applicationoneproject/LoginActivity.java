package com.example.saegeullee.applicationoneproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText idInput, passwordInput;
    private Button loginBtn;
    private TextView signUpText;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isUserLoggedIn();
        initUI();
    }

    private void isUserLoggedIn() {

        if(SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }

    private void initUI() {

        idInput = findViewById(R.id.id);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        signUpText = findViewById(R.id.signUp);

        progressDialog = new ProgressDialog(this);

        /*
         * 로그인
         */

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginUser();
            }
        });

        /*
         * 회원가입
         */

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));

            }
        });

        mAuth = FirebaseAuth.getInstance();


    }

    private void loginUser() {

        final String user_id = idInput.getText().toString().trim();
        final String password = passwordInput.getText().toString().trim();

        if (!TextUtils.isEmpty(user_id) && !TextUtils.isEmpty(password)) {

            StringRequest stringRequest = new StringRequest(

                    Request.Method.POST,
                    Constants.URL_LOGIN,

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                final JSONObject jsonObject = new JSONObject(response);

                                Toast.makeText(LoginActivity.this,
                                        jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                                if(jsonObject.getString("error").equals("false")) {

                                    Log.d(TAG, "onResponse: " + jsonObject.toString());

                                    SharedPrefManager.getInstance(LoginActivity.this).userLogIn(jsonObject);

                                    finish();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", user_id);
                    params.put("password", password);

                    return params;
                }
            };

            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        } else {

            Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
        }
    }
}
