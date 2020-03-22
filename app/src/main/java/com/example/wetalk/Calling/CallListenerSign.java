package com.example.wetalk.Calling;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.AudioManager;

import com.example.wetalk.Classes.GlobalApplication;
import com.google.firebase.auth.FirebaseAuth;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;

public class CallListenerSign {

    public static Call call;
    public static SinchClient sinchClient;
    public static CallClient callClient;


    Context context = GlobalApplication.getAppContext();

    private static final String APP_KEY = "56095560-a311-4128-b5b7-087f14834b93";
    private static final String APP_SECRET = "4zvuV9c/6UC4LZisgAJG2A==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    public CallListenerSign()
    {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(context)
                .userId(FirebaseAuth.getInstance().getUid())
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.startListeningOnActiveConnection();
        //sinchClient.start();
       // sinchClient.getCallClient().addCallClientListener(new SinchOnIncomingCalllClientListener());
    }
 }
