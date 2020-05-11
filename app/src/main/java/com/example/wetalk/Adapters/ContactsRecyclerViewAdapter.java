package com.example.wetalk.Adapters;

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
import com.example.wetalk.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.MyViewHolder> {

    private List<Contact> mContactsList;
    private int choice;
    private final ListItemClickListener mOnClickListener;

    public ContactsRecyclerViewAdapter(List<Contact> mContactsList, int choice, ListItemClickListener mOnClickListener) {
        this.mContactsList = mContactsList;
        this.choice = choice;
        this.mOnClickListener = mOnClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (mContactsList.get(position).getImage() != null)
            loadImage(holder, position);
        holder.tv_unreadMessages.setVisibility(View.INVISIBLE);
        holder.tv_status.setVisibility(View.INVISIBLE);
        holder.tv_lastMessage.setVisibility(View.INVISIBLE);

        holder.tv_name.setText(mContactsList.get(position).getName());

        if (choice == 1) {
            holder.tv_status.setVisibility(View.VISIBLE);
            holder.tv_status.setText(mContactsList.get(position).getStatus());
            holder.tv__lastMessageTime.setVisibility(View.INVISIBLE);
        }
        else {
            holder.tv_name.setText(mContactsList.get(position).getName());
            holder.tv_phone.setText(mContactsList.get(position).getPhone());
            if (mContactsList.get(position).getLastMessage() != null) {
                holder.tv_lastMessage.setVisibility(View.VISIBLE);
                holder.tv_lastMessage.setText(mContactsList.get(position).getLastMessage().getMessage());
                holder.tv__lastMessageTime.setText(mContactsList.get(position).getLastMessage().getMessageTimeForContactView());
            }
            if (mContactsList.get(position).getUnreadMessages() > 0) {
                holder.tv_unreadMessages.setText(String.valueOf(mContactsList.get(position).getUnreadMessages()));
                holder.tv_unreadMessages.setVisibility(View.VISIBLE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            mOnClickListener.onItemClick(position);
        });
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
        private TextView tv_status;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_status = itemView.findViewById(R.id.status);
            tv_name = itemView.findViewById(R.id.contact_name);
            tv_phone = itemView.findViewById(R.id.contact_phone);
            civ_image = itemView.findViewById(R.id.contact_img);
            tv_lastMessage = itemView.findViewById(R.id.last_message);
            tv_unreadMessages = itemView.findViewById(R.id.circle_badge);
            tv__lastMessageTime = itemView.findViewById(R.id.last_message_time);
        }
    }

    public interface ListItemClickListener {
        void onItemClick(int position);
    }
}
