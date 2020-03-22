package com.example.wetalk.Calling;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.wetalk.Classes.GlobalApplication;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;

    public class SinchOnIncomingCalllClientListener implements CallClientListener {
        Context context = GlobalApplication.getAppContext();



        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            CallListenerSign.call = incomingCall;
            Toast.makeText(context, "incoming call", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context,CallOutActivity.class);
            intent.putExtra("incomingCall",incomingCall.getRemoteUserId());
            context.startActivity(intent);
            CallListenerSign.call.addCallListener(new SinchCallListener());
        }
    }
