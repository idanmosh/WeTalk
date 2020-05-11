package com.example.wetalk;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnTouchListener;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Adapters.MessageAdapter;
import com.example.wetalk.Calling.CallOutActivity;
import com.example.wetalk.Calling.Sinch;
import com.example.wetalk.Classes.AppDir;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.Message;
import com.example.wetalk.Permissions.Permissions;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
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

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ChatActivity extends AppCompatActivity implements OnBackPressedFragment, MessageAdapter.ListMessageClickListener {
    private final int IMAGE_REQUEST = 100;
    private final int DOCS_REQUEST = 200;

    private static final int RC_SETTINGS = 1255;
    private static final String DATE_FORMAT = "h:mm a dd MMMM yyyy";

    private static final String ContactPREFERENCES = "ContactsPrefs";
    private SharedPreferences mContactsSharedPreferences;
    private FloatingActionButton btnSendMessage, btnSendAudioMessage;
    private EditText mMessage;
    private Contact mContact;
    private Toolbar mToolbar;
    private TextView mUserName;
    private CircleImageView mUserProfileImage;
    private boolean permissionAccept=false;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, unreadMessages;
    private List<Message> messageList = new ArrayList<>();
    private HashMap<String, Message> messageMap = new HashMap<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String senderId;
    private LinearLayout revealLayout;
    private FloatingActionButton galleryBtn, docsBtn, audioBtn;
    private boolean hidden = true;
    private Animator anim;
    private MediaRecorder mMediaRecorder;
    private Uri sendImageUri;
    private Uri sendDocUri;
    private File audioFile;
    private static int pick = 0;
    private AppDir appDir;
    private TextView taskPercent;
    private ProgressBar mProgressBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        senderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Display screenSize = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screenSize.getSize(size);
        int width = size.x;

        revealLayout = findViewById(R.id.reveal_items);
        revealLayout.setVisibility(INVISIBLE);

        galleryBtn = findViewById(R.id.gallery_btn);
        galleryBtn.setOnClickListener(v -> pickImage());

        docsBtn = findViewById(R.id.docs_btn);
        docsBtn.setOnClickListener(v -> pickDoc());

        audioBtn = findViewById(R.id.audio_btn);
        audioBtn.setOnClickListener(v -> {
            Toast.makeText(this, "audio", Toast.LENGTH_SHORT).show();
            openActions();
        });

        mUserProfileImage = findViewById(R.id.contactImage);
        mUserName = findViewById(R.id.contactNameText);
        mMessage = findViewById(R.id.EditTextMessage);
        btnSendAudioMessage = findViewById(R.id.btnSendAudioMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendMessage.setVisibility(INVISIBLE);
        mMessage.setWidth((int) (width * 0.8));
        mMessage.addTextChangedListener(messageTextWatcher);
        mMessage.requestFocus();

        mContactsSharedPreferences = getSharedPreferences(ContactPREFERENCES, MODE_PRIVATE);

        initContactData();

        mContactsSharedPreferences.edit().putInt(mContact.getUserId() + "_unreadMessages", 0).apply();
        mUserName.setText(mContact.getName());
        loadImage();

        mToolbar = findViewById(R.id.toolbarContact);

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //item.setEnabled(CallListenerSign.sinchClient.isStarted());

        mToolbar.setNavigationOnClickListener(v -> sendUserToMainActivity());

        btnSendAudioMessage.setOnTouchListener(recordListener);
        btnSendMessage.setOnClickListener(v -> sendMessage());

        taskPercent = findViewById(R.id.send_audio_task_percent);
        mProgressBar = findViewById(R.id.audio_progressbar);

        rootRef.child(getString(R.string.USERS)).child(senderId).child("Messages")
                .child(mContact.getUserId()).addChildEventListener(newMessageListener);

        unreadMessages = rootRef.child(getString(R.string.USERS))
                .child(mContact.getUserId()).child("Messages")
                .child(senderId);
        Query query = unreadMessages.orderByChild("state").equalTo("unread");
        query.keepSynced(true);
        query.addValueEventListener(readSenderMessagesListener);

        setRecyclerView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private OnTouchListener recordListener = (v, event) -> {
        if (event.getAction() == ACTION_DOWN) {
            if (!Permissions.checkPermissions(this, Permissions.RECORD_AUDIO, Permissions.WRITE_STORAGE)) {
                MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.voice_message);
                player.start();
                startRecording();
            }
            else
                Permissions.audioPermissionDialog(this, ChatActivity.this);
        }

        else if (event.getAction() == ACTION_UP)
            stopRecording();

        return false;
    };

    private void startRecording()  {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (appDir == null)
            appDir = new AppDir();
        try {
            audioFile = File.createTempFile("file",".m4a", appDir.getAudioDir());
            mMediaRecorder.setOutputFile(audioFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioEncodingBitRate(96000);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaRecorder.start();
    }

    private void stopRecording() {
       try {
           mMediaRecorder.stop();
           mMediaRecorder.release();
           mMediaRecorder = null;
       }catch (RuntimeException e) {
           e.printStackTrace();
       }

        MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(ChatActivity.this)
                .setDescription("האם לשלוח את ההודעה הקולית ל-" + mContact.getName() + "?")
                .setIcon(R.drawable.ic_music_note)
                .setPositiveText(R.string.send)
                .onPositive((dialog1, which) -> sendAudioMessage())
                .setNegativeText(R.string.cancel)
                .onNegative((dialog12, which) -> audioFile.delete())
                .setCancelable(false);

        dialog.show();
    }

    private void sendAudioMessage() {
        mProgressBar.setVisibility(VISIBLE);
        taskPercent.setVisibility(VISIBLE);

        DatabaseReference senderMessageKeyRef = rootRef.child(getString(R.string.USERS))
                .child(senderId).child("Messages").child(mContact.getUserId()).push();
        String senderMessageKey = senderMessageKeyRef.getKey();
        DatabaseReference receiverMessageKeyRef = rootRef.child(getString(R.string.USERS))
                .child(mContact.getUserId()).child("Messages").child(senderId).child(Objects.requireNonNull(senderMessageKey));
        String date = getDate();

        if (appDir == null)
            appDir = new AppDir();
        File file = new File(appDir.getAudioDir(), senderMessageKey + ".m4a");
        audioFile.renameTo(file);
        audioFile.delete();
        addRecordingToMediaLibrary(file);

        if (file.exists()) {
            Uri uri = FileProvider.getUriForFile(getApplicationContext(),
                    getPackageName() + ".provider", file);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("Audio Files").child(file.getName());

            UploadTask senderUploadTask = storageRef.putFile(uri);

            senderUploadTask.addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getMetadata() != null) {
                    Task<Uri> senderResult = taskSnapshot.getStorage().getDownloadUrl();
                    senderResult.addOnSuccessListener(audioUri -> {

                        Map<String, Object> senderMessageTextBody = new HashMap<>();
                        senderMessageTextBody.put("message", audioUri.toString());
                        senderMessageTextBody.put("type", "voice");
                        senderMessageTextBody.put("from", senderId);
                        senderMessageTextBody.put("date", date);
                        senderMessageTextBody.put("state", "unread");

                        Map<String, Object> receiverMessageTextBody = new HashMap<>();
                        receiverMessageTextBody.put("message", audioUri.toString());
                        receiverMessageTextBody.put("type", "voice");
                        receiverMessageTextBody.put("from", senderId);
                        receiverMessageTextBody.put("date", date);

                        senderMessageKeyRef.updateChildren(senderMessageTextBody).addOnCompleteListener(task -> {
                            receiverMessageKeyRef.updateChildren(receiverMessageTextBody).addOnCompleteListener(task1 -> {
                                setContactState();
                                mProgressBar.setVisibility(GONE);
                                taskPercent.setVisibility(GONE);
                            });
                        });
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(GONE);
                taskPercent.setVisibility(GONE);
            }).addOnProgressListener(taskSnapshot -> {
                double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                mProgressBar.setProgress((int) p);
                taskPercent.setText((int) p + "%");
            });
        }
    }

    protected void addRecordingToMediaLibrary(File file) {
        //creating content values of size 4
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, file.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());

        //creating content resolver and storing it in the external content uri
        ContentResolver contentResolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        //sending broadcast message to scan the media file so that it can be available
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
    }

        private TextWatcher messageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if ((!TextUtils.isEmpty(s)) && (s.charAt(0) != ' ') && (s.charAt(0) != '\n')) {
                btnSendMessage.setVisibility(VISIBLE);
                btnSendAudioMessage.setVisibility(INVISIBLE);
            }
            else {
                btnSendMessage.setVisibility(INVISIBLE);
                btnSendAudioMessage.setVisibility(VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    @Override
    protected void onStart() {
        super.onStart();
        messageAdapter.notifyDataSetChanged();
    }

    private void pickDoc() {
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE))
            pickDocFromStorage();
        else {
            pick = 2;
            Permissions.ProfileImagePermissionDialog(this, ChatActivity.this);
        }
    }

    private void pickDocFromStorage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/pdf",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
        startActivityForResult(Intent.createChooser(intent,"שלח אל " + mContact.getName()), DOCS_REQUEST);
        openActions();
    }

    private void pickImage() {
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE))
            pickImageFromGallery();
        else{
            pick = 1;
            Permissions.ImagePermissionDialog(this, ChatActivity.this);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(Intent.createChooser(intent,"שלח אל " + mContact.getName()), IMAGE_REQUEST);
        openActions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQUEST) {
                sendImageUri = Objects.requireNonNull(data).getData();
                if (sendImageUri != null)
                    sentToSendImageFragment();
            }
            else if (requestCode == DOCS_REQUEST) {
                sendDocUri = Objects.requireNonNull(data).getData();
                if (sendDocUri != null)
                    sendToSendDocFragment();
            }
        }

    }

    private void sendToSendDocFragment() {
        FragmentManager fm = getSupportFragmentManager();
        SendDocFragment sendDoc = new SendDocFragment(mContact
                , getApplicationContext(), sendDocUri, this);
        setChatInvisible();
        fm.beginTransaction().setCustomAnimations(R.anim.md_styled_slide_up_normal, 0
                ,0, R.anim.md_styled_slide_down_normal).
                replace(R.id.activity_chat_layout, sendDoc).addToBackStack(null).commit();
    }

    private void setChatInvisible() {
        btnSendMessage.setVisibility(INVISIBLE);
        mMessage.setVisibility(INVISIBLE);
        userMessagesList.setVisibility(INVISIBLE);
    }

    private void setChatVisible() {
        btnSendMessage.setVisibility(VISIBLE);
        mMessage.setVisibility(VISIBLE);
        userMessagesList.setVisibility(VISIBLE);
    }

    private void sentToSendImageFragment() {
        FragmentManager fm = getSupportFragmentManager();
        SendImageFragment sendImage = new SendImageFragment(mContact
                , getApplicationContext(), sendImageUri, this);
        setChatInvisible();
        fm.beginTransaction().setCustomAnimations(R.anim.md_styled_slide_up_normal, 0
        ,0, R.anim.md_styled_slide_down_normal).
                replace(R.id.activity_chat_layout, sendImage).addToBackStack(null).commit();
    }

    private void setRecyclerView() {
        messageAdapter = new MessageAdapter(messageList, mContact, this, getApplicationContext());
        userMessagesList = findViewById(R.id.messages);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            onBackPressedFragment();
        else
            sendUserToMainActivity();
    }

    private void sendUserToMainActivity() {
        unreadMessages.removeEventListener(readSenderMessagesListener);
        Intent MainIntent = new Intent(ChatActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        overridePendingTransition(R.anim.slide_down, R.anim.slide_down);
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
            Message message = dataSnapshot.getValue(Message.class);
            Objects.requireNonNull(message).setMessageId(dataSnapshot.getKey());
            messageMap.put(message.getMessageId(), message);
            messageList.add(message);
            messageAdapter.notifyDataSetChanged();
            setContactState();
            userMessagesList.smoothScrollToPosition(Objects.requireNonNull(userMessagesList.getAdapter()).getItemCount());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message message = dataSnapshot.getValue(Message.class);
            Objects.requireNonNull(message).setMessageId(dataSnapshot.getKey());
            messageList.remove(messageMap.get(message.getMessageId()));
            messageList.add(message);
            messageMap.remove(dataSnapshot.getKey());
            messageMap.put(message.getMessageId(), message);
            messageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };

    private void sendMessage() {
        String messageText = mMessage.getText().toString();

        if ((!TextUtils.isEmpty(messageText)) && (messageText.charAt(0) != ' ') && (messageText.charAt(0) != '\n')) {
            DatabaseReference senderMessageKeyRef = rootRef.child(getString(R.string.USERS))
                    .child(senderId).child("Messages").child(mContact.getUserId()).push();
            String senderMessageKey = senderMessageKeyRef.getKey();
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

            senderMessageKeyRef.updateChildren(senderMessageTextBody).addOnCompleteListener(task -> {
                receiverMessageKeyRef.updateChildren(receiverMessageTextBody).addOnCompleteListener(task1 -> setContactState());
            });
            mMessage.setText("");
        }
    }

    private void setContactState() {
        mContactsSharedPreferences.edit().putBoolean(mContact.getUserId() + "_state", true).apply();
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
            case R.id.send_media:
                openActions();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void openActions() {
        int cx = revealLayout.getRight();
        int cy = revealLayout.getTop();
        makeEffect(revealLayout,cx,cy);
    }

    private void makeEffect(LinearLayout layout, int cx, int cy) {
        int radius = Math.max(layout.getWidth(), layout.getHeight());

        if (hidden) {
            anim = android.view.ViewAnimationUtils.createCircularReveal(layout, cx, cy, 0, radius);
            layout.setVisibility(VISIBLE);
            anim.start();
            hidden = false;
        } else {
            anim = android.view.ViewAnimationUtils.createCircularReveal(layout, cx, cy, radius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    layout.setVisibility(INVISIBLE);
                    hidden = true;
                }
            });
            anim.start();
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

                mContact = new Contact(userId,id,name,phone,status,image,null,0);
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

        if (requestCode == Permissions.IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (pick == 1)
                    pickImageFromGallery();
                else if (pick == 2)
                    pickDocFromStorage();
                else if (pick == 4)
                    messageAdapter.notifyDataSetChanged();
            }
            else
                Toast.makeText(this, "You can't get access to contacts, photos," +
                        " media, and files from your device.", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == Permissions.AUDIO_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "You can't get access phone microphone and write into phone storage," +
                        " you must confirm the permissions.", Toast.LENGTH_SHORT).show();
        }
        else
            EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);

        pick = 0;
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

    @Override
    public void onBackPressedFragment() {
        setChatVisible();
        getSupportFragmentManager().popBackStack();
    }

    private void checkStoragePermissions() {
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE)) {
            if(appDir == null)
                appDir = new AppDir();
        }
        else
            Permissions.ImagePermissionDialog(this, ChatActivity.this);
    }

    @Override
    public void onImageMessageClickPermissions() {
        pick = 4;
        checkStoragePermissions();
    }

    @Override
    public void showImage(Uri data) {
        Intent intent = new Intent(ACTION_VIEW);
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(data, "image/jpeg");
        startActivity(intent);
    }

    @Override
    public void showDoc(Uri data, String type) {
        Intent intent = new Intent(ACTION_VIEW);
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type);
        if (mimeType == null)
            mimeType = "application/*";
        intent.setDataAndType(data, mimeType);
        startActivity(intent);
    }
}


