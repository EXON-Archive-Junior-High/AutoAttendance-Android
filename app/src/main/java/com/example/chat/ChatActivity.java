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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.net.Socket;

public class ChatActivity extends AppCompatActivity  implements View.OnClickListener {
    Socket mSocket = null;
    private boolean mIsBound;
    private Messenger mServiceMessenger = null;
    private AppCompatButton sendMessageBtn = null;
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
        intent.putExtra("type", "start");
        intent.putExtra("id", userId);
        intent.putExtra("pwd", userPwd);
        startService(intent); // Service에 데이터를 전달한다.

        sendMessageBtn = (AppCompatButton)findViewById(R.id.send_button);
        sendMessageBtn.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);

        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent != null) {
            String type = intent.getStringExtra("type");

            if (type != null) {
                if (type.equals("Chat")) {
                    String msg = intent.getStringExtra("msg");
                    TextView textView = (TextView)findViewById(R.id.chatbox);
                    textView.append(msg + "\n");
                }
            }


            // Toast.makeText(this, "type : " + chat + ", msg : " + name, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_button:
                EditText editText = (EditText)findViewById(R.id.text_edit);
                EditText editUser = (EditText)findViewById(R.id.user_edit);
                Intent intent = new Intent(getApplicationContext(), socketService.class);
                intent.putExtra("type", "chat");
                Log.d("log", editUser.getText().toString() + "/" + editText.getText().toString());
                intent.putExtra("msg", editUser.getText().toString() + "/" + editText.getText().toString());
                startService(intent); // Service에 데이터를 전달한다.
                Toast.makeText(ChatActivity.this, "채팅을 보냈습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
