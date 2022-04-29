package com.example.chat;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Socket socket = null;

    private Button mLoginBtn = null;
    private EditText mIdEdit = null;
    private EditText mPasswordEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ClientThread thread = new ClientThread();
        thread.start();


        mLoginBtn = (Button)findViewById(R.id.button);
        mIdEdit = (EditText)findViewById(R.id.id_edit);
        mPasswordEdit = (EditText)findViewById(R.id.password_edit);

        mLoginBtn.setOnClickListener(this);
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
        Log.d("Login", "LoginBtnClick");
        DataOutputStream outstream = null;
        try {
            Log.d("Socket", socket.toString());
            outstream = new DataOutputStream(socket.getOutputStream());
            String msg = "Login/" + mIdEdit.getText().toString() + "/" + mPasswordEdit.getText().toString();
            outstream.writeUTF(msg);
            outstream.flush();
            Log.d("ClientStream", "Sent Login to server");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                String host = "192.168.35.82";
                int port = 6666;
                socket = new Socket(host, port);

                while (true) {
                    DataInputStream instream = new DataInputStream(socket.getInputStream());
                    byte[] buf = new byte[1024];
                    int inputLength = instream.read(buf);
                    String inputMessage = new String(buf, 0, inputLength);
                    Log.d("ClientThread", "Received data: " + inputMessage);

                    String[] msgList = inputMessage.split("/");
                    if (msgList[0] == "Login") {
                        if (msgList[1] == "Success") {
                            Toast.makeText(getApplicationContext(), "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else if (msgList[1] == "Error") {
                            Toast.makeText(getApplicationContext(), msgList[2], Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}