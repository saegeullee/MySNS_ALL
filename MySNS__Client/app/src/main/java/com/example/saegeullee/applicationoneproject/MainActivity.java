package com.example.saegeullee.applicationoneproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Fragment.Chat2Fragment;
import com.example.saegeullee.applicationoneproject.Fragment.FeedFragment;
import com.example.saegeullee.applicationoneproject.Fragment.FriendsFragment;
import com.example.saegeullee.applicationoneproject.Fragment.MoreFragment;
import com.example.saegeullee.applicationoneproject.Fragment.NewsFragment;
import com.example.saegeullee.applicationoneproject.Service.ChatService;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.example.saegeullee.applicationoneproject.Utility.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ChatService.OnMessageReceiveFromServerListener{

    private static final String TAG = "MainActivity";

    public interface OnChatServiceBoundSuccessListener {
        void onChatServiceBounded(ChatService chatService);
    }

    private OnChatServiceBoundSuccessListener onChatServiceBoundSuccessListener;

    public void setOnChatServiceBoundSuccessListener(OnChatServiceBoundSuccessListener onChatServiceBoundSuccessListener) {
        this.onChatServiceBoundSuccessListener = onChatServiceBoundSuccessListener;
    }

    public interface OnMessageReceivedListener {
        void onMessageReceive(String msg);
    }

    private OnMessageReceivedListener onMessageReceivedListener;

    public void setOnMessageReceivedListener(OnMessageReceivedListener onMessageReceivedListener) {
        this.onMessageReceivedListener = onMessageReceivedListener;
    }

    @Override
    public void onMessageReceivedFromServer(String msg) {

        if(onMessageReceivedListener != null)
            onMessageReceivedListener.onMessageReceive(msg);
    }

    /**
     * 서비스
     */

    private ChatService chatService;
    private boolean isBound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ChatService.MyBinder binder = (ChatService.MyBinder) service;
            chatService = binder.getService();
            chatService.setOnMessageReceiveFromServerListener(MainActivity.this);
            isBound = true;
            Log.d(TAG, "onServiceConnected: isBound : " + isBound);

            initTCPServer();

            if(onChatServiceBoundSuccessListener != null)
                onChatServiceBoundSuccessListener.onChatServiceBounded(chatService);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //강제 종료와 같은 예기치 않은 종료에만 호출된다.

            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    /**
     * 19/2/1 11:36pm 문제점 발견&해결
     * 문제점 : 하나의 서비스 객체를 MainActivity 와 ChatRoomActivity  두 개의 액티비티에서 각각 바운드시켜
     * 사용하려는데 onStop 또는 onDestroy 에서 계속 언바운드를 시켜서 액티비티가 바뀔 때마다 새로운 소켓이 생성되어
     * 서버와 연결되었다.
     * 문제점은 이게  onStop 또는 onDestroy 에서 계속 언바운드를 시켜서 새로운 소켓이 연결되고 있는 것인지도 몰랐다는 것이다.
     *
     * 해결 : 언바인드를 하지 않고 한번 바인드 되었던 서비스를 계속 유지시킨다.
     */

    @Override
    protected void onStop() {
        super.onStop();
        if(isBound) {
            unbindService(serviceConnection);
            isBound = false;
            Log.d(TAG, "onStop: isBound : " + isBound);
        }
    }


    private Toolbar toolbar;
    private ActionBar actionBar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private PageFragmentAdapter fragmentAdpater;

    private FriendsFragment fragment_friends;
    private Chat2Fragment fragment_chat2;
    private NewsFragment fragment_news;
    private FeedFragment fragment_feed;
    private MoreFragment fragment_more;

    private FirebaseAuth mAuth;

    public static final String CHANNEL_ID = "ApplicationOneProject CHANNEL ID";
    private static final String CHANNEL_NAME = "ApplicationOneProject CHANNEL NAME";
    private static final String CHANNEL_DESC = "ApplicationOneProject CHANNEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initImageLoader();
        isUserLoggedIn();
        initNotificationChannel();
        initUI();
    }

    private void initNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // 현재 사용자가 접속했음을 TCP 서버에 알린다.
    private void initTCPServer() {

        JSONObject object = new JSONObject();
        try {
            object.put("flag", "init");
            object.put("sessionId", SharedPrefManager.getInstance(MainActivity.this).getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatService.receiveData(object.toString());

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart: in : bindService");
        //서비스에 바인딩
        Intent intent = new Intent(this, ChatService.class);

        /**
         * 서비스에 연결하려면 서비스가 시작되어야 하는데 이 플래그(BIND_AUTO_CREATE)를 설정하면
         * 서비스를 따로 시작하지 않고도 바인드 서비스를 사용할 수 있다.
         * 바운딩 되면 서비스가 자동으로 시작된다.
         */
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

    }

    private void isUserLoggedIn() {

        if(!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    private void initUI() {

        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("친구");
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        setupTabClick();

        mAuth = FirebaseAuth.getInstance();
        
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                if(task.isSuccessful()) {
//                    String token = task.getResult().getToken();
//
//                    Log.d(TAG, "onComplete: token : " + token);
//                    if(SharedPrefManager.getInstance(MainActivity.this).getToken() == null)
//                        saveToken(token);
//                }
//            }
//        });

//        tokenTest();
    }

    private void tokenTest() {

        if(SharedPrefManager.getInstance(MainActivity.this).getToken() != null) {

            Log.d(TAG, "tokenTest: token not null");

            StringRequest stringRequest = new StringRequest(

                    Request.Method.POST,
                    Constants.URL_PUSH_NOTIFICATIONS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                Toast.makeText(MainActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onResponse: result " + jsonObject.getString("result"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: error : " + error.getMessage());
                }
            }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<>();
                    params.put("post", "post");

                    return params;
                }
            };
            RequestHandler.getInstance(MainActivity.this).addToRequestQueue(stringRequest);

        }

    }

    private void saveToken(final String token) {

        SharedPrefManager.getInstance(MainActivity.this).setToken(token);

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_INSERT_USER_TOKEN,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.getString("error").equals("false")) {

                                Log.d(TAG, "onResponse: insert token response : " + jsonObject.getString("message"));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: error : " + error.getMessage());
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();

                params.put(getString(R.string.db_users_id), String.valueOf(SharedPrefManager.getInstance(MainActivity.this).getId()));
                params.put(getString(R.string.db_users_token), token);

                return params;
            }
        };

        RequestHandler.getInstance(MainActivity.this).addToRequestQueue(stringRequest);

    }

    public void setupViewPager(ViewPager viewPager) {

        fragmentAdpater = new PageFragmentAdapter(getSupportFragmentManager());

        if(fragment_friends == null) { fragment_friends = new FriendsFragment(); }
        if(fragment_chat2 == null) { fragment_chat2 = new Chat2Fragment(); }
        if(fragment_news == null) { fragment_news = new NewsFragment(); }
        if(fragment_feed == null) { fragment_feed = new FeedFragment(); }
        if(fragment_more == null) { fragment_more = new MoreFragment(); }

        fragmentAdpater.addFragment(fragment_friends, getString(R.string.friends));
        fragmentAdpater.addFragment(fragment_chat2, getString(R.string.chat));
        fragmentAdpater.addFragment(fragment_news, getString(R.string.news));
        fragmentAdpater.addFragment(fragment_feed, getString(R.string.feed));
        fragmentAdpater.addFragment(fragment_more, getString(R.string.more));

        viewPager.setAdapter(fragmentAdpater);
    }

    private void setupTabIcons() {

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_person);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_chat);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_news);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_home);
        tabLayout.getTabAt(4).setIcon(R.drawable.ic_more);
    }

    private void setupTabClick() {
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
                actionBar.setTitle(fragmentAdpater.getTitle(position));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * init universal image loader
     */

    private void initImageLoader() {
        UniversalImageLoader imageLoader = new UniversalImageLoader(MainActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

}
