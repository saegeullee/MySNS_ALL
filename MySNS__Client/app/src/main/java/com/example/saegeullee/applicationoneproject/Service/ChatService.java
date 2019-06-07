package com.example.saegeullee.applicationoneproject.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


/**
 * 19/2/2
 * 서비스는 액티비티 내의 리스트 인터페이스를 implement 할 수 없구나
 */
public class ChatService extends Service {

    private static final String TAG = "ChatService";

    public interface OnMessageReceiveFromServerListener {
        void onMessageReceivedFromServer(String msg);
    }

    private OnMessageReceiveFromServerListener onMessageReceiveFromServerListener;

    public void setOnMessageReceiveFromServerListener(OnMessageReceiveFromServerListener listener) {
        this.onMessageReceiveFromServerListener = listener;
    }

    // ChatRoom3Activity 에서 사용자에게 입력 데이터를 받음

    public void receiveData(String msg) {

        Log.d(TAG, "onMessageReceived: msg : " + msg);

        Message message = mServiceHandler.obtainMessage();
        message.what = MSG_START;
        message.obj = msg;

        //핸들러쓰레드를 통해 문자를 서버에 전달한다.
        mServiceHandler.sendMessage(message);
    }

//    ChatService 의 레퍼런스를 반환하는 Binder 객체

    private IBinder mBinder = new MyBinder();
    private boolean mAllowedRebind = true;

    public class MyBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: in thread : " + Thread.currentThread().getName());

        initThread();

