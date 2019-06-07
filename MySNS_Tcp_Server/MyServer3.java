package MyChattingProgram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Chat2Fragment
 * 서버와의 통신을 JSON 타입으로 변경
 */

public class MyServer3 {

    DBManager dbManager;
    // 채팅방 액티비티에 있을 때
    HashMap<String, HashMap<String, MyserverRec>> chatRoomMap;
    HashMap<String, MyserverRec> sessionMap;

    ServerSocket serverSocket = null;
    Socket socket = null;

    public static final String TAG_ALL_CHATROOM = "all_chatroom";
    public static final String TAG_ROOM_NUMBER = "room_number";
    public static final String TAG_ROOM_MSG = "msg";
    // 클라이언트에서 createChatRoom() 된 경우
    public static final String TAG_NEW_ROOM = "new_chatroom";
    // 클라이언트가 최초 앱에 접속 MainActivity onCreate 에서 현재 앱에 접속했다는 신호를 서버에서 받음
    public static final String TAG_INIT = "init";
    public static final String TAG_ADD_MEMBER = "add_new_member";
    public static final String TAG_CREATE_GROUP_CHAT = "create_group_chat";

    public MyServer3() {
        chatRoomMap = new HashMap<String, HashMap<String, MyserverRec>>();
        Collections.synchronizedMap(chatRoomMap);

        sessionMap = new HashMap<String, MyserverRec>();
        Collections.synchronizedMap(sessionMap);

        dbManager = new DBManager();
    }

    public static void main(String[] args) {
        MyServer3 ms = new MyServer3();
        ms.init();
    }

