package com.example.saegeullee.applicationoneproject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.ChatRoomAdapter;
import com.example.saegeullee.applicationoneproject.Adapter.ChatRoomMemberListAdapter;
import com.example.saegeullee.applicationoneproject.Models.ChatMessage;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.Service.ChatService;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;

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

/**
 * ChatRoomActivity
 * 서버와의 통신을 JSON 타입으로 변경
 */

public class ChatRoomActivity extends AppCompatActivity implements ChatService.OnMessageReceiveFromServerListener {

    private static final String TAG = "ChatRoomActivity";

    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    private OnMessageReceivedListener onMessageReceivedListener;

    public void setOnMessageReceivedListener(OnMessageReceivedListener onMessageReceivedListener) {
        this.onMessageReceivedListener = onMessageReceivedListener;
    }

    @Override
    public void onMessageReceivedFromServer(String message) {

        Log.d(TAG, "onMessageReceivedFromServer: msg : " + message);

        try {
            JSONObject object = new JSONObject(message);

            Log.d(TAG, "onMessageReceivedFromServer: object : " + object.toString());;

            String chatroom_id = object.getString("chatRoomId");

            if(chatroom_id.equals(chatRoomId)) {

                String sessionId = object.getString("sessionId");
                String userId = object.getString("name");
                String profile_image = object.getString("profile_image");
                String msg_type = object.getString("msg_type");
                String msg = object.getString("msg");
                String timestamp = object.getString("timestamp");

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(sessionId);
                chatMessage.setUser_id(userId);
                chatMessage.setMessage(msg);
                chatMessage.setDate(timestamp);
                chatMessage.setMsg_type(msg_type);
                chatMessage.setProfile_image(profile_image);

                if(msg_type.equals("invited")) {
                Log.d(TAG, "onMessageReceive: msg_type : invited");

                String arrayString = object.getString("msg");
                JSONArray array = new JSONArray(arrayString);

                List<User> userList = new ArrayList<>();

                for(int i = 0; i < array.length(); i++) {

                    JSONObject obj = array.getJSONObject(i);

                    User user = new User();

                    if(!obj.getString("id").equals(String.valueOf(SharedPrefManager.getInstance(ChatRoomActivity.this).getId()))) {
                        user.setId(Integer.parseInt(obj.getString("id")));
                        user.setUser_id(obj.getString("userId"));
                        userList.add(user);
                    }
                }

                Log.d(TAG, "onMessageReceive: userList size : " + userList.size());
            }

                messageList.add(chatMessage);
                recyclerView.getLayoutManager().scrollToPosition(adapter.getItemCount() - 1);
                adapter.notifyDataSetChanged();

            } else {

                Log.d(TAG, "onMessageReceivedFromServer: WRONG CHATROOM ID");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        }

    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private Button sendBtn;
    private ChatRoomAdapter adapter;

    private List<ChatMessage> messageList;

    private User user;

    private String chatRoomId;
    private String chatRoomMemberNumber;
    private String friend_id;

    public static final String SEPARATOR = "|";

    /**
     * 친구추가
     */

    private RecyclerView friendListRecyclerView;
    private ChatRoomMemberListAdapter chatRoomMemberListAdapter;
    private ArrayList<User> chatroomMemberList;
    // 친구 추가 액티비티에서 친구 추가 된 사용자를 저장할 리스트
    private ArrayList<User> addMemberList;
    public static final int REQUEST_CODE = 1;

    /**
     * 1) 채팅방 목록 액티비티에서 해당 채팅방을 클릭해서 채팅방에 들어온 경우와
     * 2) 친구 목록에서 채팅하기 버튼을 눌러서 들어온 경우를 나눠야 한다.
     * 의 여부를 나타내는 변수
     */

    private boolean isFromFriendList = false;
    private boolean isFromChatRoomList = false;
    // 현재 채팅방이 1대1 채팅방인지 단톡방인지 여부
    private boolean isOneToOneChatRoom;

    /**
     * 서비스
     */
    private ChatService chatService;
    private boolean isBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ChatService.MyBinder binder = (ChatService.MyBinder) service;
            chatService = binder.getService();
            chatService.setOnMessageReceiveFromServerListener(ChatRoomActivity.this);
            isBound = true;
            Log.d(TAG, "onServiceConnected: isBound : " + isBound);

            //서비스가 바운드 된 후 채팅룸을 가져온다.
            if(isFromFriendList) {
                Log.d(TAG, "onServiceConnected: isFromChatRoomList : " + isFromFriendList);
                getChatRoom();
            }
            if(isFromChatRoomList) {
                Log.d(TAG, "onServiceConnected: isFromChatRoomList : " + isFromChatRoomList);
                getChatRoom2();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //강제 종료와 같은 예기치 않은 종료에만 호출된다.

            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart: in");
        //서비스에 바인딩
        Intent intent = new Intent(this, ChatService.class);

        /**
         * 서비스에 연결하려면 서비스가 시작되어야 하는데 이 플래그(BIND_AUTO_CREATE)를 설정하면
         * 서비스를 따로 시작하지 않고도 바인드 서비스를 사용할 수 있다.
         * 바운딩 되면 서비스가 자동으로 시작된다.
         */

        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //서비스와 연결 해제
        if(isBound) {
            unbindService(serviceConnection);
            isBound = false;
            Log.d(TAG, "onStop: isBound : " + isBound);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        if(getIntent().hasExtra(getString(R.string.intent_chatroom_id))) {
            getChatRoomId();
            isFromChatRoomList = true;
        }

        if(getIntent().hasExtra(getString(R.string.db_users_object))) {
            getUserObject();
            isFromFriendList = true;
        }

        // 1대1 채팅방일 때 새로운 사용자를 추가했으므로 새로운 채팅방을 생성해야 한다.
        if(getIntent().hasExtra(getString(R.string.intent_chatroom_notice_create_new_chatroom))) {

            getUsersListForNewChatRoom();

        }

        initUI();
    }

    /**
     * 2019/2/11
     * 1) 채팅방 목록 액티비티에서 해당 채팅방을 클릭해서 채팅방에 들어온 경우와
     * 2) 친구 목록에서 채팅하기 버튼을 눌러서 들어온 경우를 나눠야 한다.
     */

    // 1) 채팅방 목록 액티비티에서 해당 채팅방을 클릭해서 채팅방에 들어온 경우
    private void getChatRoomId() {
        if(getIntent().hasExtra(getString(R.string.intent_chatroom_id))) {
            chatRoomId = getIntent().getStringExtra(getString(R.string.intent_chatroom_id));
            chatRoomMemberNumber = getIntent().getStringExtra(getString(R.string.intent_chatroom_size));
            friend_id = getIntent().getStringExtra(getString(R.string.intent_chatroom_friend_id));

            if(Integer.parseInt(chatRoomMemberNumber) > 2) {
                isOneToOneChatRoom = false;
            } else {
                isOneToOneChatRoom = true;
            }

            Log.d(TAG, "getChatRoomId: chatRoomId : " + chatRoomId);
            Log.d(TAG, "getChatRoomId: chatRoomMemberNumber : " + chatRoomMemberNumber);
        }
    }

    // 2) 친구 목록에서 채팅하기 버튼을 눌러서 들어온 경우
    private void getUserObject() {

        if(getIntent().hasExtra(getString(R.string.db_users_object))) {
            user = getIntent().getParcelableExtra(getString(R.string.db_users_object));
            Log.d(TAG, "getUserObject: user : " + user.toString());

            isOneToOneChatRoom = true;
        }
    }

    // 3) 1대 1 채팅방일때 새로운 사용자를 추가했을 경우
    private void getUsersListForNewChatRoom() {

        Log.d(TAG, "getUsersListForNewChatRoom: in");

        ArrayList<User> addUsers;
        addUsers = getIntent().getParcelableArrayListExtra(getString(R.string.chatroom_add_member_list));

        chatRoomMemberNumber = String.valueOf(addUsers.size());
        Log.d(TAG, "getUsersListForNewChatRoom: chatRoomMemberNumber : " + chatRoomMemberNumber);

        Log.d(TAG, "onActivityResult: addMemberList : " + addUsers.toString());

        isOneToOneChatRoom = false;

        createChatRoomForGroupChat(addUsers);
    }

    private void initUI() {

        toolbar = findViewById(R.id.chat_room_toolbar);
        setSupportActionBar(toolbar);
        if(isFromFriendList)
            getSupportActionBar().setTitle(user.getUser_id());
        if(isFromChatRoomList) {
            if(Integer.parseInt(chatRoomMemberNumber) > 1) {
                getSupportActionBar().setTitle("그룹채팅 " + (Integer.parseInt(chatRoomMemberNumber) + 1));
            } else {
                getSupportActionBar().setTitle(friend_id);
            }
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputMessage = findViewById(R.id.input_message);
        sendBtn = findViewById(R.id.send_button);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(ChatRoomActivity.this, 1));
        recyclerView.setNestedScrollingEnabled(false);

        messageList = new ArrayList<>();

        chatroomMemberList = new ArrayList<>();
        addMemberList = new ArrayList<>();

        adapter = new ChatRoomAdapter(this, messageList);

//        adapter = new ChatRoomTempAdapter(this, messageList);
        recyclerView.setAdapter(adapter);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (inputMessage.getText().toString().equals("")) {

                    Toast.makeText(ChatRoomActivity.this, "메시지를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {


                    Log.d(TAG, "onClick: onclick");

                    String id = String.valueOf(SharedPrefManager.getInstance(ChatRoomActivity.this).getId());
                    String user_id = SharedPrefManager.getInstance(ChatRoomActivity.this).getUserId();
                    String profile_image = SharedPrefManager.getInstance(ChatRoomActivity.this).getUserProfileImage();

                    JSONObject object = new JSONObject();

                    try {
                        object.put("flag", "msg");
                        object.put("chatRoomId", chatRoomId);
                        object.put("sessionId", id);
                        object.put("name", user_id);
                        object.put("profile_image", profile_image);
                        object.put("msg",inputMessage.getText().toString());
                        object.put("msg_type", "msg");
                        object.put("timestamp", getTimeStampForServer());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    chatService.receiveData(object.toString());
                    inputMessage.setText("");

                }
            }
        });
    }

    /**
     * isFromChatRoomList = true 일 때
     */
    private void getChatRoom2() {

        Log.d(TAG, "getChatRoom2: in");
        Log.d(TAG, "getChatRoom2: chatRoomId : " + chatRoomId);

        Log.d(TAG, "onResponse: chatService : " + chatService);

        JSONObject object = new JSONObject();

        try {

            object.put("flag", "room_number");
            object.put("chatRoomId", chatRoomId);
            object.put("sessionId", SharedPrefManager.getInstance(ChatRoomActivity.this).getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatService.receiveData(object.toString());

        initChatRoom();

    }

    /**
     * isFromFriendList = true 일 때
     */
    private void getChatRoom() {

        Log.d(TAG, "getChatRoom: in");

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_CHAT_ROOM,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.getString("error").equals("false")) {

                                if(jsonObject.getString("room_id").equals("no_room")) {

                                    /**
                                     * 채팅방이 없다면 채팅방을 생성한다.
                                     */
                                    createChatRoom();

                                } else {

                                    chatRoomId = jsonObject.getString("room_id");

                                    /**
                                     * 기존에 만들어진 채팅방이 있을 때 기존에 저장된 채팅 메세지를 읽어와야한다.
                                     */
                                    initChatRoom();
                                }
                            }

                            Log.d(TAG, "onResponse: jsonObject : " + jsonObject.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
            }
        }

        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();

                /**
                 * 현재 사용자와 해당 사용자와의 채팅 방을 가져온다.
                 */

                int current_user_id = SharedPrefManager.getInstance(ChatRoomActivity.this).getId();
                int other_user_id = user.getId();

                params.put(getString(R.string.db_users_my_id), String.valueOf(current_user_id));
                params.put(getString(R.string.db_users_friend_id), String.valueOf(other_user_id));

                return params;
            }
        };
        RequestHandler.getInstance(ChatRoomActivity.this).addToRequestQueue(stringRequest);

    }


