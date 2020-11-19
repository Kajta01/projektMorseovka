package com.example.buzzer01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Socket extends AppCompatActivity {

    private final String REMOTE_PORT_KEY = "remote_port_key";
    private final String REMOTE_IP_KEY = "remote_ip_key";

    private Button buttonSend;
    private EditText editRemoteIp;
    private EditText editRemotePort;
    private EditText editMessage;

    private MyDatagramSender myDatagramSender = null;

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
        editor.putString(REMOTE_PORT_KEY, editRemotePort.getText().toString());
        editor.putString(REMOTE_IP_KEY, editRemoteIp.getText().toString());
        editor.commit();
        super.onStop();
    }

    @Override
    protected void onStart() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref != null) {
            editRemotePort.setText(sharedPref.getString(REMOTE_PORT_KEY, getString(R.string.default_remote_port)));
            editRemoteIp.setText(sharedPref.getString(REMOTE_IP_KEY, getString(R.string.ip_zero)));
        }
        super.onStart();
    }

    private void sendMessage(String remoteAddress, int remotePort, String message) {
        MyDatagramSender sender = new MyDatagramSender();
        sender.setRemoteAddress(remoteAddress);
        sender.setRemotePort(remotePort);
        sender.setMessage(message);
        sender.start();

    }

    private class MyDatagramSender extends Thread{
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

        @Override
        public void run() {
            try {
                DatagramSocket client_socket = new DatagramSocket(remotePort);
                InetAddress IPAddress = InetAddress.getByName(remoteAddress);
                byte[] send_data = message.getBytes();

                DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, IPAddress, remotePort);
                //client_socket.setBroadcast(true);
                client_socket.send(send_packet);
                client_socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}