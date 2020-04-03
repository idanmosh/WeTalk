package com.example.wetalk.Calling;
import android.app.Activity;
import android.media.AudioManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.widget.Toast;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import com.example.wetalk.Classes.GlobalApplication;
import com.google.firebase.auth.FirebaseAuth;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoCallListener;

import java.util.List;


public class Sinch extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    public static Call call;
    public static SinchClient sinchClient;
    public static CallClient callClient;
    public static String USERID;

    private static final String APP_KEY = "56095560-a311-4128-b5b7-087f14834b93";
    private static final String APP_SECRET = "4zvuV9c/6UC4LZisgAJG2A==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";
    public static boolean mVideoViewsAdded = false;
    public static boolean noPermissions = false;
    private static final int RC_SETTINGS = 125;
    private static Context con;
    private static boolean isNoPermissions;

    public Sinch(Context context) {
        con = context;
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    }
    @AfterPermissionGranted(RC_SETTINGS)
    private void RequestPermissions() {
        String[] perm = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(con, perm)) {
            isNoPermissions = true;
        }

        else {
            EasyPermissions.requestPermissions((Activity) con, "this app need to access your camera and mic", RC_SETTINGS, perm);
            isNoPermissions = false;
        }
    }


    public static class SinchCallListener implements /*CallListener,*/VideoCallListener {
        Context context = GlobalApplication.getAppContext();

        @Override
        public void onCallProgressing(Call call) {
            Toast.makeText(context, "onCallProgressing", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallEstablished(Call call) {
            Toast.makeText(context, "call started", Toast.LENGTH_SHORT).show();
            CallOutActivity.state.setText("start");



        }

        @Override
        public void onCallEnded(Call endCall) {
            if (noPermissions)
            {
                Toast.makeText(context, "call end", Toast.LENGTH_SHORT).show();
                endCall.hangup();
                CallOutActivity.state.setText("final");
                call = null;
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setSpeakerphoneOn(false);
                audioManager.setMicrophoneMute(false);
            }
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) { }

        @Override
        public void onVideoTrackAdded(Call call) {
            Toast.makeText(context, "call Video", Toast.LENGTH_SHORT).show();
            addVideo();
        }

        @Override
        public void onVideoTrackPaused(Call call) {
            Toast.makeText(context, "Share Video Paused", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onVideoTrackResumed(Call call) {
            Toast.makeText(context, "Share Video Resumed", Toast.LENGTH_SHORT).show();

        }

        private void addVideo () {
            CallOutActivity.state.setText("Video");

        }
    }

    private class SinchCalllClientListener implements CallClientListener {


        private Context context;

        public SinchCalllClientListener(Context context) {
            this.context = context;
            RequestPermissions();

        }
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            RequestPermissions();
            if (isNoPermissions)
            {
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
}
