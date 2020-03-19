package com.example.wetalk;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private String id;
    private String phone;
    private String name;
    private String image;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initContactData();
    }

    private void initContactData() {
        Uri intentData = getIntent().getData();
        if (!Uri.EMPTY.equals(intentData)) {
            assert intentData != null;
            Cursor c = getContentResolver().query(intentData, null, null, null, null);
            assert c != null;
            if (c.moveToNext()) {
                id = c.getString(c.getColumnIndex(ContactsContract.Data.DATA7));
                phone = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                name = c.getString(c.getColumnIndex(ContactsContract.Data.DATA2));
                image = c.getString(c.getColumnIndex(ContactsContract.Data.DATA5));
                status = c.getString(c.getColumnIndex(ContactsContract.Data.DATA6));
            }

            c.close();
        }
    }
}
