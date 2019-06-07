package com.example.saegeullee.applicationoneproject;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.ChatRoomListAdapter;
import com.example.saegeullee.applicationoneproject.Models.ChatMessage;
import com.example.saegeullee.applicationoneproject.Models.ChatRoom;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.Service.ChatService;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;
import com.example.saegeullee.applicationoneproject.Utility.TimeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ChatRoomListActivity extends AppCompatActivity implements
        ChatService.OnMessageReceiveFromServerListener{
    

    private static final String TAG = "ChatRoomListActivity";

    @Override
    public void onMessageReceivedFromServer(String message) {

        Log.d(TAG, "onMessageReceivedFromServer: msg : " + message);

        StringTokenizer st = new StringTokenizer(message, SEPARATOR);

        final String room_id = st.nextToken();
        final String id = st.nextToken();
        final String user_id = st.nextToken();
        final String msg_type = st.nextToken();
        final String msg = st.nextToken();
        final String timestamp = st.nextToken();

        Log.d(TAG, "run: room_id : " + room_id + " user_id : " + user_id + " msg : " + msg);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(id);
        chatMessage.setUser_id(user_id);
        chatMessage.setMessage(msg);
        chatMessage.setDate(timestamp);
        chatMessage.setMsg_type(msg_type);


//        messageList.add(chatMessage);
//        recyclerView.getLayoutManager().scrollToPosition(adapter.getItemCount() - 1);
//        adapter.notifyDataSetChanged();

    }

    private RecyclerView recyclerView;
    private ChatRoomListAdapter adapter;
    private List<ChatRoom> chatRoomList;


    public static final String SEPARATOR = "|";

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
            chatService.setOnMessageReceiveFromServerListener(ChatRoomListActivity.this);
            isBound = true;
            Log.d(TAG, "onServiceConnected: isBound : " + isBound);

            getChatRoomList();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //강제 종료와 같은 예기치 않은 종료에만 호출된다.
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //서비스와 연결 해제
        if(isBound) {
            unbindService(serviceConnection);
            isBound = false;
            Log.d(TAG, "onDestroy: isBound : " + isBound);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        //서비스에 바인딩
        Intent intent = new Intent(this, ChatService.class);

        /**
         * 서비스에 연결하려면 서비스가 시작되어야 하는데 이 플래그(BIND_AUTO_CREATE)를 설정하면
         * 서비스를 따로 시작하지 않고도 바인드 서비스를 사용할 수 있다.
         * 바운딩 되면 서비스가 자동으로 시작된다.
         */
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        initUI();
        setDataToView();

    }


    private void setDataToView() {

        adapter = new ChatRoomListAdapter(ChatRoomListActivity.this, chatRoomList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ChatRoomListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                User user = chatRoomList.get(position).getUserList().get(0);
                Intent intent = new Intent(ChatRoomListActivity.this, ChatRoomActivity.class);
                intent.putExtra(getString(R.string.db_users_object), user);
                startActivity(intent);
            }
        });
    }

    private void initUI() {

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(ChatRoomListActivity.this, 1));
        recyclerView.setNestedScrollingEnabled(false);

        chatRoomList = new ArrayList<>();
    }

    private void getChatRoomList() {

        chatRoomList.clear();

        StringRequest stringRequest = new StringRequest(

                Request.Method.POST,
                Constants.URL_GET_CHAT_ROOM_LIST,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.getString("error").equals("false")) {

                                Log.d(TAG, "onResponse: message : " + jsonObject.getString("message"));

                                JSONArray jsonArray = jsonObject.getJSONArray("chat_room_list");

                                for(int i = 0; i < jsonArray.length(); i++) {

                                    JSONArray array = jsonArray.getJSONArray(i);
                                    Log.d(TAG, "onResponse: array : " + array);

                                    for(int j = 0; j < array.length(); j++) {

                                        JSONObject obj = array.getJSONObject(j);
                                        Log.d(TAG, "onResponse: obj : " + obj.toString());

                                        JSONObject messageObj = obj.getJSONObject("last_message");
                                        Log.d(TAG, "onResponse: messageObj : " + messageObj.toString());

                                        ChatRoom chatRoom = new ChatRoom();
                                        User user = new User();
                                        user.setId(Integer.parseInt(obj.getString("id")));
                                        user.setUser_id(obj.getString("user_id"));
                                        user.setProfile_image(obj.getString("profile_image"));

                                        List<User> userList = new ArrayList<>();
                                        userList.add(user);

                                        chatRoom.setRoom_id(obj.getString("room_id"));
                                        chatRoom.setUserList(userList);

                                        if(!messageObj.getString("message").equals("null")) {
                                            chatRoom.setLastMessage(messageObj.getString("message"));
                                        }

                                        if(!messageObj.getString("created_at").equals("null")) {
                                            chatRoom.setTimestamp(TimeManager.compareTime(messageObj.getString("created_at")));
                                        }

                                        chatRoomList.add(chatRoom);
                                    }
                                }
                                Log.d(TAG, "onResponse: chatRoomList size : " + chatRoomList.size());
                                adapter.notifyDataSetChanged();

                                /**
                                 * 현재 사용자가 접속하고 있는 모든 채팅방 id를 서버로 보내서 해당 채팅방에
                                 * 새로 입력되는 메시지가 실시간으로 현재 사용자에게 보여지도록 한다.
                                 */

                                StringBuffer chatRoomListString = new StringBuffer(500);
                                chatRoomListString.setLength(0);

                                for(int i =0; i < chatRoomList.size(); i++) {

                                    String chatRoomId =chatRoomList.get(i).getRoom_id();
                                    Log.d(TAG, "onResponse: chatRoomIds in ChatRoomList : " + chatRoomId);
                                    chatRoomListString.append(chatRoomId);
                                    chatRoomListString.append(SEPARATOR);
                                }

                                Log.d(TAG, "onResponse: chatRoomListString : " + chatRoomListString.toString());

                                String msg = "/모든채팅방|" +
                                        SharedPrefManager.getInstance(ChatRoomListActivity.this).getId() + "|" +
                                        chatRoomListString;

                                chatService.receiveData(msg);

//                                Message message = mServiceHandler.obtainMessage();
//                                message.what = MSG_START;
//                                message.obj = "/모든채팅방|" +
//                                        SharedPrefManager.getInstance(ChatRoomListActivity.this).getId() + "|" +
//                                        chatRoomListString;
//
//                                mServiceHandler.sendMessage(message);

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

                /**
                 * 현재 사용자가 속해 있는 모든 채팅방 목록을 불러온다.
                 */

                params.put(getString(R.string.db_users_id),
                        String.valueOf(SharedPrefManager.getInstance(ChatRoomListActivity.this).getId()));

                return params;
            }
        };
        RequestHandler.getInstance(ChatRoomListActivity.this).addToRequestQueue(stringRequest);

    }

}
