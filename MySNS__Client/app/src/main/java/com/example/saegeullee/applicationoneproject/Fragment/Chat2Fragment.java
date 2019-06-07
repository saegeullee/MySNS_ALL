package com.example.saegeullee.applicationoneproject.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.ChatRoomListAdapter;
import com.example.saegeullee.applicationoneproject.ChatRoomActivity;
import com.example.saegeullee.applicationoneproject.Constants;
import com.example.saegeullee.applicationoneproject.MainActivity;
import com.example.saegeullee.applicationoneproject.Models.ChatRoom;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.R;
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


/**
 * Chat2Fragment
 * 서버와의 통신을 JSON 타입으로 변경
 */

public class Chat2Fragment extends Fragment implements
        MainActivity.OnChatServiceBoundSuccessListener,
        MainActivity.OnMessageReceivedListener {

    private static final String TAG = "Chat2Fragment";

    @Override
    public void onChatServiceBounded(ChatService chatService) {

        Log.d(TAG, "onChatServiceBounded: ");
        service = chatService;
//        if(service != null)
//            chatRoomList = getChatRoomList();

    }

    @Override
    public void onMessageReceive(String message) {

        Log.d(TAG, "onMessageReceive: msg : " + message);

//        {"msg":"[{\"id\":7,\"userId\":\"user3\"},{\"id\":6,\"userId\":\"user2\"},{\"id\":5,\"userId\":\"helloworld\"}]",
// "profile_image":"","chatRoomId":"73","name":"user1","msg_type":"invited","sessionId":"4","timestamp":"2019-02-15 01:30:56"}

        String chatroom_id = "";
        String sessionId = "";
        String userId = "";
        String profile_image = "";
        String msg_type = "";
        String msg = "";
        String timestamp = "";

        List<User> userList = new ArrayList<>();

        try {
            JSONObject object = new JSONObject(message);

            Log.d(TAG, "onMessageReceivedFromServer: object : " + object);

            chatroom_id = object.getString("chatRoomId");
            sessionId = object.getString("sessionId");
            userId = object.getString("name");
            profile_image = object.getString("profile_image");
            msg_type = object.getString("msg_type");
            timestamp = object.getString("timestamp");

            /**
             * 채팅방에 새로운 메시지가 오면 해당 채팅방을 목록의 제일 위로 보낸다
             */


            if(msg_type.equals("msg")) {
                Log.d(TAG, "onMessageReceive: msg_type : msg");
                msg = object.getString("msg");

                /**
                 * 2/16 4:53 am 에러발생
                 * 처음에 다른 사용자가 1대1 채팅방을 만들고 메시지를 보냈을 때
                 * 다른 사용자 목록을 userList 에 추가하지 않아서 에러가 발생하였다.
                 */

                User user = new User();
                user.setId(Integer.parseInt(sessionId));
                user.setUser_id(userId);
                user.setProfile_image(profile_image);

                userList.add(user);

            } else if(msg_type.equals("invited")) {
                Log.d(TAG, "onMessageReceive: msg_type : invited");

                /**
                 * 2019/2/15 1:38am 에러발생
                 * JSONArray 획득 실패
                 * Value [{"id":7,"userId":"user3"},{"id":6,"userId":"user2"},{"id":5,"userId":"helloworld"}]
                 * at msg of type java.lang.String cannot be converted to JSONArray
                 * Array 자체가 "[]" 모양이라 이건 JSONArray 가 아니라 String 이다.
                 * 따라서 String 을 먼저 얻고 그다음에 JSONArray 로 변환한다.
                 */
                String arrayString = object.getString("msg");
                JSONArray array = new JSONArray(arrayString);

                for(int i = 0; i < array.length(); i++) {

                    JSONObject obj = array.getJSONObject(i);

                    User user = new User();

                    if(!obj.getString("id").equals(String.valueOf(SharedPrefManager.getInstance(getActivity()).getId()))) {
                        user.setId(Integer.parseInt(obj.getString("id")));
                        user.setUser_id(obj.getString("userId"));
                        userList.add(user);
                    }
                }

                Log.d(TAG, "onMessageReceive: userList size : " + userList.size());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean isRoomExists = false;
        for(int i = 0; i < chatRoomList.size(); i++) {
            if(chatRoomList.get(i).getRoom_id().equals(chatroom_id)) {
                isRoomExists = true;

                if(!msg_type.equals("invited")) {

                    chatRoomList.get(i).setLastMessage(msg);
                    chatRoomList.get(i).setTimestamp(TimeManager.compareTime(timestamp));
                    chatRoomList.get(i).setTime_update(timestamp);

                } else {

                    //기존에 채팅방이 있고 새로운 사용자가 추가된 경우에는
                    //기존의 채팅방 사용자 목록에 추가된 사용자 목록을 더해줘야 한다.

                    List<User> tempList;
                    tempList = chatRoomList.get(i).getUserList();
                    tempList.addAll(userList);

                    chatRoomList.get(i).setUserList(tempList);
                    chatRoomList.get(i).setTime_update(timestamp);

                }
                break;
            }
        }

        if(!isRoomExists) {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoom_id(chatroom_id);

            if(!msg_type.equals("invited")) {

                /**
                 * 2/16 4:53 am 에러발생
                 * 처음에 다른 사용자가 1대1 채팅방을 만들고 메시지를 보냈을 때
                 * 다른 사용자 목록을 userList 에 추가하지 않아서 에러가 발생하였다.
                 */

                chatRoom.setLastMessage(msg);
                chatRoom.setTimestamp(TimeManager.compareTime(timestamp));
                chatRoom.setUserList(userList);
                chatRoom.setTime_update(timestamp);

            } else {

                chatRoom.setUserList(userList);
                chatRoom.setTimestamp(TimeManager.compareTime(timestamp));
                chatRoom.setTime_update(timestamp);

            }

            chatRoomList.add(chatRoom);
        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((MainActivity)getActivity()).setOnChatServiceBoundSuccessListener(this);
        ((MainActivity)getActivity()).setOnMessageReceivedListener(this);
    }

    private RecyclerView recyclerView;
    private ChatRoomListAdapter adapter;
    private List<ChatRoom> chatRoomList;
    public static final String SEPARATOR = "|";
    private View view;
    private ChatService service;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: in");

        view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.chat_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setNestedScrollingEnabled(false);

        chatRoomList = new ArrayList<>();

//        initChatRoomListTimeUpdater();

        setHasOptionsMenu(true);
        setDataToView();

        return view;
    }

    private void initChatRoomListTimeUpdater() {


        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "run: Thread run");

                    if (chatRoomList != null) {
                        Log.d(TAG, "run: chatRoomList not null");

                        for (int i = 0; i < chatRoomList.size(); i++) {
                            Log.d(TAG, "run: getTimeUpdate : " + chatRoomList.get(i).getTime_update());
                            if (chatRoomList.get(i) != null) {
                                chatRoomList.get(i).setTimestamp(TimeManager.compareTime(chatRoomList.get(i).getTime_update()));
                            }
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, " runOnUiThread run: ");
                                adapter.notifyDataSetChanged();

                            }
                        });
                    }
                }
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: chatRoomList : " + chatRoomList);
        Log.d(TAG, "onStart: before chatRoomList size : " + chatRoomList.size());

