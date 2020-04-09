package com.example.wetalk.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.Messages;
import com.example.wetalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Context mContext;
    private Contact receiverContact;

    public MessageAdapter(List<Messages> userMessageList, Context mContext, Contact receiverContact) {
        this.userMessageList = userMessageList;
        this.mContext = mContext;
        this.receiverContact = receiverContact;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Messages messages = userMessageList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        if (receiverContact.getImage() != null)
            loadImage(holder);

        if (messages.getType().equals("text")) {
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.receiverMessageTxt.setVisibility(View.INVISIBLE);
            holder.senderLayout.setVisibility(View.INVISIBLE);

            if (messages.getFrom().equals(messageSenderId)) {
                holder.senderLayout.setVisibility(View.VISIBLE);
                holder.senderMessageTxt.setText(messages.getMessage());
                holder.timeTxt.setText(messages.getMessageTime());
            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageTxt.setVisibility(View.VISIBLE);
                holder.receiverMessageTxt.setText(messages.getMessage());
            }
        }
    }

    private void loadImage(MessageViewHolder holder) {
        Glide.with(holder.receiverProfileImage.getContext()).asBitmap().load(receiverContact.getImage()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                holder.receiverProfileImage.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) { }
        });
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends ViewHolder {

        private TextView senderMessageTxt, receiverMessageTxt, timeTxt;
        private CircleImageView receiverProfileImage;
        private LinearLayout senderLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderLayout = itemView.findViewById(R.id.sender_layout);
            senderMessageTxt = itemView.findViewById(R.id.sender_message_text);
            receiverMessageTxt = itemView.findViewById(R.id.receiver_message_text);
            timeTxt = itemView.findViewById(R.id.sender_time);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }
}
