package com.example.wetalk.Calling;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.wetalk.R;

public class CallFragment extends Fragment implements View.OnClickListener {
    ImageView btnSpeaker,btnMute;
    ImageView btnRejectCall,btnHangUp,btnAcceptCall;
    boolean btnMuteOff=false, btnSpeakerOff = false;

    onBtnClickListener mCallback;

    @Override
    public void onClick(View v) {
        mCallback.onBtnClick(v);

        switch (v.getId()){
            case R.id.btnMute:
                btnMuteClick();
                break;
            case R.id.btnSpeaker:
                btnSpeakerClick();
                break;
    }
    }
    public void btnMuteClick(){
        if (!btnMuteOff)
        {
            Glide.with(getContext()).load(R.drawable.mute_press).into(btnMute);
            btnMuteOff = true;
        }
        else
        {
            Glide.with(getContext()).load(R.drawable.mute).into(btnMute);
            btnMuteOff = false;
        }
    }


    public void btnSpeakerClick(){
        if (!btnSpeakerOff)
        {
            Glide.with(getContext()).load(R.drawable.speaker_press).into(btnSpeaker);
            btnSpeakerOff = true;
        }
        else
        {
            Glide.with(getContext()).load(R.drawable.speaker).into(btnSpeaker);
            btnSpeakerOff = false;
        }
    }

    public interface onBtnClickListener{
        void onBtnClick(View position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mCallback = (onBtnClickListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()
            +" CallF problem");
        }
    }

    public CallFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_call,container,false);
        btnMute = rootView.findViewById(R.id.btnMute);
        btnRejectCall = rootView.findViewById(R.id.btnRejectCall);
        btnSpeaker = rootView.findViewById(R.id.btnSpeaker);
        btnAcceptCall = rootView.findViewById(R.id.btnAcceptCall);
        btnHangUp = rootView.findViewById(R.id.btnHangUp);

        btnMute.setOnClickListener(this);
        btnRejectCall.setOnClickListener(this);
        btnSpeaker.setOnClickListener(this);
        btnAcceptCall.setOnClickListener(this);
        btnHangUp.setOnClickListener(this);

        return rootView;
    }

}
