package com.example.saegeullee.applicationoneproject;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.saegeullee.applicationoneproject.Adapter.ChatRoomAddFriendAdapter;
import com.example.saegeullee.applicationoneproject.Adapter.ChatRoomAddFriendTopAdapter;
import com.example.saegeullee.applicationoneproject.Models.User;
import com.example.saegeullee.applicationoneproject.Service.ChatService;
import com.example.saegeullee.applicationoneproject.Utility.RequestHandler;
import com.example.saegeullee.applicationoneproject.Utility.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddFriendInChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "AddFriendInChatRoomActi";

    private Toolbar toolbar;
    private Button confirmBtn;
    private TextView friendNum;

    // 채팅방에 추가하려는 목록(상단)
    private RecyclerView addFriendListRecyclerView;
    private ChatRoomAddFriendTopAdapter addFriendTopAdapter;
    private ArrayList<User> addFriendTopList;

    // 친구목록(하단)
    private RecyclerView friendListRecyclerView;
    private ChatRoomAddFriendAdapter friendListAdapter;

//    private EditText friendSearchET;

    //현재 방에 들어있는 사람 목록 -> getChatRoomMemberList() 를 통해 받아온다.
    private ArrayList<User> originalChatRoomMemberList;

    //친구 목록 -> 현재 방에 들어있는 사람은 따로 표시를 해주어야 한다.
    private ArrayList<User> friendList;

    public static final String SEPARATOR = "|";
    private String chatRoomId;

    private boolean isOneToOneChatRoom;
    //isOneToOneChatRoom true 일때 1대1 채팅 중이던 친구의 정보를 얻기 위한 변수
    private User friend;

    /**
     * 2019/2/14 22:00 pm
     * 문제점 발생
     * -> ChatFragment, ChatRoomActivity 를 거쳐 현재 AddFriendInChatRoom 액티비티 까지 오면
     * Service 에 바운드된 액티비티가 하나도 없어지게 된다. 그렇게 되면 Service 는 Destroy 가 되고
     * 뒤로가기를 눌러서 다시 ChatRoomActivity 로 가게 되면 새로운 Service 가 Create 된다.
     * 해결
     * 이를 방지하기 위해 현재 액티비티에도 서비스를 바운드 시켜 서비스가 계속 액티비티에 바운드 된 상태를
     * 유지할 수 있도록 하였다.
     */

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

            isBound = true;
            Log.d(TAG, "onServiceConnected: isBound : " + isBound);

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
//
//        /**
//         * 서비스에 연결하려면 서비스가 시작되어야 하는데 이 플래그(BIND_AUTO_CREATE)를 설정하면
//         * 서비스를 따로 시작하지 않고도 바인드 서비스를 사용할 수 있다.
//         * 바운딩 되면 서비스가 자동으로 시작된다.
//         */
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
        setContentView(R.layout.activity_add_friend_in_chat_room);

        initUI();
        getOriginalChatRoomMemberList();
        setDataToView();
    }

    private void setDataToView() {

        friendList = getFriendList();

        // 어댑터에 originalChatRoomMemberList 도 같이 보내서 모든 친구리스트 중에서 이미 originalChatRoomMemberList 에 있는 사용자는
        // ViewType 을 걸어주어서 비활성 모드로 표시한다.
        friendListAdapter = new ChatRoomAddFriendAdapter(this, friendList);
        friendListRecyclerView.setAdapter(friendListAdapter);

        addFriendTopAdapter = new ChatRoomAddFriendTopAdapter(this, addFriendTopList);
        addFriendListRecyclerView.setAdapter(addFriendTopAdapter);


        friendListAdapter.setOnRadioButtonClickListener(new ChatRoomAddFriendAdapter.OnRadioButtonClickListener() {
            @Override
            public void onRadioButtonChecked(int position) {
                addFriendListRecyclerView.setVisibility(View.VISIBLE);
                addFriendTopList.add(friendList.get(position));
                addFriendTopAdapter.notifyDataSetChanged();
                addFriendListRecyclerView.getLayoutManager().scrollToPosition(addFriendTopAdapter.getItemCount() - 1);

                friendList.get(position).setRadioBtnChecked(true);
                friendListAdapter.notifyDataSetChanged();

                if(addFriendTopList.size() > 0) {
                    confirmBtn.setEnabled(true);
                    friendNum.setVisibility(View.VISIBLE);
                    friendNum.setText(String.valueOf(addFriendTopList.size()));
                }
            }

            @Override
            public void onRadioButtonUnChecked(int position) {
                for(int i = 0; i < addFriendTopList.size(); i++) {
                    if(addFriendTopList.get(i).getUser_id().equals(friendList.get(position).getUser_id())) {

                        friendList.get(position).setRadioBtnChecked(false);
                        friendListAdapter.notifyDataSetChanged();

                        addFriendTopList.remove(i);
                        addFriendTopAdapter.notifyItemRemoved(i);
                        if(addFriendTopList.size() == 0) {
                            addFriendListRecyclerView.setVisibility(View.GONE);
                        }
                    }
                }

                if(addFriendTopList.size() < 1) {
                    confirmBtn.setEnabled(false);
                    friendNum.setVisibility(View.GONE);
                }
                friendNum.setText(String.valueOf(addFriendTopList.size()));
            }
        });

        addFriendTopAdapter.setOnCancelBtnClickListener(new ChatRoomAddFriendTopAdapter.OnCancelBtnClickListener() {
            @Override
            public void onCancelBtnClicked(int position) {

                addFriendTopList.remove(position);
                addFriendTopAdapter.notifyItemRemoved(position);

                if(addFriendTopList.size() > 0) {
                    confirmBtn.setEnabled(true);
                    friendNum.setVisibility(View.VISIBLE);
                    friendNum.setText(String.valueOf(addFriendTopList.size()));
                } else {
                    confirmBtn.setEnabled(false);
                    friendNum.setVisibility(View.GONE);
                }

                for(int i =0; i < friendList.size(); i++) {
                    if(friendList.get(i).getUser_id().equals(addFriendTopList.get(position).getUser_id())) {

                        /**
                         * 2019/1/28
                         * You must change your model from your activity and then ask adapter to update the view
                         */

                        friendList.get(i).setRadioBtnChecked(false);
                        friendListAdapter.notifyDataSetChanged();
                        break;
                    }
                }

            }
        });

    }

    private void getOriginalChatRoomMemberList() {

        if(getIntent().hasExtra(getString(R.string.chatroom_member_list))) {

            originalChatRoomMemberList = getIntent().getParcelableArrayListExtra(getString(R.string.chatroom_member_list));
            Log.d(TAG, "getChatRoomMemberList: " + originalChatRoomMemberList.toString());
        }

        if(getIntent().hasExtra(getString(R.string.db_chat_room_id))) {
            chatRoomId = getIntent().getStringExtra(getString(R.string.db_chat_room_id));
            Log.d(TAG, "getOriginalChatRoomMemberList: chatRoomId : " + chatRoomId);
        }

        if(getIntent().hasExtra(getString(R.string.intent_chatroom_is_one_to_one_chatroom))) {

            isOneToOneChatRoom = getIntent().getBooleanExtra(getString(R.string.intent_chatroom_is_one_to_one_chatroom), true);
            Log.d(TAG, "getOriginalChatRoomMemberList: isOneToOneChatRoom : " + isOneToOneChatRoom);
        }
    }


    private void initUI() {

        confirmBtn = findViewById(R.id.confirmBtn);
        friendNum = findViewById(R.id.friendNum);

        toolbar = findViewById(R.id.top_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("대화상대 초대");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        addFriendListRecyclerView = findViewById(R.id.addFriendListRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        linearLayoutManager.setStackFromEnd(true);
        addFriendListRecyclerView.setLayoutManager(linearLayoutManager);

        friendListRecyclerView = findViewById(R.id.friendListRecyclerView);
        friendListRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
//        friendSearchET = findViewById(R.id.editText);

        friendList = new ArrayList<>();
        addFriendTopList = new ArrayList<>();

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!isOneToOneChatRoom) {
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(getString(R.string.chatroom_add_member_list), addFriendTopList);
                    setResult(AddFriendInChatRoomActivity.RESULT_OK, intent);
                } else {

                    /**
                     * 여기 지금 로직이 이상해
                     * 아침에 일어나서 여기 확ㅇ니해라
                     * 초대해서 새로 생성 된 채팅방이 채팅방 목록에서
                     * 친구아이디가 중복해서 보이게 되거든
                     */

                    addFriendTopList.add(friend);

                    User me = new User();
                    me.setId(SharedPrefManager.getInstance(AddFriendInChatRoomActivity.this).getId());
                    me.setUser_id(SharedPrefManager.getInstance(AddFriendInChatRoomActivity.this).getUserId());
                    me.setProfile_image(SharedPrefManager.getInstance(AddFriendInChatRoomActivity.this).getUserProfileImage());

                    addFriendTopList.add(me);

                    Intent intent = new Intent(AddFriendInChatRoomActivity.this, ChatRoomActivity.class);
                    intent.putParcelableArrayListExtra(getString(R.string.chatroom_add_member_list), addFriendTopList);
                    intent.putExtra(getString(R.string.intent_chatroom_notice_create_new_chatroom), true);
                    startActivity(intent);
                }

                finish();
            }
        });

    }
