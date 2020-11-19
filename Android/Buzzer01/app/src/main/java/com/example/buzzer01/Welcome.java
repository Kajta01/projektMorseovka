package com.example.buzzer01;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;

public class Welcome extends AppCompatActivity {
    private Button buttonDecodeBuzzer;
    private Button buttonSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        buttonDecodeBuzzer = this.findViewById(R.id.buttonDecodeBuzzer);
        buttonDecodeBuzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this, Decode.class);
                startActivityForResult(intent,1);
            }
        });
        buttonSocket = this.findViewById(R.id.buttonSocket);
        buttonSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this, Socket.class);
                startActivityForResult(intent,1);
            }
        });

    }
}