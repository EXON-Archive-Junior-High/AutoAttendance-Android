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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        mIsBound = true;    // Serive가 돌고있다면 true / MainActivity에서 Service를 이미 시작 시킴..
        //mSocket = MainActivity.
        // 서비스 시작
        setStartService();

        // test
        testBtn1 = (AppCompatButton)findViewById(R.id.send_button2);
        testBtn2 = (AppCompatButton)findViewById(R.id.send_button3);
        testBtn1.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_button2: // "A to S"
                sendMessageToService("A to S");
                Toast.makeText(ChatActivity.this, "A to S", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send_button3: // "S to A"
                break;
        }
    }

    /** 서비스 시작 및 Messenger 전달 */
    private void setStartService() {
        startService(new Intent(ChatActivity.this, socketService.class));
        bindService(new Intent(this, socketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /** 서비스 정지 */
    private void setStopService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        stopService(new Intent(ChatActivity.this, socketService.class));
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, socketService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /** Service 로 부터 message를 받음 */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i("test","act : what "+msg.what);
            switch (msg.what) {
                case socketService.MSG_SEND_TO_ACTIVITY:
                    int value1 = msg.getData().getInt("fromService");
                    String value2 = msg.getData().getString("test");
                    Toast.makeText(ChatActivity.this, value2, Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    }));

    /** Service 로 메시지를 보냄 */
    private void sendMessageToService(String str) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, socketService.MSG_SEND_TO_SERVICE, str);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }


}