//
//    private void insertNewChatRoomMembers(final String addUserList) {
//
//        StringRequest stringRequest = new StringRequest(
//
//                Request.Method.POST,
//                Constants.URL_INSERT_NEW_CHAT_ROOM_MEMBERS,
//
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        }
//        ) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//
//                /**
//                 * 몇 번 채팅방의 누가 어떤 사용자들을 초대하는가
//                 */
//                params.put(getString(R.string.db_chat_room_id), chatRoomId);
//                params.put(getString(R.string.db_users_id), String.valueOf(SharedPrefManager.getInstance(AddFriendInChatRoomActivity.this).getId()));
//                params.put(getString(R.string.db_chat_room_add_user_list), addUserList);
//
//                return params;
//            }
//        };
//        RequestHandler.getInstance(AddFriendInChatRoomActivity.this).addToRequestQueue(stringRequest);
//    }
//

    private ArrayList<User> getFriendList() {

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_GET_FRIEND_LIST_FOR_CHATROOM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            Log.d(TAG, "onResponse: message : " + jsonObject.getString("message"));

                            JSONArray jsonArray = jsonObject.getJSONArray("friend_list");

                            for(int i = 0; i < jsonArray.length(); i++) {

                                JSONObject obj = jsonArray.getJSONObject(i);
                                Log.d(TAG, "onResponse: obj " + obj.toString());

                                User user = new User(obj, AddFriendInChatRoomActivity.this);

                                Log.d(TAG, "onResponse: user : " + user.toString());

                                if(isOneToOneChatRoom) {
                                    if(user.isInChatRoom()) {
                                        friend = user;

                                        Log.d(TAG, "onResponse: friend : " + friend.toString());
                                    }
                                }

                                if(!String.valueOf(user.getId()).equals(String.valueOf(SharedPrefManager.getInstance(AddFriendInChatRoomActivity.this).getId()))) {
                                    friendList.add(user);
                                }
                            }

                            Log.d(TAG, "onResponse: friendList size : " + friendList.size());
                            friendListAdapter.notifyDataSetChanged();

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
                 * 현재 사용자의 친구 목록을 불러온다.
                 *
                 * 1/24 9:45pm 여기서부터 하면된다.
                 * 리사이클러뷰에 친구 목록 띄워줘야하는데 여기서 막힘
                 */

                /**
                 * 2/15 3:21 am
                 * 현재 이미 이 채팅방에 있는 사용자는 blur 처리를 해주기 위해 서버로 현재 채팅방의 아이디도 같이 보낸다.
                 */

                params.put(getString(R.string.db_users_id), String.valueOf(SharedPrefManager.getInstance(AddFriendInChatRoomActivity.this).getId()));
                params.put(getString(R.string.db_chat_room_id), chatRoomId);

                return params;
            }
        };

        RequestHandler.getInstance(AddFriendInChatRoomActivity.this).addToRequestQueue(stringRequest);
        return friendList;
    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
