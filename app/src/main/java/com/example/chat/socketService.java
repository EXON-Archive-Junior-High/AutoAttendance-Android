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

public class socketService  extends Service {
    public static final int MSG_REGISTER_CLIENT = 1;
    //public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;

    public static final int SOCKET_RECV_CHAT = 1;
    public static final int SOCKET_RECV_LOGIN = 2;

    public Socket socket = null;
    private String host = "192.168.35.82";
    private int port = 6666;

    private final int LOGIN_SUCCESS = 0;
    private final int LOGIN_ERROR = 1;

    private boolean isConnected = false;
    private String mId = null;
    private String mPwd = null;

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
        String sId = intent.getStringExtra("id");
        String sPwd = intent.getStringExtra("pwd");

        Log.d(TAG, "id : " + sId + ", pwd : " + sPwd);

        for (int i=0; i<5;++i) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) { };

            Log.d(TAG, "Waiting " + i + " seconds.");
        }

        Intent showIntent = new Intent(getApplicationContext(), ChatActivity.class);
        showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        showIntent.putExtra("chat", "hahahahaha");
        showIntent.putExtra("list", " person list");
        startActivity(showIntent); // Service에서 Activity로 데이터를 전달
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}