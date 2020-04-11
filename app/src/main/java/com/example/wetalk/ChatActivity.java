package com.example.wetalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Adapters.MessageAdapter;
import com.example.wetalk.Calling.CallOutActivity;
import com.example.wetalk.Calling.Sinch;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ChatActivity extends AppCompatActivity {
    private static final int RC_SETTINGS = 1255;
    private static final String DATE_FORMAT = "h:mm a dd MMMM yyyy";

    private FloatingActionButton btnSendMessage;
    private EditText mMessage;
    private Contact mContact;
    private Toolbar mToolbar;
    private TextView mUserName, mUserStatus;
    private CircleImageView mUserProfileImage;
    private MenuItem item;
    private boolean permissionAccept=false;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private final List<Messages> messageList = new ArrayList<>();
    private final HashMap<String, Messages> messageMap = new HashMap<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String senderId;
    private String senderMessageKey;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        senderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mUserProfileImage = findViewById(R.id.contactImage);
        mUserName = findViewById(R.id.contactNameText);
        mMessage = findViewById(R.id.EditTextMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        mMessage.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initContactData();

        mUserName.setText(mContact.getName());
        loadImage();

        mToolbar = findViewById(R.id.toolbarContact);

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //item.setEnabled(CallListenerSign.sinchClient.isStarted());

        mToolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        btnSendMessage.setOnClickListener(v -> {
            sendMessage();
        });

        rootRef.child(getString(R.string.USERS)).child(senderId).child("Messages")
                .child(mContact.getUserId()).addChildEventListener(newMessageListener);

        DatabaseReference unreadMessages = rootRef.child(getString(R.string.USERS))
                .child(mContact.getUserId()).child("Messages")
                .child(senderId);
        Query query = unreadMessages.orderByChild("state").equalTo("unread");
        query.addValueEventListener(readSenderMessagesListener);

        messageAdapter = new MessageAdapter(messageList, getApplicationContext(), mContact);
        userMessagesList = findViewById(R.id.messages);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private ValueEventListener readSenderMessagesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (snapshot.exists() && snapshot.hasChild("state")) {
                    String messageState = snapshot.child("state").getValue().toString();
                    if (messageState.equals("unread")) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("state", "read");
                        rootRef.child(getString(R.string.USERS))
                                .child(mContact.getUserId())
                                .child("Messages")
                                .child(senderId)
                                .child(snapshot.getKey().toString())
                                .updateChildren(map).addOnCompleteListener(setStateMessageToReadListener);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            databaseError.getMessage();
        }
    };

    private OnCompleteListener setStateMessageToReadListener = task -> {};

    private ChildEventListener newMessageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Messages message = dataSnapshot.getValue(Messages.class);
            Objects.requireNonNull(message).setMessageId(dataSnapshot.getKey());
            messageMap.put(message.getMessageId(), message);
            messageList.add(message);
            messageAdapter.notifyDataSetChanged();
            userMessagesList.smoothScrollToPosition(Objects.requireNonNull(userMessagesList.getAdapter()).getItemCount());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Messages message = dataSnapshot.getValue(Messages.class);
            Objects.requireNonNull(message).setMessageId(dataSnapshot.getKey());
            messageList.remove(messageMap.get(message.getMessageId()));
            messageList.add(message);
            messageMap.remove(dataSnapshot.getKey());
            messageMap.put(message.getMessageId(), message);
            messageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void sendMessage() {
        String messageText = mMessage.getText().toString();

        if ((!TextUtils.isEmpty(messageText)) && (messageText.charAt(0) != ' ') && (messageText.charAt(0) != '\n')) {
            DatabaseReference senderMessageKeyRef = rootRef.child(getString(R.string.USERS))
                    .child(senderId).child("Messages").child(mContact.getUserId()).push();
            senderMessageKey = senderMessageKeyRef.getKey();
            DatabaseReference receiverMessageKeyRef = rootRef.child(getString(R.string.USERS))
                    .child(mContact.getUserId()).child("Messages").child(senderId).child(Objects.requireNonNull(senderMessageKey));
            String date = getDate();

            Map<String, Object> senderMessageTextBody = new HashMap<>();
            senderMessageTextBody.put("message", messageText);
            senderMessageTextBody.put("type", "text");
            senderMessageTextBody.put("from", senderId);
            senderMessageTextBody.put("date", date);
            senderMessageTextBody.put("state", "unread");

            Map<String, Object> receiverMessageTextBody = new HashMap<>();
            receiverMessageTextBody.put("message", messageText);
            receiverMessageTextBody.put("type", "text");
            receiverMessageTextBody.put("from", senderId);
            receiverMessageTextBody.put("date", date);

            senderMessageKeyRef.updateChildren(senderMessageTextBody).addOnCompleteListener(task -> {});
            receiverMessageKeyRef.updateChildren(receiverMessageTextBody).addOnCompleteListener(task -> {});
            mMessage.setText("");
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date today = Calendar.getInstance().getTime();
        String date = sdf.format(today);
        return date;
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
                sendToCreateCallVideoToContact();
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
        RequestPermissions();
        if (permissionAccept) {
            if (ifSinchClientNull()) return;
            Sinch.call = Sinch.sinchClient.getCallClient().callUser(mContact.getUserId());
            Sinch.call.addCallListener(new Sinch.SinchCallListener());
            sendToCallingActivity();
        }
    }

    private void sendToCreateCallVideoToContact(){
        RequestPermissions();
        if (permissionAccept) {
            if (ifSinchClientNull()) return;
            Sinch.call = Sinch.sinchClient.getCallClient().callUserVideo(mContact.getUserId());
            Sinch.call.addCallListener(new Sinch.SinchCallListener());
            sendToCallingActivity();
        }
    }

    private Boolean ifSinchClientNull(){
        if (Sinch.sinchClient==null){
            Toast.makeText(this, "Sinch Client not connected", Toast.LENGTH_SHORT).show();
            Sinch sinchListnerActivity = new Sinch(this);
            Toast.makeText(this, "Try again or restart the app", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void sendToCallingActivity(){
        Intent callscreen = new Intent(this, CallOutActivity.class);
        callscreen.putExtra("calling", mContact.getUserId());
        callscreen.putExtra("callingName", mContact.getName());
        callscreen.putExtra("callingImage", mContact.getImage());
        callscreen.putExtra("incomming", false);
        callscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callscreen);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);

    }
    @AfterPermissionGranted(RC_SETTINGS)
    private void RequestPermissions() {

        String[] perm = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perm)) {
            permissionAccept = true;
        }

        else {
            EasyPermissions.requestPermissions(this, "this app need to access your camera and mic", RC_SETTINGS, perm);
            permissionAccept = false;
        }
    }

}


