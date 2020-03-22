package com.example.wetalk;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Calling.CallListenerSign;
import com.example.wetalk.Calling.CallOutActivity;
import com.example.wetalk.Calling.SinchCallListener;
import com.example.wetalk.Classes.Contact;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.calling.Call;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ChatActivity extends AppCompatActivity {

    private Contact mContact;
    private Toolbar mToolbar;
    private TextView mUserName, mUserStatus;
    private CircleImageView mUserProfileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mUserProfileImage = findViewById(R.id.contactImage);
        mUserName = findViewById(R.id.contactNameText);

        initContactData();

        mUserName.setText(mContact.getName());
        loadImage();

        mToolbar = findViewById(R.id.toolbarContact);

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mToolbar.setNavigationOnClickListener(v -> {
            finish();
        });





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.call_contact:
                sendToCreateCallToContact();
                return true;
            case R.id.video_contact:
               // deleteUserData();
                return true;
            case R.id.show_contact:
                //
                return true;
            case R.id.find:
               // sendUserToFindFriendsActivity();
            default:
                return super.onContextItemSelected(item);
        }
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

    private void loadImage() {
        if (mContact.getImage() != null) {
            Glide.with(getApplicationContext()).asBitmap().load(mContact.getImage()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    mUserProfileImage.setImageBitmap(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    Glide.with(getApplicationContext()).load(placeholder).into(mUserProfileImage);
                }
            });
        }
    }


    private void sendToCreateCallToContact(){
        //CallListenerSign callListenerSign = new CallListenerSign();
        if(CallListenerSign.sinchClient==null){
            Toast.makeText(this, "Sinch Client not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        CallListenerSign.call = CallListenerSign.sinchClient.getCallClient().callUser(mContact.getUserId());
        CallListenerSign.call.addCallListener(new SinchCallListener());
        Intent callscreen = new Intent(this, CallOutActivity.class);
        callscreen.putExtra("calling", mContact.getUserId());
        callscreen.putExtra("incomming", false);
        callscreen.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(callscreen);
    }
}
