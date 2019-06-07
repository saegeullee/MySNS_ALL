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
import java.util.StringTokenizer;

public class ChatFragment extends Fragment implements
        MainActivity.OnChatServiceBoundSuccessListener,
        MainActivity.OnMessageReceivedListener {

    private static final String TAG = "ChatFragment";

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

        StringTokenizer st = new StringTokenizer(message, SEPARATOR);

        final String room_id = st.nextToken();
        final String id = st.nextToken();
        final String user_id = st.nextToken();
        final String msg_type = st.nextToken();
        final String msg = st.nextToken();
        final String timestamp = st.nextToken();

        Log.d(TAG, "run: room_id : " + room_id + " user_id : " + user_id + " msg : " + msg);

        boolean isRoomExists = false;
        for(int i = 0; i < chatRoomList.size(); i++) {
            if(chatRoomList.get(i).getRoom_id().equals(room_id)) {
                isRoomExists = true;
                chatRoomList.get(i).setLastMessage(msg);
                chatRoomList.get(i).setTimestamp(TimeManager.compareTime(timestamp));
                break;
            }
        }

        if(!isRoomExists) {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoom_id(room_id);
            chatRoom.setLastMessage(msg);
            chatRoom.setTimestamp(timestamp);
            User user = new User();
            user.setUser_id(user_id);

            List<User> userList = new ArrayList<>();
            userList.add(user);
            chatRoom.setUserList(userList);
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

        setHasOptionsMenu(true);
        setDataToView();

        return view;
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
                                        SharedPrefManager.getInstance(getActivity()).getId() + "|" +
                                        chatRoomListString;

                                service.receiveData(msg);

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
