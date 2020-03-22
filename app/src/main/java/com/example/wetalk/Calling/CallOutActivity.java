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
import android.view.WindowManager;
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

public class CallOutActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView callState;
    private ImageView rejectBtn, acceptBtn;
    private ImageView btnMute, btnHangUp, btnSpeaker;
    private ImageView UserprofileImage;
    private String callerId, contactUser;
    private DatabaseReference userRef;
    private TextView nameCallingCaontact;
    private String contactUserImage, contactUserName;
    private LinearLayout linearLayoutContactBeforAccept, linearLayoutButtom;
    float scale;
    private AudioManager audioManager;
    private Uri uri;
    private Ringtone ringtone;
    private Ringtone ring;
    private String getintentExtra;

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

        boolean isIncomming = getIntent().getBooleanExtra("incomming", true);
        if(isIncomming) {
            setDesplayIncominCall();
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ring = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ring.play();
        }
        else
            setDesplayOutCall();

    }
    private void setDesplayOutCall(){
        nameCallingCaontact.setText(contactUser);
        btnMute.setVisibility(View.GONE);
        btnSpeaker.setVisibility(View.GONE);
        btnHangUp.setVisibility(View.VISIBLE);
        acceptBtn.setVisibility(View.GONE);
        rejectBtn.setVisibility(View.GONE);

    }



    private void setDesplayIncominCall() {
        nameCallingCaontact.setText(contactUser);
        btnMute.setVisibility(View.GONE);
        btnSpeaker.setVisibility(View.GONE);
        btnHangUp.setVisibility(View.GONE);
        acceptBtn.setVisibility(View.VISIBLE);
        rejectBtn.setVisibility(View.VISIBLE);
        String contactInConversation = contactUser + " calling" ;
        callState.setText(contactInConversation);


    }

    private void initView() {
        contactUser = getIntent().getStringExtra("calling");
        if(contactUser==null){
            contactUser = getIntent().getStringExtra("incomingCall");
            CallListenerSign.call = CallListenerSign.getSinchClient().getCallClient().getCall(contactUser);
        }
        acceptBtn = findViewById(R.id.btnAcceptCall);
        rejectBtn = findViewById(R.id.btnRejectCall);
        btnHangUp = findViewById(R.id.btnHangUp);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        UserprofileImage = findViewById(R.id.profile_image_caller);
        nameCallingCaontact = findViewById(R.id.name_calling_caontact);
        callState = findViewById(R.id.callState);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        setProfileInfo();
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
        CallListenerSign.call.answer();
        if (ring!=null)ring.stop();
        setDesplayAfterAnswer();
    }

    private void setDesplayAfterAnswer() {
        linearLayoutContactBeforAccept.setGravity(Gravity.TOP);
        nameCallingCaontact.setText(CallListenerSign.call.getRemoteUserId());
        nameCallingCaontact.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        UserprofileImage.getLayoutParams().height = (int)(200*scale+0.5f);
        acceptBtn.setVisibility(View.GONE);
        rejectBtn.setVisibility(View.GONE);
        btnHangUp.setVisibility(View.VISIBLE);
        btnMute.setVisibility(View.VISIBLE);
        btnSpeaker.setVisibility(View.VISIBLE);
        String contactInConversation = contactUser + " in conversation" ;
        callState.setText(contactInConversation);

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
        if(uri!=null && ringtone!=null)ringtone.stop();
        CallListenerSign.call.hangup();
        finish();
    }

    private void setProfileInfo() {
        try {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(contactUser).exists()) {
                        contactUserImage = dataSnapshot.child(contactUser).child("image").getValue().toString();

                        contactUserName = dataSnapshot.child(contactUser).child("name").getValue().toString();
                        Glide.with(getApplicationContext()).load(contactUserImage)
                                .into(UserprofileImage);
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
