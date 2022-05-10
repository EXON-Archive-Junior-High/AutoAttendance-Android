package com.example.chat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.net.Socket;

public class ChatActivity extends AppCompatActivity {
    Socket mSocket = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        //mSocket = MainActivity.
    }
}
