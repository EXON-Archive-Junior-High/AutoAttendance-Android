package com.example.chat;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Socket socket = null;
    private String host = "192.168.35.188";
    private int port = 6666;

    private final int LOGIN_SUCCESS = 0;
    private final int LOGIN_ERROR = 1;
    private final int SERVER_ERROR = 2;

    SendMessageHandler mHandler = null;

    private Button mLoginBtn = null;
    private EditText mIdEdit = null;
    private EditText mPasswordEdit = null;

    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ClientThread thread = new ClientThread();
        // thread.start();

        mLoginBtn = (Button)findViewById(R.id.button);
        mIdEdit = (EditText)findViewById(R.id.id_edit);
        mPasswordEdit = (EditText)findViewById(R.id.password_edit);

        mLoginBtn.setOnClickListener(this);
        mHandler = new SendMessageHandler();
    }

    @Override
    protected void onPause(){
        super.onPause();
        // Thread나 소켓 recv를 잠시멈춤..
    }

    @Override
    protected void onResume(){
        super.onResume();
        // Thread나 소켓 recv 를 다시 시작.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Log.d("Login", "log");
        if (socket == null) {
            ClientThread thread = new ClientThread();
            thread.start();
        }
    }

    public class SendMessageHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case LOGIN_SUCCESS:
                    Toast.makeText(MainActivity.this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("id", msg.getData().getString("id"));
                    intent.putExtra("pwd", msg.getData().getString("pwd"));
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "서버를 확인해주세요!.", Toast.LENGTH_SHORT).show();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                startActivity(intent); //액티비티 띄우기
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    break;
                case LOGIN_ERROR:
                    Toast.makeText(MainActivity.this, msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(MainActivity.this, "서버를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    public Socket getSocket() {
        return socket;
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
                    msg = "Login/" + mIdEdit.getText().toString() + "/" + mPasswordEdit.getText().toString();
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
                            Bundle data = new Bundle();
                            data.putString("id", mIdEdit.getText().toString());
                            data.putString("pwd", mPasswordEdit.getText().toString());
                            msgHandle.setData(data);
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
            } catch (Exception e) {
                Message msgHandle = mHandler.obtainMessage();
                msgHandle.what = SERVER_ERROR;
                mHandler.sendMessage(msgHandle);
                Log.e("Error", "dd");

                e.printStackTrace();
            }
        }
    }
}