    private void getAllChatRoomMembers() {

        chatroomMemberList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_CHAT_ROOM_MEBMER,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse: message : " + jsonObject.getString("message"));

                            JSONArray jsonArray = jsonObject.getJSONArray("chat_room_members");

                            User me = new User();
                            me.setId(SharedPrefManager.getInstance(ChatRoomActivity.this).getId());
                            me.setUser_id(SharedPrefManager.getInstance(ChatRoomActivity.this).getUserId());
                            me.setProfile_image(SharedPrefManager.getInstance(ChatRoomActivity.this).getUserProfileImage());

                            chatroomMemberList.add(me);

                            for(int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                Log.d(TAG, "onResponse: chat room member obj : " + obj.toString());

                                User user = new User();
                                user.setId(Integer.parseInt(obj.getString("id")));
                                user.setUser_id(obj.getString("user_id"));
                                user.setProfile_image(obj.getString("profile_image"));
                                user.setIs_friend(obj.getBoolean("isFriend"));

                                chatroomMemberList.add(user);
                            }
                            Log.d(TAG, "onResponse: chatroomMemberList : " + chatroomMemberList.size());

                            chatRoomMemberListAdapter.notifyDataSetChanged();

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

                /**
                 * 19/1/25 12:15pm
                 * 현재 사용자의 정보는 굳이 가져올 필요가 없기 때문에
                 * 현재 사용자의 id 를 같이 보내서 WHERE user_id != id QUERY 에 추가하자
                 */

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_chat_room_id), chatRoomId);
                params.put(getString(R.string.db_users_id), String.valueOf(SharedPrefManager.getInstance(ChatRoomActivity.this).getId()));

                return params;
            }
        };

        RequestHandler.getInstance(ChatRoomActivity.this).addToRequestQueue(stringRequest);
    }

    /**
     * 이 채팅방의 모든 메세지를 DB 에서 가져온다.
     */

    private void initChatRoom() {
        Log.d(TAG, "initChatRoom: in");

        messageList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_CHAT_ROOM_MESSAGES,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse: " + jsonObject.getString("message"));

                            JSONArray jsonArray = jsonObject.getJSONArray("chat_room_messages");

                            for(int i =0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);
                                Log.d(TAG, "onResponse: chat message object : " + object.toString());

                                ChatMessage message = new ChatMessage();
                                message.setId(object.getString("id"));
                                message.setUser_id(object.getString("user_id"));

                                if(object.getString("message_type").equals("invited")) {
                                    message.setMessage(object.getString("message"));
                                } else {
                                    message.setMessage(object.getString("message"));
                                }

                                message.setMsg_type(object.getString("message_type"));
                                message.setDate(object.getString("created_at"));
                                message.setProfile_image(object.getString("profile_image"));

                                messageList.add(message);
                            }

                            Log.d(TAG, "onResponse: message size : " + messageList.size());
                            recyclerView.getLayoutManager().scrollToPosition(adapter.getItemCount() - 1);
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: chat room message error : " + error.getMessage());
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                /**
                 * 해당 채팅방(ID)의 모든 메시지를 가져온다.
                 */

                Log.d(TAG, "getParams: chatRoomId : " + chatRoomId);

                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.db_chat_room_id), chatRoomId);

                return params;
            }
        };

        RequestHandler.getInstance(ChatRoomActivity.this).addToRequestQueue(stringRequest);
    }

    // 1대 1 채팅방에서 새로운 사용자를 초대한 경우 새로운 단톡방을 만든다.
    private void createChatRoomForGroupChat(final ArrayList<User> addUsersList) {

        Log.d(TAG, "createChatRoomForGroupChat: in");

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_CREATE_CHAT_ROOM_FOR_GROUP_CHAT,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.getString("error").equals("false")) {

                                chatRoomId = jsonObject.getString("room_id");

                                JSONArray array = new JSONArray();

                                for(int i = 0; i < addUsersList.size(); i++) {

                                    JSONObject object = new JSONObject();

                                    try {
                                        object.put("id", addUsersList.get(i).getId());
                                        object.put("userId", addUsersList.get(i).getUser_id());

                                        array.put(i, object);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }


                                toolbar = findViewById(R.id.chat_room_toolbar);
                                setSupportActionBar(toolbar);
                                getSupportActionBar().setTitle("그룹채팅" + (addUsersList.size() + 1));

                                JSONObject obj = new JSONObject();

                                try {
                                    //add_user_list 에 현재 사용자의 sessionId, userId 포함되어 있다.

                                    obj.put("flag", "create_group_chat");
                                    obj.put("sessionId", SharedPrefManager.getInstance(ChatRoomActivity.this).getId());
                                    obj.put("name", SharedPrefManager.getInstance(ChatRoomActivity.this).getUserId());
                                    obj.put("timestamp", getTimeStampForServer());
                                    obj.put("add_user_list", array);
                                    obj.put("chatRoomId", chatRoomId);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                chatService.receiveData(obj.toString());
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

                JSONArray array = new JSONArray();

                List<User> userList = new ArrayList<>(addUsersList);

                for(int i = 0; i < userList.size(); i++) {

                    JSONObject object = new JSONObject();

                    try {
                        object.put("id", userList.get(i).getId());
                        object.put("userId", userList.get(i).getUser_id());

                        array.put(i, object);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                JSONObject obj = new JSONObject();

                try {
                    obj.put("add_user_list", array);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                params.put(getString(R.string.db_create_group_chatroom), obj.toString());
                Log.d(TAG, "getParams: create group chat : " + obj.toString());

                return params;
            }
        };

        RequestHandler.getInstance(ChatRoomActivity.this).addToRequestQueue(stringRequest);
    }


    private void createChatRoom() {

        Log.d(TAG, "createChatRoom: in");

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_CREATE_CHAT_ROOM,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            Log.d(TAG, "onResponse: jsonObject : " + jsonObject.toString());

                            if(jsonObject.getString("error").equals("false")) {

                                chatRoomId = jsonObject.getString("room_id");
                                Log.d(TAG, "onResponse: createChatRoom : chatRoomId : " + chatRoomId);

                                /**
                                 * 방번호와 현재 사용자의 id 를 같이 보낸다.
                                 * 현재 접속중인 사용자의 id 만 서버의 globalMap 에 추가한다.
                                 */

                                /**
                                 * 2019/2/12 2:55am
                                 * 새로운 채팅방을 생성하거나 새로운 사용자를 채팅방에 추가할 때
                                 * 새로운 사용자가 현재 서버에 접속중이라면 새로운 사용자의 화면에서
                                 * 해당 채팅방이 실시간으로 새로 생성되어야 한다.
                                 * 이를 위해 user.getId() 를 통해 사용자의 id를 같이 서버로 보낸다.
                                 */

                                JSONObject object = new JSONObject();
                                object.put("flag", "new_chatroom");
                                object.put("chatRoomId", chatRoomId);
                                object.put("sessionId", SharedPrefManager.getInstance(ChatRoomActivity.this).getId());
                                object.put("friend_sessionId", user.getId());

                                chatService.receiveData(object.toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
            }
        }

        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();

                /**
                 * 현재 사용자와 해당 사용자와의 채팅 방을 생성
                 */

                int current_user_id = SharedPrefManager.getInstance(ChatRoomActivity.this).getId();
                int other_user_id = user.getId();

                params.put(getString(R.string.db_users_my_id), String.valueOf(current_user_id));
                params.put(getString(R.string.db_users_friend_id), String.valueOf(other_user_id));

                return params;
            }
        };
        RequestHandler.getInstance(ChatRoomActivity.this).addToRequestQueue(stringRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chatroom, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.addFriend:
                showCustomDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCustomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_friend_dialog);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        final Button addFriendBtn = dialog.findViewById(R.id.addFriendBtn);
        friendListRecyclerView = dialog.findViewById(R.id.recyclerView);
        friendListRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        getAllChatRoomMembers();
        /**
         * 1/25 2:33am
         * 임시로 이런 로직을 짜놓은 것 같은데
         * 처음에 서버에서 해당 방의 사용자를 받아와서 chatRoomMemberList 에 넣어줘야지
         */
//        chatroomMemberList.add(me);
//        chatroomMemberList.add(user);

        chatRoomMemberListAdapter = new ChatRoomMemberListAdapter(this, chatroomMemberList);
        friendListRecyclerView.setAdapter(chatRoomMemberListAdapter);

        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * 19/1/24 5:43pm
                 * 현재 이 방에 있는 사람들의 목록을 보내줘야 한다.
                 */
                Intent intent = new Intent(ChatRoomActivity.this, AddFriendInChatRoomActivity.class);
                intent.putExtra(getString(R.string.db_chat_room_id), chatRoomId);
                intent.putParcelableArrayListExtra(getString(R.string.chatroom_member_list), chatroomMemberList);

                // 1대1 채팅방이 아닌경우
                if (!isOneToOneChatRoom) {
                    startActivityForResult(intent, REQUEST_CODE);

                // 1대1 채팅인 경우는 사용자를 추가할 때 새로 채팅방을 생성해야한다.
                } else {
                    intent.putExtra(getString(R.string.intent_chatroom_is_one_to_one_chatroom), true);
                    startActivity(intent);
                    finish();
                }

                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE) {

            if(resultCode == Activity.RESULT_OK) {

                addMemberList = data.getParcelableArrayListExtra(getString(R.string.chatroom_add_member_list));
                Log.d(TAG, "onActivityResult: addMemberList : " + addMemberList.toString());


                chatRoomMemberNumber = String.valueOf(Integer.parseInt(chatRoomMemberNumber) + addMemberList.size());
                toolbar = findViewById(R.id.chat_room_toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setTitle("그룹채팅 " + chatRoomMemberNumber);

                isOneToOneChatRoom = false;

                /**
                 * 1/25 2:27am
                 * 아래의 코드 chatroomMemberList.addAll(addMemberList); 한줄로 끝남
                 */
//                for(int i = 0; i < addMemberList.size(); i++) {
//                    chatroomMemberList.add(addMemberList.get(i));
//
//                }

                /**
                 * chatroomMember 에 사용자를 추가하는 것은 getAllChatRoomMembers(); 에서 받아오니깐
                 * 아래에 서버에 추가한 사용자 목록을 알려줘서 이를 서버에서 DB에 저장하고 현재 채팅방에 추가한 사용자들을 넣어준다.
                 * 그니깐 chatroomMemberList.addAll(addMemberList); 를 여기서 할 필요 없다.
                 * 여기서는 UI 에 ------------- user1 님이 user 님을 초대했습니다 ----------- 를 더해주면 된다.
                 */
//                chatroomMemberList.addAll(addMemberList);

                /**
                 * 1. UI에 ------------- user1 님이 user 님을 초대했습니다 -----------
                 */


                /**
                 * 2. addMemberList 를 서버에 알려주고 해당 사용자들을 채팅방에 추가해야 한다.
                 */

                JSONObject object = new JSONObject();
                JSONArray array = new JSONArray();

                String current_id = String.valueOf(SharedPrefManager.getInstance(ChatRoomActivity.this).getId());
                String current_user_id = SharedPrefManager.getInstance(ChatRoomActivity.this).getUserId();

                try {

                    //누가 누구를 초대했는가 (방번호도 같이 보내야됨)

                    object.put("flag", "add_new_member");
                    object.put("chatRoomId", chatRoomId);
                    object.put("sessionId", current_id);
                    object.put("name", current_user_id);
                    object.put("timestamp", getTimeStampForServer());

                    for(int i = 0; i < addMemberList.size(); i++) {

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", addMemberList.get(i).getId());
                        jsonObject.put("userId", addMemberList.get(i).getUser_id());

                        array.put(i, jsonObject);
                    }

                    object.put("memberList", array);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                chatService.receiveData(object.toString());
            }
        }
    }


    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.KOREA);

        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(new Date());
    }

    private String getTimeStampForServer() {
        SimpleDateFormat sdfServer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);

        sdfServer.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdfServer.format(new Date());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

