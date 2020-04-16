package com.example.wetalk.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.Message;
import com.example.wetalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Context mContext;
    private Contact receiverContact;

    public MessageAdapter(List<Message> userMessageList, Context mContext, Contact receiverContact) {
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
        Message message = userMessageList.get(position);
        String fromUserId = message.getFrom();
        String fromMessageType = message.getType();

        if (receiverContact.getImage() != null)
            loadImage(holder);

        if (message.getType().equals(fromMessageType)) {
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.receiverLayout.setVisibility(View.INVISIBLE);
            holder.senderLayout.setVisibility(View.INVISIBLE);

            if (message.getFrom().equals(messageSenderId)) {
                holder.senderLayout.setVisibility(View.VISIBLE);
                holder.senderMessageTxt.setText(message.getMessage());
                holder.senderTimeTxt.setText(message.getMessageTime());
                if (userMessageList.get(position).getState() != null) {
                    if (userMessageList.get(position).getState().equals("read"))
                        holder.senderCheckRead.setImageResource(R.drawable.ic_done_all_gray);
                }
            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverLayout.setVisibility(View.VISIBLE);
                holder.receiverMessageTxt.setText(message.getMessage());
                holder.receiverTimeTxt.setText(message.getMessageTime());
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

        private TextView senderMessageTxt, receiverMessageTxt, senderTimeTxt, receiverTimeTxt;
        private CircleImageView receiverProfileImage;
        private ImageView senderCheckRead;
        private LinearLayout senderLayout, receiverLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderCheckRead = itemView.findViewById(R.id.sender_check_image);
            senderLayout = itemView.findViewById(R.id.sender_layout);
            receiverLayout = itemView.findViewById(R.id.receiver_layout);
            senderMessageTxt = itemView.findViewById(R.id.sender_message_text);
            receiverMessageTxt = itemView.findViewById(R.id.receiver_message_text);
            senderTimeTxt = itemView.findViewById(R.id.sender_time);
            receiverTimeTxt = itemView.findViewById(R.id.receiver_time);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }
}
