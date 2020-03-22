package com.example.wetalk.Calling;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.Classes.GlobalApplication;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class SinchCallListener implements CallListener {
    Context context = GlobalApplication.getAppContext();


    @Override
    public void onCallProgressing(Call call) {
        Toast.makeText(context, "onCallProgressing", Toast.LENGTH_SHORT).show();
        ((AppCompatActivity)context).setVolumeControlStream(AudioManager.STREAM_RING);
    }

    @Override
    public void onCallEstablished(Call call) {
        ((AppCompatActivity)context).setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        Toast.makeText(context, "call started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCallEnded(Call call) {
        Toast.makeText(context, "call end", Toast.LENGTH_SHORT).show();
        call = null;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMicrophoneMute(false);

        ((AppCompatActivity)context).setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
    }

    @Override
    public void onShouldSendPushNotification(Call call, List<PushPair> list) {

    }
}
