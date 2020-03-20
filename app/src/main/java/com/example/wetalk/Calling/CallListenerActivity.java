package com.example.wetalk.Calling;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.wetalk.R;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class CallListenerActivity extends AppCompatActivity implements CallFragment.onBtnClickListener{


    private static final String APP_KEY = "56095560-a311-4128-b5b7-087f14834b93";
    private static final String APP_SECRET = "4zvuV9c/6UC4LZisgAJG2A==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    /*private static final int btnMute = 2131230862;
    private static final int btnRejectCall = 2131230863;
    private static final int btnSpeaker = 2131230864;
    private static final int btnAcceptCall = 2131230859;
    private static final int btnHangUp = 2131230861;*/


    private static Call call;
    private static SinchClient sinchClient;
    private String currentUser;
    private String outCall;
    private AudioManager audioManager;
    private Boolean callEnd=false;
    private  static CallFragment callFragment;
    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_listener);

        callFragment = new CallFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.Frame_activity_call_listener_view ,callFragment)
                .commit();


        Intent intent = getIntent();
        try{
            if(intent.getStringExtra("currentUser")!=null )
            {
                currentUser = intent.getStringExtra("currentUser");

                sinchClient = Sinch.getSinchClientBuilder()
                        .context(this)
                        .userId(currentUser)
                        .applicationKey(APP_KEY)
                        .applicationSecret(APP_SECRET)
                        .environmentHost(ENVIRONMENT)
                        .build();

                sinchClient.setSupportCalling(true);
                sinchClient.startListeningOnActiveConnection();
                sinchClient.start();

                sinchClient.getCallClient().addCallClientListener(new CallListenerActivity.SinchCallClientListener());
            }

            if(intent.getStringExtra("outCall")!=null )
            {
                outCall = intent.getStringExtra("outCall");
                call = sinchClient.getCallClient().callUser(outCall);
                call.addCallListener(new CallListenerActivity.SinchCallListener());
                openFragment();

                //Intent callActivity = new Intent(CallListenerActivity.this,CallActivity.class);
                //callActivity.putExtra("outCall",outCall);
                //startActivity(callActivity);
            }
/*
            if(intent.getStringExtra("endCall")!=null && !callEnd){
                call.hangup();
                call.addCallListener(new CallListenerActivity.SinchCallListener());
                call = null;
                finish();
            }*/
/*
            if(intent.getStringExtra("acceptCalling")!=null){
                call.answer();
                call.addCallListener(new CallListenerActivity.SinchCallListener());
                finish();
            }*/
        }

        catch (Exception e){
            Toast.makeText(this, "Class error CallListenerActivity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBtnClick(View position) {
        if(getApplicationContext().getResources().getResourceEntryName(position.getId())=="btnHangUp")
            btnHangUp();
        if(getApplicationContext().getResources().getResourceEntryName(position.getId())=="btnAcceptCall")
            btnAcceptCall();
        if(getApplicationContext().getResources().getResourceEntryName(position.getId())=="btnRejectCall")
            btnHangUp();
        if(getApplicationContext().getResources().getResourceEntryName(position.getId())=="btnSpeaker")
            btnSpeaker();
        if(getApplicationContext().getResources().getResourceEntryName(position.getId())=="btnMute")
            btnMute();
    }


    private void openFragment(){


    }

    private void btnHangUp() {
        call.hangup();
        call.addCallListener(new CallListenerActivity.SinchCallListener());
        call = null;
        finish();
    }

    private void btnAcceptCall() {
        call.answer();
        call.addCallListener(new CallListenerActivity.SinchCallListener());
        finish();
    }

    private void btnSpeaker() {
        if (!audioManager.isSpeakerphoneOn())
        {
            audioManager.setSpeakerphoneOn(true);
        }
        else
        {
            audioManager.setSpeakerphoneOn(false);
        }
    }

    private void btnMute() {
        if (!audioManager.isMicrophoneMute())
        {
            audioManager.setMicrophoneMute(true);
        }
        else
        {
            audioManager.setMicrophoneMute(false);
        }
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            Toast.makeText(CallListenerActivity.this, "call end", Toast.LENGTH_SHORT).show();
            call = null;
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMicrophoneMute(false);

            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

            fragmentManager.beginTransaction().hide(callFragment);

            finish();
            /*Intent intent = new Intent(CallListenerActivity.this,MainActivity.class);
            startActivity(intent);*/
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            //Intent intent = new Intent(CallListenerActivity.this,CallActivity.class);
           // intent.putExtra("AcceptCallIdOfCaller",call.getRemoteUserId());
           // startActivity(intent);
            Toast.makeText(CallListenerActivity.this, "call started", Toast.LENGTH_SHORT).show();
            finish();

        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            Toast.makeText(CallListenerActivity.this, "onCallProgressing", Toast.LENGTH_SHORT).show();
            setVolumeControlStream(AudioManager.STREAM_RING);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {

        }
    }
    private class SinchCallClientListener implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            Toast.makeText(CallListenerActivity.this, "incoming call", Toast.LENGTH_SHORT).show();
           //Intent intent = new Intent(CallListenerActivity.this,CallActivity.class);
            //intent.putExtra("incomingCall",call.getRemoteUserId());
            //startActivity(intent);
            call.addCallListener(new CallListenerActivity.SinchCallListener());
            openFragment();
            finish();
        }
    }



}
