package com.example.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.net.Socket;

public class ChatActivity extends AppCompatActivity  implements View.OnClickListener {
    Socket mSocket = null;
    private boolean mIsBound;
    private Messenger mServiceMessenger = null;
    private AppCompatButton testBtn1 = null;
    private AppCompatButton testBtn2 = null;
    private socketService mBindService;
    private String userId = null;
    private String userPwd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        Intent revIntent = getIntent();
        userId = revIntent.getExtras().getString("id");
        userPwd = revIntent.getExtras().getString("pwd");

        mIsBound = true;    // Serive가 돌고있다면 true / MainActivity에서 Service를 이미 시작 시킴..
        //mSocket = MainActivity.
        // Service에서 보낸 Intent 객체를 전달받음
        Intent passedIntent = getIntent(); //
        processIntent(passedIntent);

        Intent intent = new Intent(getApplicationContext(), socketService.class);
        intent.putExtra("id", userId);
        intent.putExtra("pwd", userPwd);
        startService(intent); // Service에 데이터를 전달한다.

        // test
        testBtn1 = (AppCompatButton)findViewById(R.id.send_button2);
        testBtn2 = (AppCompatButton)findViewById(R.id.send_button3);
        testBtn1.setOnClickListener(this);
        testBtn2.setOnClickListener(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);

        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent != null) {
            String chat = intent.getStringExtra("chat");
            String name = intent.getStringExtra("list");

            Toast.makeText(this, "chat : " + chat + ", name : " + name, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_button2: // "A to S"
                Toast.makeText(ChatActivity.this, "A to S", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_button3: // "S to A"
                break;
        }
    }
}
