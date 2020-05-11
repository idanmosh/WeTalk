package com.example.wetalk.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.AppDir;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.Message;
import com.example.wetalk.Permissions.Permissions;
import com.example.wetalk.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> userMessageList;
    private FirebaseAuth mAuth;
    private Contact receiverContact;
    private AppDir appDir;
    private ListMessageClickListener listener;
    private Context mContext;
    private static boolean[] inProgressDoc;
    private static boolean[] inProgressImage;

    public MessageAdapter(List<Message> userMessageList, Contact receiverContact, ListMessageClickListener listener, Context mContext) {
        this.userMessageList = userMessageList;
        this.receiverContact = receiverContact;
        this.listener = listener;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        inProgressImage = new boolean[userMessageList.size()];
        inProgressDoc = new boolean[userMessageList.size()];

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Message message = userMessageList.get(position);

        if (inProgressDoc.length != userMessageList.size()) {
            boolean[] temp = inProgressDoc;
            inProgressDoc = new boolean[userMessageList.size()];
            for (int i = 0; i < temp.length; i++)
                inProgressDoc[i] = temp[i];
        }

        if (inProgressImage.length != userMessageList.size()) {
            boolean[] temp = inProgressImage;
            inProgressImage = new boolean[userMessageList.size()];
            for (int i = 0; i < temp.length; i++)
                inProgressImage[i] = temp[i];
        }

        if (appDir == null)
            appDir = new AppDir();

        if (receiverContact.getImage() != null)
            loadReceiverImage(holder);

        if (!inProgressImage[position]) {
            holder.imageProgressBar.setVisibility(GONE);
            holder.receiverImageBlur.setVisibility(INVISIBLE);
            holder.receiverImageLayout.setVisibility(GONE);
        }
        else
            holder.receiverImageLayout.setVisibility(VISIBLE);

        if (!inProgressDoc[position]) {
            holder.docProgressBar.setVisibility(GONE);
            holder.receiverDocImageDownload.setVisibility(INVISIBLE);
            holder.receiverDocLayout.setVisibility(GONE);
        }
        else
            holder.receiverDocLayout.setVisibility(VISIBLE);

        holder.receiverAudioLayout.setVisibility(GONE);
        holder.senderAudioLayout.setVisibility(GONE);
        holder.receiverDocImage.setVisibility(INVISIBLE);
        holder.senderDocLayout.setVisibility(GONE);
        holder.receiverProfileImage.setVisibility(INVISIBLE);
        holder.receiverImage.setVisibility(INVISIBLE);
        holder.receiverLayout.setVisibility(GONE);
        holder.senderLayout.setVisibility(GONE);
        holder.senderMessageImg.setVisibility(GONE);
        holder.receiverMessageImg.setVisibility(GONE);

        if (message.getType().equals("text"))
            textMessage(holder, position, message, messageSenderId);
        else if (message.getType().equals("image"))
            imageMessage(holder, position, message, messageSenderId);
        else if (message.getType().equals("voice"))
            voiceMessage(holder, position, message, messageSenderId);
        else
            docMessage(holder, position, message, messageSenderId);
    }

    private void voiceMessage(MessageViewHolder holder, int position, Message message, String messageSenderId) {

        if (message.getFrom().equals(messageSenderId)) {
            holder.senderAudioLayout.setVisibility(VISIBLE);
            Uri audioUri = getAudioMessageUri(message.getMessageId());
            if (audioUri != null)
                holder.senderPlayer.setAudio(audioUri.getPath());
            else {
                appDir.saveAudioToStorage(mContext, message.getMessageId(), message.getMessage());
                do {
                    audioUri = getAudioMessageUri(message.getMessageId());
                } while (audioUri == null);
                holder.senderPlayer.setAudio(message.getMessage());
            }
            holder.senderTimeAudio.setText(message.getMessageTime());
            if (message.getState() != null) {
                if (message.getState().equals("read"))
                    holder.senderCheckReadAudio.setImageResource(R.drawable.ic_done_all_gray);
            }
        }
        else {
            holder.receiverAudioLayout.setVisibility(VISIBLE);
            holder.receiverProfileImage.setVisibility(VISIBLE);
            Uri audioUri = getAudioMessageUri(message.getMessageId());
            if (audioUri != null)
                holder.receiverPlayer.setAudio(audioUri.getPath());
            else {
                appDir.saveAudioToStorage(mContext, message.getMessageId(), message.getMessage());
                do {
                    audioUri = getAudioMessageUri(message.getMessageId());
                } while (audioUri == null);
                holder.receiverPlayer.setAudio(message.getMessage());
            }
            holder.receiverTimeAudio.setText(message.getMessageTime());
        }
    }

    private Uri getAudioMessageUri(String messageId) {
        return appDir.getAudioFromStorage(messageId + ".m4a", mContext);
    }

    @SuppressLint("StaticFieldLeak")
    private void docMessage(MessageViewHolder holder, int position, Message message, String messageSenderId) {
        if (message.getFrom().equals(messageSenderId)) {
            holder.senderDocLayout.setVisibility(VISIBLE);
            if (message.getMessage().length() > 35)
                holder.senderDocNameTxt.setText(message.getMessage().substring(0, 35) + "...");
            else
                holder.senderDocNameTxt.setText(message.getMessage());
            holder.senderDocTypeTxt.setText(message.getType());
            holder.senderTimeDoc.setText(message.getMessageTime());
            if (message.getState() != null) {
                if (message.getState().equals("read"))
                    holder.senderCheckReadDoc.setImageResource(R.drawable.ic_done_all_gray);
            }
        }
        else {
            holder.receiverDocLayout.setVisibility(VISIBLE);
            holder.receiverProfileImage.setVisibility(VISIBLE);
            holder.receiverTimeDoc.setText(message.getMessageTime());
            holder.receiverDocTypeTxt.setText(message.getType());
            if (message.getMessage().length() > 35)
                holder.receiverDocNameTxt.setText(message.getMessage().substring(0, 35) + "...");
            else
                holder.receiverDocNameTxt.setText(message.getMessage());
        }

        if (message.getMessage() != null) {
            if (!Permissions.checkPermissions(mContext, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE)) {
                if (appDir != null) {
                    if ((getDocUri(message) != null)) {
                        if (inProgressDoc[position]) {
                            inProgressDoc[position] = false;
                            Handler mHandler = new Handler();
                            final int[] progress = {holder.docProgressBar.getProgress()};
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(final Void... params) {
                                    while (progress[0] < 100) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        progress[0] += 20;
                                        mHandler.post(() -> {
                                            holder.docProgressBar.setProgress(progress[0]);
                                            if (progress[0] == 100) {
                                                holder.receiverDocImage.setVisibility(VISIBLE);
                                                holder.docProgressBar.setVisibility(GONE);
                                                notifyItemChanged(position,  holder.receiverDocImage);
                                            }
                                        });
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                        else
                            holder.receiverDocImage.setVisibility(VISIBLE);
                    }
                    else
                        holder.receiverDocImageDownload.setVisibility(VISIBLE);
                }
            }
            else
                holder.receiverDocImageDownload.setVisibility(VISIBLE);
        }

        holder.receiverDocImageDownload.setOnClickListener(v -> {
            listener.onImageMessageClickPermissions();
            if (haveStoragePermissions()) {
                Uri docUri = getDocUri(message);
                if (docUri == null) {
                    holder.docProgressBar.setVisibility(VISIBLE);
                    inProgressDoc[position] = true;
                    appDir.saveDocToStorage(mContext, new String[] {message.getMessage(),message.getMessageId(),
                            "." + message.getType().toLowerCase()}, message.getLink());
                    do {
                        docUri = getDocUri(message);
                    } while (docUri == null);
                    notifyItemChanged(position, holder.receiverDocImageDownload);
                }
            }
        });

        holder.receiverDocImage.setOnClickListener(v -> {
            listener.onImageMessageClickPermissions();
            if (haveStoragePermissions()) {
                Uri docUri = getDocUri(message);
                if (docUri == null)
                    appDir.saveDocToStorage(mContext, new String[]{message.getMessage(), message.getMessageId(),
                            "." + message.getType().toLowerCase()}, message.getLink());
                do {
                    docUri = getDocUri(message);
                } while (docUri == null);

                listener.showDoc(docUri, message.getType().toLowerCase());
            }
        });

        holder.senderDocImage.setOnClickListener(v -> {
            listener.onImageMessageClickPermissions();
            if (haveStoragePermissions()) {
                Uri docUri = getDocUri(message);
                if (docUri == null)
                    appDir.saveDocToStorage(mContext, new String[]{message.getMessage(), message.getMessageId(),
                            "." + message.getType().toLowerCase()}, message.getLink());
                do {
                    docUri = getDocUri(message);
                } while (docUri == null);

                listener.showDoc(docUri, message.getType().toLowerCase());
            }
        });
    }

    private Uri getDocUri(Message message) {
        if (message.getMessage().contains("http"))
            return getDocMessageUri(message.getMessageId() + message.getType().toLowerCase());
        else
            return getDocMessageUri(message.getMessage());
    }

    private Uri getDocMessageUri(String fileName) {
        return appDir.getDocFromStorage(fileName, mContext);
    }

    @SuppressLint("StaticFieldLeak")
    private void imageMessage(MessageViewHolder holder, int position, Message message, String messageSenderId) {
        if (message.getFrom().equals(messageSenderId)) {
            holder.senderImageLayout.setVisibility(VISIBLE);
            if (message.getImage() != null)
                loadImage(holder.senderImage, position,0);
            if (!message.getMessage().equals("")) {
                holder.senderMessageImg.setText(message.getMessage());
                holder.senderMessageImg.setVisibility(VISIBLE);
            }
            holder.senderTimeImg.setText(message.getMessageTime());
            if (message.getState() != null) {
                if (message.getState().equals("read"))
                    holder.senderCheckReadImg.setImageResource(R.drawable.ic_done_all_gray);
            }
        }
        else {
            loadImage(holder.receiverImage, position,0);
            holder.receiverProfileImage.setVisibility(VISIBLE);
            loadImage(holder.receiverImageBlur, position, 18);

            if (!message.getMessage().equals("")) {
                holder.receiverMessageImg.setText(message.getMessage());
                holder.receiverMessageImg.setVisibility(VISIBLE);
            }
            holder.receiverImageLayout.setVisibility(VISIBLE);
            holder.receiverMessageImg.setText(message.getMessage());
            holder.receiverTimeImg.setText(message.getMessageTime());
        }

        if (message.getImage() != null) {
            if (!Permissions.checkPermissions(mContext, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE)) {
                if (appDir != null) {
                    if ((getImageMessageUri(message.getMessageId()) != null)) {
                        if (inProgressImage[position]) {
                            inProgressImage[position] = false;
                            Handler mHandler = new Handler();
                            final int[] progress = {holder.imageProgressBar.getProgress()};
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(final Void... params) {
                                    while (progress[0] < 100) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        progress[0] += 20;
                                        mHandler.post(() -> {
                                            holder.imageProgressBar.setProgress(progress[0]);
                                            if (progress[0] == 100) {
                                                holder.receiverImage.setVisibility(VISIBLE);
                                                holder.imageProgressBar.setVisibility(GONE);
                                                notifyItemChanged(position,  holder.receiverImage);
                                            }
                                        });
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                        else
                            holder.receiverImage.setVisibility(VISIBLE);
                    }
                    else {
                        holder.receiverImageBlur.setVisibility(VISIBLE);
                        holder.receiverImageBlur.setImageResource(R.drawable.ic_file_download);
                    }
                }
            }
            else {
                holder.receiverImageBlur.setVisibility(VISIBLE);
                holder.receiverImageBlur.setImageResource(R.drawable.ic_file_download);
            }
        }

        holder.senderImage.setOnClickListener(v-> {
            listener.onImageMessageClickPermissions();
            if (haveStoragePermissions()) {
                Uri imageUri = getImageMessageUri(message.getMessageId());
                if (imageUri == null)
                    appDir.saveImageToStorage(holder.senderImage, message.getMessageId(), mContext);

                imageUri = getImageMessageUri(message.getMessageId());
                do {
                    imageUri = getImageMessageUri(message.getMessageId());
                } while (imageUri == null);
                loadImage(holder.senderImage, position,0);
                listener.showImage(imageUri);
            }
        });

        holder.receiverImage.setOnClickListener(v -> {
            listener.onImageMessageClickPermissions();
            if (haveStoragePermissions()) {
                Uri imageUri = getImageMessageUri(message.getMessageId());
                if (imageUri == null)
                    appDir.saveImageToStorage(holder.receiverImage, message.getMessageId(), mContext);

                imageUri = getImageMessageUri(message.getMessageId());
                do {
                    imageUri = getImageMessageUri(message.getMessageId());
                } while (imageUri == null);

                listener.showImage(imageUri);
            }
        });

        holder.receiverImageBlur.setOnClickListener(v -> {
            listener.onImageMessageClickPermissions();
            if (haveStoragePermissions()) {
                Uri imageUri = getImageMessageUri(message.getMessageId());
                if (imageUri == null) {
                    holder.imageProgressBar.setVisibility(VISIBLE);
                    notifyItemChanged(position, holder.receiverImageBlur);
                    inProgressImage[position] = true;
                    appDir.saveImageToStorage(holder.receiverImage, message.getMessageId(), mContext);
                }
            }
        });
    }

    private boolean haveStoragePermissions() {
        return !Permissions.checkPermissions(mContext, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE);
    }

    private Uri getImageMessageUri(String messageId) {
        return appDir.getImageFromStorage(messageId, mContext);
    }

    private void textMessage(MessageViewHolder holder, int position, Message message, String messageSenderId) {
        if (message.getFrom().equals(messageSenderId)) {
            holder.senderLayout.setVisibility(VISIBLE);
            holder.senderMessageTxt.setText(message.getMessage());
            holder.senderTimeTxt.setText(message.getMessageTime());
            if (userMessageList.get(position).getState() != null) {
                if (userMessageList.get(position).getState().equals("read"))
                    holder.senderCheckRead.setImageResource(R.drawable.ic_done_all_gray);
            }
        }
        else {
            holder.receiverProfileImage.setVisibility(VISIBLE);
            holder.receiverLayout.setVisibility(VISIBLE);
            holder.receiverMessageTxt.setText(message.getMessage());
            holder.receiverTimeTxt.setText(message.getMessageTime());
        }
    }

    private void loadImage(ImageView imgView, int position, int override) {
        Glide.with(imgView.getContext()).asBitmap().load(userMessageList.get(position).getImage())
                .override(override,override).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (override == 0)
                    imgView.setImageBitmap(resource);
                else
                    imgView.setBackground(new BitmapDrawable(mContext.getResources(), resource));
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) { }
        });
    }

    private void loadReceiverImage(MessageViewHolder holder) {
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

        private TextView senderTimeAudio, receiverTimeAudio;
        private TextView senderMessageTxt, receiverMessageTxt, senderTimeTxt, receiverTimeTxt;
        private TextView senderMessageImg, receiverMessageImg, senderTimeImg, receiverTimeImg;
        private TextView senderDocNameTxt, receiverDocNameTxt, senderTimeDoc, receiverTimeDoc;
        private TextView senderDocTypeTxt, receiverDocTypeTxt;
        private CircleImageView receiverProfileImage;
        private ImageView senderCheckRead;
        private ImageView senderCheckReadDoc;
        private ImageView senderCheckReadAudio;
        private ImageView senderImage, receiverImage, receiverImageBlur;
        private ImageView senderCheckReadImg;
        private ImageView senderDocImage, receiverDocImage, receiverDocImageDownload;
        private LinearLayout senderDocLayout, receiverDocLayout;
        private LinearLayout senderLayout, receiverLayout;
        private LinearLayout senderImageLayout, receiverImageLayout;
        private LinearLayout senderAudioLayout, receiverAudioLayout;
        private ProgressBar imageProgressBar, docProgressBar;
        private VoicePlayerView senderPlayer, receiverPlayer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverTimeAudio = itemView.findViewById(R.id.receiver_time_audio);
            receiverAudioLayout = itemView.findViewById(R.id.receiver_audio_layout);
            senderAudioLayout = itemView.findViewById(R.id.sender_audio_layout);
            senderCheckReadAudio = itemView.findViewById(R.id.sender_check_image_audio);
            senderTimeAudio = itemView.findViewById(R.id.sender_time_audio);
            receiverPlayer = itemView.findViewById(R.id.receiver_voicePlayerView);
            senderPlayer = itemView.findViewById(R.id.sender_voicePlayerView);
            docProgressBar = itemView.findViewById(R.id.doc_download_progress_bar);
            receiverDocImageDownload = itemView.findViewById(R.id.receiver_doc_download);
            receiverDocTypeTxt = itemView.findViewById(R.id.receiver_type_txt);
            senderDocTypeTxt = itemView.findViewById(R.id.sender_type_txt);
            senderCheckReadDoc = itemView.findViewById(R.id.sender_check_image_doc);
            receiverTimeDoc = itemView.findViewById(R.id.receiver_time_doc);
            senderTimeDoc = itemView.findViewById(R.id.sender_time_doc);
            receiverDocNameTxt = itemView.findViewById(R.id.receiver_doc_name_txt);
            senderDocNameTxt = itemView.findViewById(R.id.sender_doc_name_txt);
            receiverDocImage = itemView.findViewById(R.id.receiver_doc);
            senderDocImage = itemView.findViewById(R.id.sender_doc);
            receiverDocLayout = itemView.findViewById(R.id.receiver_doc_layout);
            senderDocLayout = itemView.findViewById(R.id.sender_doc_layout);
            imageProgressBar = itemView.findViewById(R.id.image_download_progress_bar);
            receiverImageBlur = itemView.findViewById(R.id.receiver_image_blur);
            senderCheckReadImg = itemView.findViewById(R.id.sender_check_image_image);
            receiverTimeImg = itemView.findViewById(R.id.receiver_time_image);
            senderTimeImg = itemView.findViewById(R.id.sender_time_image);
            receiverMessageImg = itemView.findViewById(R.id.receiver_message_image);
            senderMessageImg = itemView.findViewById(R.id.sender_message_image);
            senderImage = itemView.findViewById(R.id.sender_image);
            receiverImage = itemView.findViewById(R.id.receiver_image);
            senderImageLayout = itemView.findViewById(R.id.sender_image_layout);
            receiverImageLayout = itemView.findViewById(R.id.receiver_image_layout);
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

    public interface ListMessageClickListener{
        void onImageMessageClickPermissions();
        void showImage(Uri uri);
        void showDoc(Uri uri, String type);
    }
}
