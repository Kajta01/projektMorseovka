package com.example.buzzer01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketSend extends AppCompatActivity {

    private final String REMOTE_PORT_KEY = "remote_port_key";
    private final String REMOTE_IP_KEY = "remote_ip_key";
    private final String REMOTE_MESSAGE = "remote_message";

    private Button buttonSend;
    private EditText editRemoteIp;
    private EditText editRemotePort;
    private EditText editMessage;

    private UDPClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        editRemoteIp = findViewById(R.id.editRemoteIp);
        editRemotePort = findViewById(R.id.editRemotePort);
        editMessage = findViewById(R.id.editMessage);

        buttonSend = this.findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(btnSendClick);
    }

    View.OnClickListener btnSendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendMessage(
                    editRemoteIp.getText().toString(),
                    Integer.parseInt(editRemotePort.getText().toString()),
                    editMessage.getText().toString());
        }
    };

    @Override
    protected void onStop() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REMOTE_PORT_KEY, editRemotePort.getText().toString());
        editor.putString(REMOTE_IP_KEY, editRemoteIp.getText().toString());
        editor.putString(REMOTE_MESSAGE, editMessage.getText().toString());
        editor.commit();
        super.onStop();
    }

    @Override
    protected void onStart() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref != null) {
            editRemotePort.setText(sharedPref.getString(REMOTE_PORT_KEY, getString(R.string.default_remote_port)));
            editRemoteIp.setText(sharedPref.getString(REMOTE_IP_KEY, getString(R.string.ip_zero)));
            editMessage.setText(sharedPref.getString(REMOTE_MESSAGE, "a"));
        }
        super.onStart();
    }

    private void sendMessage(String remoteAddress, int remotePort, String message) {

        client = new UDPClient();
        client.setRemoteAddress(remoteAddress);
        client.setRemotePort(remotePort);
        client.setMessage(message);

        client.execute();


    }

    public class UDPClient extends AsyncTask<Void, Void, Void>
    {
        String remoteAddress;
        String message;
        int remotePort;

        public void setRemotePort(int remotePort) {
            this.remotePort = remotePort;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setRemoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
        }

        Socket socket;
        @Override
        protected Void doInBackground(Void... strings) {
            try {
                socket = new Socket(remoteAddress,remotePort);

                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                String messageStr = message;
                byte[] message = messageStr.getBytes();
                outputStream.write(message);
                outputStream.flush();
                outputStream.close();


                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}