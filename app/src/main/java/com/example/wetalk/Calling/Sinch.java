package com.example.wetalk.Calling;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.Classes.GlobalApplication;
import com.google.firebase.auth.FirebaseAuth;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;


public class Sinch extends AppCompatActivity {

    public static Call call;
    public static SinchClient sinchClient;
    public static CallClient callClient;
    public static String USERID;

    private static final String APP_KEY = "56095560-a311-4128-b5b7-087f14834b93";
    private static final String APP_SECRET = "4zvuV9c/6UC4LZisgAJG2A==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";


    public Sinch(Context context) {
        USERID = FirebaseAuth.getInstance().getUid();
        sinchClient = com.sinch.android.rtc.Sinch.getSinchClientBuilder()
                .context(context)
                .userId(USERID)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.getCallClient().addCallClientListener(new SinchCalllClientListener(context){});
        sinchClient.start();

    }


    public static class SinchCallListener implements CallListener {
        Context context = GlobalApplication.getAppContext();

        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(context, "onCallProgressing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallEstablished(Call call) {
            Toast.makeText(context, "call started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallEnded(Call endCall) {
            Toast.makeText(context, "call end", Toast.LENGTH_SHORT).show();
            endCall.hangup();
            CallOutActivity.state.setText("final");
            call = null;
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMicrophoneMute(false);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) { }
    }

    private class SinchCalllClientListener implements CallClientListener {


        private Context context;

        public SinchCalllClientListener(Context context) {
            this.context = context;
        }
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            Toast.makeText(context, "incoming call", Toast.LENGTH_SHORT).show();
            call = incomingCall;
            call.addCallListener(new SinchCallListener());
            Intent callscreen = new Intent(context, CallOutActivity.class);
            callscreen.putExtra("incomingCall",incomingCall.getRemoteUserId());
            callscreen.putExtra("callId", call.getCallId());
            callscreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callscreen);
        }
    }
}