    public void init() {
        try {
            serverSocket = new ServerSocket(9998);
            System.out.println("-------------서버 시작--------------");

            while(true) {
                socket = serverSocket.accept();
                System.out.println(socket.getInetAddress() + " : " + socket.getPort()); //클라이언트 정보 (ip, 포트) 출력

                Thread thread = new MyserverRec(socket);
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRoomMsg(String chatRoomId, String sessionId, String name, String profile_image,
                            String msg_type, String msg, String timestamp) {

        HashMap<String, MyserverRec> cRMap = chatRoomMap.get(chatRoomId);

        Iterator<String> chatRoom_it = chatRoomMap.get(chatRoomId).keySet().iterator();

        while(chatRoom_it.hasNext()) {
            try {

                JSONObject object = new JSONObject();
                object.put("chatRoomId", chatRoomId);
                object.put("sessionId", sessionId);
                object.put("name", name);
                object.put("profile_image", profile_image);
                object.put("msg_type", msg_type);
                object.put("msg", msg);
                object.put("timestamp", timestamp);

                MyserverRec myserverRec = cRMap.get(chatRoom_it.next());
                myserverRec.writer.write(object.toString());
                myserverRec.writer.newLine();
                myserverRec.writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class MyserverRec extends Thread {

        BufferedWriter writer;
        BufferedReader reader;

        String userId = ""; //사용자 id 저장 -> 채팅방 액티비티
        String user_id = ""; //사용자 id 저장 -> 채팅방 목록 액티비티

        String roomId = ""; //방 이름(id) 저장
        String recvData = "";

        boolean isInChatRoom = false;
        boolean isInChatRoomList = false;

        public MyserverRec(Socket socket) {


            try {
//                in = new DataInputStream(socket.getInputStream());
//                out = new DataOutputStream(socket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // this 로 Thread 를 hashMap 에 넣으면 안된다.

        private void putSessionInChatRoom(String sessionId, String chatRoomId) {

            // null 이 아니라면 현재 다른 사용자가 이 chatroomId 의 채팅방에 접속중
            if(chatRoomMap.get(chatRoomId) != null) {

                System.out.println(chatRoomId + "번 채팅방에 접속중인 사용자 있음");

                HashMap<String, MyserverRec> tempMap;
                tempMap = chatRoomMap.get(chatRoomId);
                tempMap.put(sessionId, sessionMap.get(sessionId));
                System.out.println("tempMap : " + tempMap);

                chatRoomMap.put(chatRoomId, tempMap);

                // null 이면 현재 어떤 사용자도 이 채팅방에 접속중이 아니다.
            } else {

                System.out.println(chatRoomId + "번 채팅방에 접속중인 사용자 없음");
                HashMap<String, MyserverRec> newSession = new HashMap<>();
                newSession.put(sessionId, sessionMap.get(sessionId));

                chatRoomMap.put(chatRoomId, newSession);

            }
            System.out.println("after 현재 유저가 접속중인 모든 채팅방 id 목록 : " + chatRoomMap.toString());

        }

        private boolean isOnline(String sessionId) {

            // 상대방(친구)이 이미 온라인에 접속중
            if(sessionMap.get(sessionId) != null) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void run() {

            try {

                while((recvData = reader.readLine()) != null) {

                    System.out.println(recvData);

                    System.out.println(Thread.currentThread().getName());
                    System.out.println("recvData : " + recvData);

                    JSONObject jObj = new JSONObject(recvData);
                    String flag = jObj.getString("flag");
                    System.out.println("flag : " + flag);
                    System.out.println(jObj.toString());

                    if(flag.equals(TAG_INIT)) {

                        String sessionId = jObj.getString("sessionId");
                        sessionMap.put(sessionId, this);

                        System.out.println("현재 접속중인 모든 사용자 sessionMap : " + sessionMap.toString());


                    } else if(flag.equals(TAG_ALL_CHATROOM)) {

                        JSONArray jsonArray = jObj.getJSONArray("chatroom_id_list");
                        String sessionId = jObj.getString("sessionId");

                        //현재 접속중인 모든 사용자를 관리하기 위한 map

                        if(sessionMap.get(sessionId) != null) {
                            // 해당 사용자 이미 온라인 (접속중)

                        } else {
                            // 해당 사용자 오프라인 상태였기 때문에 온라인 상태로 바꿔준다.
                            HashMap<String, MyserverRec> tempMap = new HashMap<>();

                            tempMap = sessionMap;
                            tempMap.put(sessionId, this);

                            sessionMap = tempMap;
                        }

                        System.out.println("현재 접속중인 모든 사용자 sessionMap : " + sessionMap.toString());

                        System.out.println("before 현재 유저가 접속중인 모든 채팅방 id 목록 : " + chatRoomMap.toString());

                        for(int i = 0; i < jsonArray.length(); i++) {

                            String chatRoomId = jsonArray.get(i).toString();
                            putSessionInChatRoom(sessionId, chatRoomId);
                        }

                        //TEST

//                        Iterator iterator = chatRoomMap.values().iterator();
//
//                        while(iterator.hasNext()) {
//                            System.out.println("iterator.next() : " + iterator.next());
//                            System.out.println(iterator.next());
//                        }

                    } else if(flag.equals(TAG_ROOM_NUMBER)) {

                        String chatRoomId = jObj.getString("chatRoomId");
                        String sessionId = jObj.getString("sessionId");

                        //userId 사용자를 chatRoomId 방에 넣어주면 된다.
//                        putSessionInChatRoom(chatRoomId, sessionId);

                    } else if(flag.equals(TAG_ROOM_MSG)) {

                        String chatRoomId = jObj.getString("chatRoomId");
                        String sessionId = jObj.getString("sessionId");
                        String name = jObj.getString("name");
                        String profile_image = jObj.getString("profile_image");
                        String msg = jObj.getString("msg");
                        String msg_type = jObj.getString("msg_type");
                        String timestamp = jObj.getString("timestamp");

                        // 몇번 채팅방의 어떤 사용자가 메시지를 언제 보냈는가
                        sendRoomMsg(chatRoomId, sessionId, name, profile_image, msg_type, msg, timestamp);
                        dbManager.insertMessages(chatRoomId, sessionId, msg, msg_type);

                    // 클라이언트에서 createChatRoom() 된 경우
                    } else if(flag.equals(TAG_NEW_ROOM)) {

                        String chatRoomId = jObj.getString("chatRoomId");
                        String sessionId = jObj.getString("sessionId");
                        String friendSessionId = jObj.getString("friend_sessionId");

                        //상대방이 현재 앱을 사용하고 있을 경우에만 putSessionInChatRoom 을 해줘야 한다.
                        //현재 사용자 또한 putSessionInChatRoom 해줘야 한다.
                        // String sessionId, String chatRoomId
                        putSessionInChatRoom(sessionId, chatRoomId);

                        if(isOnline(friendSessionId)) {
                            System.out.println("상대방이 서버에 접속중입니다.");
                            putSessionInChatRoom(friendSessionId, chatRoomId);
                        } else {
                            System.out.println("상대방 서버에 접속중이지 않습니다.");
                        }

                    } else if(flag.equals(TAG_ADD_MEMBER)) {

                        String chatRoomId = jObj.getString("chatRoomId");
                        String sessionId = jObj.getString("sessionId");
                        String name = jObj.getString("name");
                        String timestamp = jObj.getString("timestamp");

                        JSONArray array = jObj.getJSONArray("memberList");

                        //초대한 사람들 목록
                        for(int i = 0; i < array.length(); i++) {

                            JSONObject object = array.getJSONObject(i);

                            String id = object.getString("id");
                            String user_id = object.getString("userId");

                            //채팅방에 초대한 사용자가 서버에 접속중이라면 해당 채팅방에 세션을 넣어준다.
                            if(isOnline(id)) {
                                putSessionInChatRoom(id, chatRoomId);
                            }

                        }

                        // msg_type invited 이면 invited
                        sendRoomMsg(chatRoomId, sessionId, name, "", "invited", array.toString(), timestamp);

                        dbManager.insertMessages(chatRoomId, sessionId, array.toString(), "invited");
                        dbManager.addUserInChatRoom(chatRoomId, array.toString());

                    } else if(flag.equals(TAG_CREATE_GROUP_CHAT)) {

                        String chatRoomId = jObj.getString("chatRoomId");
                        String sessionId = jObj.getString("sessionId");
                        String name = jObj.getString("name");
                        String timestamp = jObj.getString("timestamp");

                        JSONArray array = jObj.getJSONArray("add_user_list");

                        //초대한 사람들 목록
                        for(int i = 0; i < array.length(); i++) {

                            JSONObject object = array.getJSONObject(i);

                            String id = object.getString("id");
                            String user_id = object.getString("userId");

                            //채팅방에 초대한 사용자가 서버에 접속중이라면 해당 채팅방에 세션을 넣어준다.
                            if(isOnline(id)) {
                                putSessionInChatRoom(id, chatRoomId);
                            }
                        }

//                        putSessionInChatRoom(sessionId, chatRoomId);

                        // msg_type invited 이면 invited
                        sendRoomMsg(chatRoomId, sessionId, name, "", "invited", array.toString(), timestamp);
                        dbManager.insertMessages(chatRoomId, sessionId, array.toString(), "invited");

                    }

                    System.out.println("Thread Num : " + Thread.activeCount() );
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e + "--------->");

            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

                System.out.println("finally socket close");

            }

        }

    }

}
