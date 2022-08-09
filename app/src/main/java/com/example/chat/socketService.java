package com.example.chat;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class socketService  extends Service {
    int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    public Socket socket = null;
    private String host = "192.168.35.82";
    private int port = 6666;

    private final int LOGIN_SUCCESS = 0;
    private final int LOGIN_ERROR = 1;

    private SendMessageHandler mHandler = null;
    private boolean isConnected = false;
    private String mId = null;
    private String mPwd = null;
    @Override
    public void onCreate() {
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        mHandler = new SendMessageHandler();
        mId = intent.getStringExtra("id");
        mPwd = intent.getStringExtra("pwd");

        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.putExtra("msg","안녕하세욯 !");
        startActivity(mainIntent);

        return mStartMode;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }

    public class SendMessageHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case LOGIN_SUCCESS:
                    //Toast.makeText(MainActivity.this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                    //startActivity(intent);//액티비티 띄우기
                    break;
                case LOGIN_ERROR:
                    //Toast.makeText(MainActivity.this, msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
                    break;
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
                            msgHandle.what = LOGIN_SUCCESS;
                            mHandler.sendMessage(msgHandle);
                        } else if (msgList[1].equals("Error")) {
                            isConnected = false;
                            msgHandle.what = LOGIN_ERROR;
                            Bundle data = new Bundle();
                            data.putString("msg", msgList[2]);
                            msgHandle.setData(data);
                            mHandler.sendMessage(msgHandle);
                        }
                    }
                }



//                while (true) {
//                    DataInputStream instream = new DataInputStream(socket.getInputStream());
//                    byte[] buf = new byte[1024];
//                    int inputLength = instream.read(buf);
//                    String inputMessage = new String(buf, 0, inputLength);
//                    Log.d("ClientThread", "Received data: " + inputMessage);
//
//                    String[] msgList = inputMessage.split("/");
//                    if (msgList[0] == "Login") {
//                        if (msgList[1] == "Success") {
//                            Toast.makeText(getApplicationContext(), "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
//                        }
//                        else if (msgList[1] == "Error") {
//                            Toast.makeText(getApplicationContext(), msgList[2], Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
            } catch (Exception e) {
                Log.e("Error", "dd");
                e.printStackTrace();
            }
        }
    }
}