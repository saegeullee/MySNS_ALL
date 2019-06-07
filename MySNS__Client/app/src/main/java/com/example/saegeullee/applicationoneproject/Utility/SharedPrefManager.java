package com.example.saegeullee.applicationoneproject.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.saegeullee.applicationoneproject.Models.User;

import org.json.JSONObject;


public class SharedPrefManager {

    private static final String TAG = "SharedPrefManager";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private static final String SHARED_PREF_NAME = "mySharedPref";

    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PROFILE_IMAGE = "profile_image";
    private static final String KEY_TOKEN = "token";

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public void userLogIn(JSONObject jsonObject) {

        User user = new User(jsonObject, mCtx);

        Log.d(TAG, "userLogIn: " + user.toString());

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_USER_ID, user.getUser_id());
        editor.putString(KEY_USER_NAME, user.getUser_name());
        editor.putString(KEY_USER_EMAIL, user.getUser_email());
        editor.putString(KEY_USER_PROFILE_IMAGE, user.getProfile_image());

        editor.apply();
    }

    public void setToken(String token) {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if(sharedPreferences.getString(KEY_USER_ID, null) != null) {

            // 사용자 로그인 중
            return true;
        } else {
            return false;
        }
    }

    public void logOutUser() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }

    public void userUpdate(JSONObject jsonObject) {

        User user = new User(jsonObject, mCtx);

        Log.d(TAG, "userUpdate: " + user.toString());


        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_USER_ID, user.getUser_id());
        editor.putString(KEY_USER_NAME, user.getUser_name());
        editor.putString(KEY_USER_EMAIL, user.getUser_email());

        if(user.getProfile_image() != null) {
            editor.putString(KEY_USER_PROFILE_IMAGE, user.getProfile_image());
        }

        editor.apply();


    }

    public String getToken() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);

    }


    public int getId() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_ID, 0);

    }

    public String getUserId() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ID, null);

    }

    public String getUserName() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_NAME, null);

    }

    public String getUserEmail() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_EMAIL, null);

    }

    public String getUserProfileImage() {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_PROFILE_IMAGE, null);

    }

    public User getUserObject() {

        User user = new User();

        user.setId(mInstance.getId());
        user.setProfile_image(mInstance.getUserProfileImage());
        user.setUser_id(mInstance.getUserId());
        user.setUser_email(mInstance.getUserEmail());
        user.setUser_name(mInstance.getUserName());

        return user;

    }

}
