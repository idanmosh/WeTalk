package com.example.wetalk.Calling;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.wetalk.R;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;


public class CallOutActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView rejectBtn, acceptBtn;
    private ImageView btnMute, btnHangUp, btnSpeaker;
    private ImageView UserprofileImage;
    private String contactName;
    private DatabaseReference userRef;
    private TextView nameCallingCaontact;
    public static TextView state;
    private LinearLayout linearLayoutContactBeforAccept;
    float scale;
    private AudioManager audioManager;
    private Uri uri;
    private Ringtone ring;
    private boolean isIncomming;
    private String contactInCallingUserImage,contactInCallingUserName;
    private Call call;
    private Boolean callActive = false;
    View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_call_out);

        initView();
        call = Sinch.sinchClient.getCallClient().getCall(getIntent().getStringExtra("callId"));

        isIncomming = getIntent().getBooleanExtra("incomming", true);
        if(isIncomming) {
            setDesplayIncominCall();
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ring = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ring.play();

        }
        else
            setDesplayOutCall();

    }


    private void finishIfEnd(){
        
    }


    private void setDesplayOutCall(){
        btnMute.setVisibility(View.GONE);
        btnSpeaker.setVisibility(View.GONE);
        btnHangUp.setVisibility(View.VISIBLE);
        acceptBtn.setVisibility(View.GONE);
        rejectBtn.setVisibility(View.GONE);
        contactName = getIntent().getStringExtra("callingName");
        String contactImage = getIntent().getStringExtra("callingImage");
        if (contactImage!=null){
            Glide.with(getApplicationContext()).load(contactImage)
                    .into(UserprofileImage);
        }
        if (contactName!=null) {
            String contactInConversation = "Call to "+ contactName;
            nameCallingCaontact.setText(contactInConversation);
        }
        else
            nameCallingCaontact.setText("Calling");

    }



    private void setDesplayIncominCall() {
        btnMute.setVisibility(View.GONE);
        btnSpeaker.setVisibility(View.GONE);
        btnHangUp.setVisibility(View.GONE);
        acceptBtn.setVisibility(View.VISIBLE);
        rejectBtn.setVisibility(View.VISIBLE);
        contactName = getIntent().getStringExtra("incomingCall");
        setNameAndPhoto();

    }

    private void initView() {

        acceptBtn = findViewById(R.id.btnAcceptCall);
        rejectBtn = findViewById(R.id.btnRejectCall);
        btnHangUp = findViewById(R.id.btnHangUp);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        acceptBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);
        btnHangUp.setOnClickListener(this);
        btnMute.setOnClickListener(this);
        btnSpeaker.setOnClickListener(this);
        linearLayoutContactBeforAccept = findViewById(R.id.linearLayoutContact);
        UserprofileImage = findViewById(R.id.profile_image_caller);
        nameCallingCaontact = findViewById(R.id.name_calling_caontact);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        state = findViewById(R.id.state);
        ifStateChangeFinshIntent();
    }

    private void ifStateChangeFinshIntent() {
        state.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                finish();
            }
        });
    }

    private void setNameAndPhoto() {
        try {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(contactName).exists()) {
                        contactInCallingUserImage = dataSnapshot.child(contactName).child("image").getValue().toString();

                        contactInCallingUserName = dataSnapshot.child(contactName).child("name").getValue().toString();
                        contactName = contactInCallingUserName;
                        Glide.with(getApplicationContext()).load(contactInCallingUserImage)
                                .into(UserprofileImage);

                        nameCallingCaontact.setText(contactInCallingUserName + " calling");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (Exception e){
            nameCallingCaontact.setText("No Name" + "ERROR IN DATA");
            Toast.makeText(this, "error in Database Reference", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAcceptCall:
                btnAcceptClick();
                break;
            case R.id.btnRejectCall:
            case R.id.btnHangUp:
                btnHangUpClick();
                break;
            case R.id.btnMute:
                btnMuteClick();
                break;
            case R.id.btnSpeaker:
                btnSpeakerClick();
                break;

        }
    }
    public void btnAcceptClick() {
        call.answer();
        if (ring!=null)ring.stop();
        setDesplayAfterAnswer();
    }

    private void setDesplayAfterAnswer() {
        linearLayoutContactBeforAccept.setGravity(Gravity.TOP);
        nameCallingCaontact.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        UserprofileImage.getLayoutParams().height = (int)(200*scale+0.5f);
        acceptBtn.setVisibility(View.GONE);
        rejectBtn.setVisibility(View.GONE);
        btnHangUp.setVisibility(View.VISIBLE);
        btnMute.setVisibility(View.VISIBLE);
        btnSpeaker.setVisibility(View.VISIBLE);
        String contactInConversation = contactName + " in conversation" ;
        nameCallingCaontact.setText(contactInConversation);
    }

    public void btnSpeakerClick(){
        if (!audioManager.isSpeakerphoneOn())
        {
            Glide.with(getApplicationContext()).load(R.drawable.speaker_press).into(btnSpeaker);
            audioManager.setSpeakerphoneOn(true);
        }
        else
        {
            audioManager.setSpeakerphoneOn(false);
            Glide.with(getApplicationContext()).load(R.drawable.speaker).into(btnSpeaker);
        }
    }

    public void btnMuteClick(){
        if (!audioManager.isMicrophoneMute())
        {
            Glide.with(getApplicationContext()).load(R.drawable.mute_press).into(btnMute);
            audioManager.setMicrophoneMute(true);
        }
        else
        {
            audioManager.setMicrophoneMute(false);
            Glide.with(getApplicationContext()).load(R.drawable.mute).into(btnMute);
        }
    }

    public void btnHangUpClick(){
        audioManager.setSpeakerphoneOn(false);
        if(uri!=null && ring!=null)ring.stop();
        Sinch.call.hangup();
        Sinch.call = null;
        finish();
    }
}