//        if(service != null)
            chatRoomList = getChatRoomList();

        Log.d(TAG, "onStart: after chatRoomList size : " + chatRoomList.size());

    }

    private void setDataToView() {

        adapter = new ChatRoomListAdapter(getActivity(), chatRoomList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ChatRoomListAdapter.OnItemClickListener() {

            @Override
            public void onItemClicked(int position) {

                String chatRoomId = chatRoomList.get(position).getRoom_id();
                String chatRoomMemberNumber = String.valueOf(chatRoomList.get(position).getUserList().size());
                String friend_id = chatRoomList.get(position).getUserList().get(0).getUser_id();

                Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                intent.putExtra(getString(R.string.intent_chatroom_id), chatRoomId);
                intent.putExtra(getString(R.string.intent_chatroom_size), chatRoomMemberNumber);
                intent.putExtra(getString(R.string.intent_chatroom_friend_id), friend_id);

                startActivity(intent);
            }
        });
    }

    private List<ChatRoom> getChatRoomList() {

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

                                    JSONObject chatRoomObj = jsonArray.getJSONObject(i);

                                    ChatRoom chatRoom = new ChatRoom();
                                    chatRoom.setRoom_id(chatRoomObj.getString("room_id"));

                                    JSONArray array = chatRoomObj.getJSONArray("room_user_info");
                                    Log.d(TAG, "onResponse: array : " + array);

                                    List<User> userList = new ArrayList<>();

                                    for(int j = 0; j < array.length(); j++) {

                                        JSONObject obj = array.getJSONObject(j);
                                        Log.d(TAG, "onResponse: obj : " + obj.toString());

                                        JSONObject messageObj = obj.getJSONObject("last_message");
                                        Log.d(TAG, "onResponse: messageObj : " + messageObj.toString());

                                        User user = new User();
                                        user.setId(Integer.parseInt(obj.getString("id")));
                                        user.setUser_id(obj.getString("user_id"));
                                        user.setProfile_image(obj.getString("profile_image"));

                                        userList.add(user);

                                        chatRoom.setUserList(userList);

                                        if(!messageObj.getString("message").equals("null")) {
                                            chatRoom.setLastMessage(messageObj.getString("message"));
                                        }

                                        if(!messageObj.getString("created_at").equals("null")) {
                                            chatRoom.setTimestamp(TimeManager.compareTime(messageObj.getString("created_at")));
                                            chatRoom.setTime_update(messageObj.getString("created_at"));
                                        }
                                    }
                                    Log.d(TAG, "onResponse: userList size : " + userList.size());
                                    chatRoomList.add(chatRoom);
                                }
                                Log.d(TAG, "onResponse: chatRoomList size : " + chatRoomList.size());
                                adapter.notifyDataSetChanged();

                                /**
                                 * 현재 사용자가 접속하고 있는 모든 채팅방 id를 서버로 보내서 해당 채팅방에
                                 * 새로 입력되는 메시지가 실시간으로 현재 사용자에게 보여지도록 한다.
                                 */

                                JSONObject object = new JSONObject();
                                object.put("flag", "all_chatroom");
                                object.put("sessionId", SharedPrefManager.getInstance(getActivity()).getId());
                                object.put("name", SharedPrefManager.getInstance(getActivity()).getUserId());

                                JSONArray array = new JSONArray();
                                for(int i = 0; i < chatRoomList.size(); i++) {

                                    String chatRoomId = chatRoomList.get(i).getRoom_id();
                                    array.put(i, chatRoomId);
                                }

                                object.put("chatroom_id_list", array);

                                service.receiveData(object.toString());

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
                        String.valueOf(SharedPrefManager.getInstance(getActivity()).getId()));

                return params;
            }
        };
        RequestHandler.getInstance(getActivity()).addToRequestQueue(stringRequest);
        return chatRoomList;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_fragment_friends, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Snackbar.make(view, item.getTitle() + "Clicked", Snackbar.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }

}
