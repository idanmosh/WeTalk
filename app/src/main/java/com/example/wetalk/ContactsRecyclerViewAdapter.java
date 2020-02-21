package com.example.wetalk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.Contact;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private List<Contact> mContactsList;
    private int choice;

    public ContactsRecyclerViewAdapter(Context mContext, List<Contact> mContactsList, int choice) {
        this.mContext = mContext;
        this.mContactsList = mContactsList;
        this.choice = choice;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (choice == 1) {
            if (mContactsList.get(position).getImage() != null)
                loadImage(holder, position);
            holder.tv_name.setText(mContactsList.get(position).getName());
            holder.tv_lastMessage.setText(mContactsList.get(position).getStatus());
            holder.tv__lastMessageTime.setVisibility(View.GONE);
            holder.tv_unreadMessages.setVisibility(View.GONE);
        }
        else {
            holder.tv_name.setText(mContactsList.get(position).getName());
            holder.tv_phone.setText(mContactsList.get(position).getPhone());

            holder.tv_lastMessage.setText(mContactsList.get(position).getLastMessage());
            holder.tv__lastMessageTime.setText(mContactsList.get(position).getLastMessageTime());
            holder.tv_unreadMessages.setText(mContactsList.get(position).getUnreadMessages());
        }

        holder.itemView.setOnClickListener(v -> {});
    }

    private void loadImage(@NonNull MyViewHolder holder, int position) {
        Glide.with(holder.civ_image.getContext()).asBitmap().load(mContactsList.get(position).getImage()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                holder.civ_image.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) { }
        });

    }

    @Override
    public int getItemCount() {
        return mContactsList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private TextView tv_phone;
        private CircleImageView civ_image;
        private TextView tv_lastMessage;
        private TextView tv_unreadMessages;
        private TextView tv__lastMessageTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.contact_name);
            tv_phone = itemView.findViewById(R.id.contact_phone);
            civ_image = itemView.findViewById(R.id.contact_img);
            tv_lastMessage = itemView.findViewById(R.id.last_message);
            tv_unreadMessages = itemView.findViewById(R.id.circle_badge);
            tv__lastMessageTime = itemView.findViewById(R.id.last_message_time);
        }
    }

}
