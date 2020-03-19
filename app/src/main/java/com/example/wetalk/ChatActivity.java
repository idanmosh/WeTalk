package com.example.wetalk;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.Classes.Contact;

public class ChatActivity extends AppCompatActivity {

    private Contact mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initContactData();


    }

    private void initContactData() {
        Uri intentData = getIntent().getData();
        if (intentData != null) {
            Cursor c = getContentResolver().query(intentData, null, null, null, null);
            assert c != null;
            if (c.moveToNext()) {
                String id = c.getString(c.getColumnIndex(ContactsContract.Data.DATA7));
                String phone = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                String name = c.getString(c.getColumnIndex(ContactsContract.Data.DATA2));
                String userId = c.getString(c.getColumnIndex(ContactsContract.Data.DATA4));
                String image = c.getString(c.getColumnIndex(ContactsContract.Data.DATA5));
                String status = c.getString(c.getColumnIndex(ContactsContract.Data.DATA6));

                mContact = new Contact(userId,id,name,phone,status,image);
            }

            c.close();
        }
        else
            mContact = (Contact) getIntent().getSerializableExtra("CONTACT");

    }
}
