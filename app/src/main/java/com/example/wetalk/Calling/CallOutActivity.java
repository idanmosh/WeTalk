package com.example.wetalk.Calling;
import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;
import com.example.wetalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;

public class CallOutActivity extends AppCompatActivity implements View.OnClickListener{
    private ConstraintLayout constraintLayoutCallRing,constraintLayoutCallAccept,constraintLayoutVideoCall;

    private ImageView rejectBtn, acceptBtn;
    private ImageView rejectBtnAfter, acceptBtnAfter;

    private ImageView btnMute, btnSpeaker,Btnpause,btnRejectCallVideo,btnMuteVideo;
    private ImageView UserprofileImage,UserprofileImageAfterAccept;

    private String contactName;
    private String contactInCallingUserImage,contactInCallingUserName;

    public static TextView state,incomingVideoCall;
    private TextView nameCallingCaontact,nameCallingCaontactAfterAccept;

    private DatabaseReference userRef;
    private AudioManager audioManager;
    private Uri notification;
    private Ringtone ring;
    private boolean isIncomming,callPause=true,permissionsBool = false;
    private Call call;
    private Chronometer chronometer;


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
        call = Sinch.sinchClient.getCallClient().getCall(getIntent().getStringExtra("callId"));
        initView();
        Sinch.noPermissions = true;
        call = Sinch.sinchClient.getCallClient().getCall(getIntent().getStringExtra("callId"));

