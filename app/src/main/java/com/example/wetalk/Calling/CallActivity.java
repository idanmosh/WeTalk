package com.example.wetalk.Calling;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.wetalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CallActivity extends AppCompatActivity {

    private TextView callState;
    private ImageView rejectBtn , acceptBtn;
    private ImageView btnMute , btnHangUp, btnSpeaker;
    private ImageView UserprofileImage;
    private String callerId, contactUser;
    private Boolean inCall = false, outCall=false;
    private DatabaseReference userRef;
    private TextView nameCallingCaontact;
    private String contactUserImage,contactUserName;
    private LinearLayout linearLayoutContactBeforAccept,linearLayoutButtom;
    float scale;
    private AudioManager audioManager;
    private Uri uri;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);



        Intent intent = getIntent();

        linearLayoutContactBeforAccept = findViewById(R.id.linearLayoutContact);
        acceptBtn = findViewById(R.id.btnAcceptCall);
        rejectBtn =  findViewById(R.id.btnRejectCall);
        btnHangUp = findViewById(R.id.btnHangUp);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        UserprofileImage = findViewById(R.id.profile_image_caller);
        nameCallingCaontact = findViewById(R.id.name_calling_caontact);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        callState =  findViewById(R.id.callState);



        if(intent.getStringExtra("AcceptCallIdOfCaller")!=null){
            contactUser = intent.getStringExtra("AcceptCallIdOfCaller");
            ScreenSetup();
        }

        if(intent.getStringExtra("incomingCall")!=null){
            contactUser = intent.getStringExtra("incomingCall");
            inCall =true;
            acceptBtn.setVisibility(View.VISIBLE);

            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(),uri);
            ringtone.play();
        }
        if(intent.getStringExtra("outCall")!=null){
            acceptBtn.setVisibility(View.GONE);
            contactUser = intent.getStringExtra("outCall");
            outCall = true;
        }
        getAndsetProfileInfo();

/*
        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });*/

        btnHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setSpeakerphoneOn(false);
                rejectBtn.callOnClick();
            }
        });
        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(uri!=null && ringtone!=null)
                {
                    ringtone.stop();
                }
                String endCalling = "endCall";
                Intent endCallIntent = new Intent(CallActivity.this, CallListenerActivity.class);
                endCallIntent.putExtra("endCall", endCalling);
                startActivity(endCallIntent);
                finish();

            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String acceptCalling = "acceptCalling";
                Intent acceptCallingIntent = new Intent(CallActivity.this,CallListenerActivity.class);
                acceptCallingIntent.putExtra("acceptCalling",acceptCalling);
                startActivity(acceptCallingIntent);
                if(uri!=null && ringtone!=null)
                {
                    ringtone.stop();
                }

            }
        });

    }

    private void ScreenSetup() {
        linearLayoutContactBeforAccept.setGravity(Gravity.TOP);
        nameCallingCaontact.setText(contactUserName);
        nameCallingCaontact.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        UserprofileImage.getLayoutParams().height = (int)(200*scale+0.5f);
        acceptBtn.setVisibility(View.GONE);
        rejectBtn.setVisibility(View.GONE);
        btnHangUp.setVisibility(View.VISIBLE);
        btnMute.setVisibility(View.VISIBLE);
        btnSpeaker.setVisibility(View.VISIBLE);

    }

    private void getAndsetProfileInfo() {
        try {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(contactUser).exists()) {
                        contactUserImage = dataSnapshot.child(contactUser).child("image").getValue().toString();

                        contactUserName = dataSnapshot.child(contactUser).child("name").getValue().toString();
                        Glide.with(getApplicationContext()).load(contactUserImage)
                                .into(UserprofileImage);
                        if(inCall)
                        {
                            nameCallingCaontact.setText(contactUserName + " calling");
                        }
                        if(outCall)
                        {
                            nameCallingCaontact.setText("calling to " +contactUserName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (Exception e){
            Toast.makeText(this, "error in Database Reference", Toast.LENGTH_SHORT).show();
        }
    }


}

