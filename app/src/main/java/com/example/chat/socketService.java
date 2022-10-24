package com.example.chat;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class socketService  extends Service {
    public static final int MSG_REGISTER_CLIENT = 1;
    //public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;

    public static final int SOCKET_RECV_CHAT = 1;
    public static final int SOCKET_RECV_LOGIN = 2;

    public Socket socket = null;
    private String host = "192.168.35.188";
    private int port = 6666;

    private final int FRIEND_REQUEST = 0;
    private final int SEND_ACTIVITY = 1;
    private final int SEND_SERVER = 2;
    private final int LOGIN_SUCCESS = 0;
    private final int LOGIN_ERROR = 1;

    private boolean isConnected = false;
    private String mId = null;
    private String mPwd = null;
    private boolean mLoginFlag = false;
    SendMessageHandler mHandler = null;


    private static final String TAG = "MyService";

    public socketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new SendMessageHandler();
        Log.d(TAG, "onCreate 호출됨.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() 호출됨.");

        if (intent == null) return Service.START_STICKY;
        else processCommand(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void processCommand(Intent intent) {
        String sType = intent.getStringExtra("type");
        if (sType.equals("start")) {
            // 서비스 처음 시작할 때
            String sId = intent.getStringExtra("id");
            String sPwd = intent.getStringExtra("pwd");
            if (sId != null) this.mId = sId;
            if (sPwd != null) this.mPwd = sPwd;
            Log.d(TAG, "id : " + sId + ", pwd : " + sPwd);
            ClientThread thread = new ClientThread();
            thread.start();
        } else if (sType.equals("chat")) {
            // 채팅 보낼 때
            String sMsg = intent.getStringExtra("msg");
            serverSendThread thread = new serverSendThread(SEND_SERVER, sMsg);
            thread.start();

        } else if (sType.equals("friend")) {
            // 친구 목록 요청 할 때
            serverSendThread thread = new serverSendThread(FRIEND_REQUEST, null);
            thread.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class serverSendThread extends Thread {
        private int mType = 0;
        private String mMsg = "";
        public serverSendThread(int type, String msg){
            mType = type;
            mMsg = msg;
        }
        @Override
        public void run() {
            try {
                if (mLoginFlag) {
                    DataOutputStream outstream = new DataOutputStream(socket.getOutputStream());
                    String msg = "";
                    outstream = new DataOutputStream(socket.getOutputStream());
                    if (mType == FRIEND_REQUEST) msg = "FriendList/Get";
                    else if (mType == SEND_SERVER) msg = "Chat/Send/" + mMsg;

                    outstream.writeUTF(msg);
                    outstream.flush();
                }
            } catch (Exception e) {

            }
        }
    }

    class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                if (!isConnected) {
                    isConnected = true;
                    socket = new Socket(host, port);

                    DataOutputStream outstream = new DataOutputStream(socket.getOutputStream());
                    String msg = "";
                    msg = "Login/" + mId + "/" + mPwd;
                    outstream.writeUTF(msg);
                    // Log.d("Message", msg.getBytes("UTF-8").toString());
                    outstream.flush();
                    Log.d("ClientStream", "Sent to server.");

                    DataInputStream instream = new DataInputStream(socket.getInputStream());
                    byte[] buf = new byte[1024];
                    int inputLength = instream.read(buf);
                    String inputMessage = new String(buf, 0, inputLength);
                    Log.d("ClientThread", "Received data: " + inputMessage);

                    String[] msgList = inputMessage.split("/");

                    Message msgHandle = mHandler.obtainMessage();
                    if (msgList[0].equals("Login")) {
                        if (msgList[1].equals("Success")) {
                            mLoginFlag = true;
                            //msgHandle.what = FRIEND_REQUEST;
                            //mHandler.sendMessage(msgHandle);
                        } else if (msgList[1].equals("Error")) {
                            mLoginFlag = false;
                        }
                    }

                    while (mLoginFlag) {
                        Arrays.fill(buf,(byte)0);
                        inputLength = instream.read(buf);
                        inputMessage = new String(buf, 0, inputLength);
                        Log.d("ClientThread", "Received data: " + inputMessage);
                        msgList = inputMessage.split("/");
                        msgHandle = mHandler.obtainMessage();
                        if (msgList[0].equals("Chat")) {
                            msgHandle.what = SEND_ACTIVITY;
                            Bundle data = new Bundle();
                            data.putString("type", msgList[0]);
                            data.putString("msg", msgList[2]);
                            msgHandle.setData(data);
                            mHandler.sendMessage(msgHandle);
                        } else if (msgList[0].equals("FriendList")) {
                            msgHandle.what = SEND_ACTIVITY;
                            Bundle data = new Bundle();
                            data.putString("type", msgList[0]);
                            data.putString("msg", msgList[2]);
                            msgHandle.setData(data);
                            mHandler.sendMessage(msgHandle);
                        } else if (msgList[0].equals("Schedule")) {
                            msgHandle.what = SEND_ACTIVITY;
                            Bundle data = new Bundle();
                            data.putString("type", msgList[0]);
                            data.putString("msg", msgList[2]);
                            msgHandle.setData(data);
                            mHandler.sendMessage(msgHandle);
                        }
                    }

                }
            } catch (Exception e) {
                mLoginFlag = false;
                Log.e("Error", "dd");

                e.printStackTrace();
            }
        }
    }
    public class SendMessageHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SEND_ACTIVITY:
                    Intent showIntent = new Intent(getApplicationContext(), ChatActivity.class);
                    showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    showIntent.putExtra("type", msg.getData().getString("type"));
                    showIntent.putExtra("msg", msg.getData().getString("msg"));
                    startActivity(showIntent); // Service에서 Activity로 데이터를 전달
                    break;
            }
        }
    }
}