        isIncomming = getIntent().getBooleanExtra("incomming", true);
        if (isIncomming) {
            setDesplayIncominCall();
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ring = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ring.play();

        } else
            setDesplayOutCall();
    }



    private void initView() {

        state = findViewById(R.id.state);
        constraintLayoutCallRing = findViewById(R.id.constraintLayout_CallRing);
        constraintLayoutCallAccept = findViewById(R.id.constraint_Layout_CallAccept);
        constraintLayoutVideoCall = findViewById(R.id.constraint_Layout_Video_Call);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        ifStateChangeFinshIntent();
        if (Sinch.call.getDetails().isVideoOffered()) {
            incomingVideoCall = findViewById(R.id.typeOfCall);
            incomingVideoCall.setVisibility(View.VISIBLE);
        }
    }

    private void setDesplayOutCall(){
        setDesplayBeforeResponse();
        acceptBtn.setVisibility(View.GONE);
        setImageOutCall();
    }

    private void setDesplayIncominCall() {
        setDesplayBeforeResponse();
        contactName = getIntent().getStringExtra("incomingCall");
        setNameAndPhoto();
    }

    private void setDesplayBeforeResponse(){
        constraintLayoutCallRing.setVisibility(View.VISIBLE);
        constraintLayoutCallAccept.setVisibility(View.GONE);
        constraintLayoutVideoCall.setVisibility(View.GONE);
        UserprofileImage = findViewById(R.id.profile_image_wait_caller);
        UserprofileImageAfterAccept = findViewById(R.id.profile_image_caller);

        nameCallingCaontact = findViewById(R.id.name_calling_wait_caontact);
        nameCallingCaontactAfterAccept = findViewById(R.id.name_calling_contact);

        chronometer = findViewById(R.id.chronometer);
        acceptBtn = findViewById(R.id.btnAcceptCall);
        rejectBtn = findViewById(R.id.btnRejectCall_wait);


        acceptBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);
    }

    private void setDesplayAfterAnswer(Boolean bool) {
        constraintLayoutCallAccept.setVisibility(View.VISIBLE);
        constraintLayoutCallRing.setVisibility(View.GONE);
        constraintLayoutVideoCall.setVisibility(View.GONE);


        String contactInConversation = contactName + " in conversation" ;
        nameCallingCaontactAfterAccept.setText(contactInConversation);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        btnSpeaker.setOnClickListener(this);
        rejectBtnAfter = findViewById(R.id.btnRejectCall);
        rejectBtnAfter.setOnClickListener(this);
        btnMute = findViewById(R.id.btnMute);
        btnMute.setOnClickListener(this);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

    }

    private void ifStateChangeFinshIntent() {
        state.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("final")){
                    finish();
                }
                else if(s.toString().equals("start")){
                    setDesplayAfterAnswer(true);
                }
                else if(s.toString().equals("Video")){
                    VideoCalling();
                }
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
                        Glide.with(getApplicationContext()).load(contactInCallingUserImage)
                                .into(UserprofileImageAfterAccept);


                        nameCallingCaontact.setText(contactInCallingUserName + " calling");
                        nameCallingCaontactAfterAccept.setText(contactInCallingUserName + " calling");
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

    private void setImageOutCall(){
        contactName = getIntent().getStringExtra("callingName");
        String contactImage = getIntent().getStringExtra("callingImage");
        if (contactImage!=null){
            Glide.with(getApplicationContext()).load(contactImage)
                    .into(UserprofileImage);
            Glide.with(getApplicationContext()).load(contactImage)
                    .into(UserprofileImageAfterAccept);
        }
        if (contactName!=null) {
            String contactInConversation = "Call to "+ contactName;
            nameCallingCaontact.setText(contactInConversation);
            nameCallingCaontactAfterAccept.setText(contactInConversation);

        }
        else{
            nameCallingCaontact.setText("Calling");
            nameCallingCaontactAfterAccept.setText("Calling");
        }

    }

    public void VideoCalling() {
        constraintLayoutVideoCall.setVisibility(View.VISIBLE);
        constraintLayoutCallAccept.setVisibility(View.GONE);
        constraintLayoutCallRing.setVisibility(View.GONE);
        audioManager.setSpeakerphoneOn(true);

        Btnpause = findViewById(R.id.Btnpause);
        Btnpause.setOnClickListener(this);
        btnRejectCallVideo = findViewById(R.id.btnRejectCallVideo);
        btnRejectCallVideo.setOnClickListener(this);
        btnMuteVideo = findViewById(R.id.btnMuteVideo);
        btnMuteVideo.setOnClickListener(this);
        final VideoController vc = Sinch.sinchClient.getVideoController();
        if (vc != null) {
            LinearLayout localView = findViewById(R.id.call_activity_view);
            localView.addView(vc.getLocalView());
            localView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vc.toggleCaptureDevicePosition();
                }
            });
            RelativeLayout view = findViewById(R.id.linearLayoutVideo);
            vc.setResizeBehaviour(VideoScalingType.ASPECT_FILL);
            view.addView(vc.getRemoteView());
            Sinch.mVideoViewsAdded = true;
        }
    }

    private void removeVideo () {
        VideoController vc = Sinch.sinchClient.getVideoController();
        if (vc != null) {
            RelativeLayout view = findViewById(R.id.linearLayoutVideo);
            view.removeView(vc.getRemoteView());

            LinearLayout localView = findViewById(R.id.call_activity_view);
            localView.removeView(vc.getLocalView());
            Sinch.mVideoViewsAdded = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAcceptCall:
                btnAcceptClick();
                break;
            case R.id.btnRejectCall:
            case R.id.btnRejectCall_wait:
            case R.id.btnRejectCallVideo:
                btnHangUpClick();
                break;
            case R.id.btnMute:
            case R.id.btnMuteVideo:
                btnMuteClick();
                break;
            case R.id.btnSpeaker:
                btnSpeakerClick();
                break;
            case R.id.Btnpause:
                BtnpauseClick();
                break;

        }
    }
    public void BtnpauseClick(){
        if (callPause) {
            call.pauseVideo();
            callPause = false;
            Glide.with(getApplicationContext()).load(R.drawable.ic_videocam_black).into(Btnpause);
        }
        else
        {
            call.resumeVideo();
            callPause = true;
            Glide.with(getApplicationContext()).load(R.drawable.ic_videocam_off).into(Btnpause);
        }


    }
    public void btnAcceptClick() {
        call.answer();
        if (ring!=null)ring.stop();
        if (!Sinch.call.getDetails().isVideoOffered())
            setDesplayAfterAnswer(false);
    }

    public void btnSpeakerClick(){
        if (!audioManager.isSpeakerphoneOn())
        {
            Glide.with(getApplicationContext()).load(R.drawable.ic_volume_off).into(btnSpeaker);
            audioManager.setSpeakerphoneOn(true);
        }
        else
        {
            audioManager.setSpeakerphoneOn(false);
            Glide.with(getApplicationContext()).load(R.drawable.ic_volume_up).into(btnSpeaker);
        }
    }

    public void btnMuteClick(){
        if (!audioManager.isMicrophoneMute())
        {
            Glide.with(getApplicationContext()).load(R.drawable.ic_mic_off).into(btnMute);
            Glide.with(getApplicationContext()).load(R.drawable.ic_mic_off).into(btnMuteVideo);

            audioManager.setMicrophoneMute(true);
        }
        else
        {
            audioManager.setMicrophoneMute(false);
            Glide.with(getApplicationContext()).load(R.drawable.ic_mic_black).into(btnMuteVideo);
            Glide.with(getApplicationContext()).load(R.drawable.ic_mic_black).into(btnMute);

        }
    }

    public void btnHangUpClick(){
        if (Sinch.call.getDetails().isVideoOffered()){
            removeVideo();
        }
        audioManager.setSpeakerphoneOn(false);
        if(notification!=null && ring!=null){ring.stop();}
        Sinch.call.hangup();
        Sinch.call = null;
        finish();
    }
}
