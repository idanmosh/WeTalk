package com.example.wetalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    private String massageReceiverId, messageReceiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        massageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();

        Toast.makeText(ChatActivity.this,massageReceiverId,Toast.LENGTH_LONG).show();
        Toast.makeText(ChatActivity.this,messageReceiverName,Toast.LENGTH_LONG).show();


    }
}