        return START_STICKY;
    }

    private Handler mMainHandler;

    private Looper mServiceLooper;
    private HandlerThread handlerThread;
    private ServiceHandler mServiceHandler;

    private TCPClient client = null;

    private Socket socket;
    private BufferedReader networkReader = null;
    private BufferedWriter networkWriter = null;

    /**
     * 핸들러가 수행하는 기능은 아래 상수에 의해 결정
     */

    public static final int MSG_CONNECT = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_CLIENT_STOP = 3;
    public static final int MSG_SERVER_STOP = 4;
    public static final int MSG_START = 5;
    public static final int MSG_RECEIVED = 6;
    public static final int MSG_ERROR = 999;

    public ChatService() {
    }

    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate: in");
        super.onCreate();

        //핸들러쓰레드는 화면에 입력된 문자를 서버에 전송하는 작업을 수행
        initHandlerThread();
        initMainHandler();

        //소켓을 생성하고 서버와의 연결작업 수행
        //서버가 전송한 메시지를 수신받아 핸들러를 통해 메인쓰레드에 전달
        initThread();
    }

    private void initThread() {

        if(client == null) {

            client = new TCPClient("192.168.3.21", 9998);
            client.start();
        }
    }

    private void initMainHandler() {

        //메인쓰레드가 사용하는 핸들러
        mMainHandler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {

                Log.d(TAG, "mMainHandler handleMessage: in");

                String m;
                switch (msg.what) {
                    case MSG_CONNECT:
                        m = "정상적으로 서버에 접속하였습니다.";

//                        text.setText(m);
                        break;

                    case MSG_CLIENT_STOP:
                        // 사용자가 연결 작업을 종료시킬 때 호출된다.

                        m = "클라이언트가 접속을 종료하였습니다.";
                        break;

                    case MSG_SERVER_STOP:
                        //서버에 의해 연결이 종료될 때 호출된다.

                        m= "서버가 접속을 종료하였습니다.";
                        break;

                    case MSG_START:
                        //메시지를 전송하였을 때 호출된다.
                        m = "메세지 전송 완료!";
//                        text.setText((String)msg.obj);
                        break;

                    case MSG_RECEIVED:

                        if(onMessageReceiveFromServerListener != null) {
                            onMessageReceiveFromServerListener.onMessageReceivedFromServer((String) msg.obj);
                        }

                        break;

                    default:
                        // 에러가 발생했을 때 호출된다.
                        m = "에러 발생!";
//                        text.setText((String)msg.obj);
                        break;
                }

//                Toast.makeText(ChatRoom2Activity.this, m, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    private void initHandlerThread() {

        //1. 핸들러를 생성하고 실행
        handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();

        //2. 핸들러쓰레드로부터 루퍼객체를 얻는다.
        mServiceLooper = handlerThread.getLooper();

        //3. 핸들러쓰레드가 제공한 루퍼객체를 매개변수로 하여 핸들러 객체를 생성한다.
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }


    //핸들러쓰레드에서 사용하는 핸들러를 만든다.
    //핸들러쓰레드는 화면에 입력된 문자를 서버에 전송하는 작업을 수행
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_START:
                    Message toMain = mMainHandler.obtainMessage();
                    try {

                        networkWriter.write((String) msg.obj);
                        networkWriter.newLine();
                        networkWriter.flush();
                        Log.d(TAG, "handleMessage: MSG_START");
//                        outputStream.writeUTF((String) msg.obj);
//                        outputStream.flush();

                        toMain.what = MSG_START;

                    } catch (IOException e) {
                        toMain.what = MSG_ERROR;
                        e.printStackTrace();

                    }

                    toMain.obj = msg.obj;
                    mMainHandler.sendMessage(toMain);
                    break;

                case MSG_STOP:
                    Log.d(TAG, "handleMessage: MSG_STOP");
                    Log.d(TAG, "handleMessage: msg : " + msg.obj);


                    client.quit();
                    client = null;
                    break;
                case MSG_CLIENT_STOP:
                    Log.d(TAG, "handleMessage: MSG_STOP");

                    client.quit();
                    client = null;
                    break;
                case MSG_SERVER_STOP:
                    Log.d(TAG, "handleMessage: MSG_STOP");
                    client.quit();
                    client = null;
                    break;

            }
        }
    }


    //일반 쓰레드는 소켓을 생성하고 서버와의 연결 작업을 수행
    //서버가 전송한 메시지를 수신받아 핸들러를 통해 메인 쓰레드에 전달한다.

    public class TCPClient extends Thread {

        Boolean loop;
        SocketAddress socketAddress;
        String line;
//        private final int connection_timeout = 100000;

        public TCPClient(String ip, int port) {

            Log.d(TAG, "TCPClient: constructor");

            socketAddress = new InetSocketAddress(ip, port);
        }

        @Override
        public void run() {
            try {

                Log.d(TAG, "run: in");
                socket = new Socket();

                socket.connect(socketAddress);
                networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                InputStreamReader i = new InputStreamReader(socket.getInputStream());
                networkReader = new BufferedReader(i);


                //위의 모든 작업이 정상적으로 수행되면 화면으로 연결되었음을 알린다.
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_CONNECT;
                mMainHandler.sendMessage(toMain);
                loop = true;

            } catch (Exception e) {
                loop = false;
                e.printStackTrace();
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_ERROR;
                toMain.obj = "소켓을 생성하지 못했습니다.";
                mMainHandler.sendMessage(toMain);
            }

            while(loop) {

                try {

                    line = networkReader.readLine();
                    Log.d(TAG, "run: Message From server : " + line);

                    //서버로부터 FIN 패킷을 수신하면 read() 메소드는 null 을 반환한다.
                    if(line == null)
                        break;

                    // 읽어들인 문자열은 화면 출력을 위해 Runnable 객체와 핸들러를 사용하였다.
                    // 메세지를 사용하지 않은 이유는 단지 이렇게 하더라도 작동하는지 보여주기 위해서이다.

                    Message message = mMainHandler.obtainMessage();
                    message.what = MSG_RECEIVED;
                    message.obj = line;

                    mMainHandler.sendMessage(message);

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            try {
                if(networkWriter != null) {

                    networkWriter.close();
                    /**
                     * 스트림이나 소켓을 닫았다면, 해당 객체들이 가비지콜렉션의 대상이 될 수 있도록
                     * 변수에 null 을 할당하는 것이 좋다. 이는 자바의 권고사항이다.
                     */
                    networkWriter = null;

                }

                if(networkReader != null) {

                    networkReader.close();
                    networkReader = null;

                }

                if(socket != null) {
                    socket.close();
                    socket = null;
                }

                client = null;

                //서버로부터 FIN 패킷을 받았는지 아니면 사용자가 종료 버튼을 눌렀는지 여부를
                //loop 변수로 판단한다.
                if(loop) {

                    // loop 가 true 라면 서버 에 의한 종료로 간주한다.
                    loop = false;
                    Message toMain = mMainHandler.obtainMessage();
                    toMain.what = MSG_SERVER_STOP;
                    toMain.obj = "네트워크가 끊어졌습니다.";
                    mMainHandler.sendMessage(toMain);

                }

            } catch (IOException e) {
                Log.d(TAG, "run: " + e.getMessage());
                e.printStackTrace();
                Message toMain = mMainHandler.obtainMessage();
                toMain.what = MSG_ERROR;
                toMain.obj = "소켓을 닫지 못했습니다..";
                mMainHandler.sendMessage(toMain);
            }
        }

        public void quit() {
            loop = false;
            try {
                if(socket != null) {

                    socket.close();
                    socket = null;
                    Message toMain = mMainHandler.obtainMessage();
                    toMain.what = MSG_CLIENT_STOP;
                    toMain.obj = "접속을 중단합니다.";
                    mMainHandler.sendMessage(toMain);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "onBind: ");
        return mBinder;
    }

//    @Override
//    public boolean onUnbind(Intent intent) {
//
//        Log.d(TAG, "onUnbind: in");
//        //클라이언트에서 unbind 가 되면 소켓연결이 끊긴다. 그럼 서버에서 관리하고 있는 현재 클라이언트의
//        //소켓을 닫아줘야 한다.
//
//
//        Message message = mServiceHandler.obtainMessage();
//        message.what = MSG_CLIENT_STOP;
//
//        //핸들러쓰레드를 통해 문자를 서버에 전달한다.
//        mServiceHandler.sendMessage(message);
//
//        return super.onUnbind(intent);
//    }
}
