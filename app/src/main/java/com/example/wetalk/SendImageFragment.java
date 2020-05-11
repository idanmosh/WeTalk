package com.example.wetalk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.AppDir;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Permissions.Permissions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SendImageFragment extends Fragment {

    private static final String DATE_FORMAT = "h:mm a dd MMMM yyyy";

    private static final String ContactPREFERENCES = "ContactsPrefs";
    private SharedPreferences mContactsSharedPreferences;

    private Toolbar sendImageToolbar;
    private View sendImageView;
    private ImageView imageView;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private FloatingActionButton sendImageBtn;
    private ProgressBar mProgressBar;
    private TextView taskPercent;
    private EditText mMessageImage;
    private String senderId;
    private Contact mContact;
    private Uri sendImageUri;
    private Context mContext;
    private boolean crop = false;
    private OnBackPressedFragment listener;
    private AppDir appDir;

    public SendImageFragment(Contact mContact, Context mContext ,Uri sendImageUri, OnBackPressedFragment listener) {
        this.mContact = mContact;
        this.mContext = mContext;
        this.sendImageUri = sendImageUri;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sendImageView = inflater.inflate(R.layout.fragment_send_image, container, false);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        senderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mContactsSharedPreferences = mContext.getSharedPreferences(ContactPREFERENCES, Context.MODE_PRIVATE);

        Display screenSize = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screenSize.getSize(size);
        int width = size.x;

        sendImageToolbar = sendImageView.findViewById(R.id.send_image_toolbar);
        sendImageToolbar.inflateMenu(R.menu.send_image_menu);
        ((AppCompatActivity)getActivity()).setSupportActionBar(sendImageToolbar);
        Objects.requireNonNull((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        imageView = sendImageView.findViewById(R.id.send_image);
        loadSendImage();

        sendImageToolbar.setNavigationOnClickListener(v -> listener.onBackPressedFragment());

        sendImageBtn = sendImageView.findViewById(R.id.btnSendImage);
        sendImageBtn.setOnClickListener(v -> checkStoragePermissions());

        mMessageImage = sendImageView.findViewById(R.id.EditTextImage);
        mMessageImage.setWidth((int) (width * 0.8));
        mMessageImage.requestFocus();

        taskPercent = sendImageView.findViewById(R.id.send_image_task_percent);
        mProgressBar = sendImageView.findViewById(R.id.image_progressbar);

        setHasOptionsMenu(true);

        return sendImageView;
    }

    private void checkStoragePermissions() {
        if (!Permissions.checkPermissions(getContext(), Permissions.READ_STORAGE, Permissions.WRITE_STORAGE)) {
            if(appDir == null)
                appDir = new AppDir();
            sendImageMessage();
        }
        else
            Permissions.ImagePermissionDialog(getContext(), SendImageFragment.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Permissions.EXTERNAL_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                sendImageMessage();
            }
            else {
                Toast.makeText(getContext(), "You can't get access to camera and media" +
                " storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendImageMessage() {
        mProgressBar.setVisibility(VISIBLE);
        taskPercent.setVisibility(VISIBLE);

        String messageText = mMessageImage.getText().toString();

        if ((TextUtils.isEmpty(messageText)) || (messageText.charAt(0) == ' ') && (messageText.charAt(0) == '\n'))
            messageText = "";

        DatabaseReference senderMessageKeyRef = rootRef.child(getString(R.string.USERS))
                .child(senderId).child("Messages").child(mContact.getUserId()).push();
        String senderMessageKey = senderMessageKeyRef.getKey();
        DatabaseReference receiverMessageKeyRef = rootRef.child(getString(R.string.USERS))
                .child(mContact.getUserId()).child("Messages").child(senderId).child(Objects.requireNonNull(senderMessageKey));
        String date = getDate();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("Image Files").child(senderMessageKey + getString(R.string.JPG));

        File actualImage;

        if (crop) {
            actualImage = new File(Objects.requireNonNull(sendImageUri.getPath()));
            crop = false;
        }
        else
            actualImage = new File(getPath(sendImageUri));

        try {
            Bitmap compressedImage = new Compressor(mContext)
                    .setMaxWidth(250)
                    .setMaxHeight(250)
                    .setQuality(50)
                    .compressToBitmap(actualImage);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            byte[] final_image = outputStream.toByteArray();

            UploadTask senderUploadTask = storageRef.putBytes(final_image);

            String finalMessageText = messageText;

            senderUploadTask.addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getMetadata() != null) {
                    Task<Uri> senderResult = taskSnapshot.getStorage().getDownloadUrl();
                    senderResult.addOnSuccessListener(uri -> {
                        Map<String, Object> senderMessageTextBody = new HashMap<>();
                        senderMessageTextBody.put("message", finalMessageText);
                        senderMessageTextBody.put("type", "image");
                        senderMessageTextBody.put("from", senderId);
                        senderMessageTextBody.put("date", date);
                        senderMessageTextBody.put("image", uri.toString());
                        senderMessageTextBody.put("state", "unread");

                        Map<String, Object> receiverMessageTextBody = new HashMap<>();
                        receiverMessageTextBody.put("message", finalMessageText);
                        receiverMessageTextBody.put("type", "image");
                        receiverMessageTextBody.put("from", senderId);
                        receiverMessageTextBody.put("date", date);
                        receiverMessageTextBody.put("image", uri.toString());

                        senderMessageKeyRef.updateChildren(senderMessageTextBody).addOnCompleteListener(task -> {
                            receiverMessageKeyRef.updateChildren(receiverMessageTextBody).addOnCompleteListener(task1 -> {
                                setContactState();
                                appDir.saveImageToStorage(imageView, senderMessageKey, mContext);
                                mMessageImage.setText("");
                                mProgressBar.setVisibility(GONE);
                                taskPercent.setVisibility(GONE);
                                listener.onBackPressedFragment();
                            });
                        });
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(GONE);
                taskPercent.setVisibility(GONE);
            }).addOnProgressListener(taskSnapshot -> {
                double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                mProgressBar.setProgress((int) p);
                taskPercent.setText((int) p + "%");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setContactState() {
        mContactsSharedPreferences.edit().putBoolean(mContact.getUserId() + "_state", true).apply();
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    @SuppressLint("SimpleDateFormat")
    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date today = Calendar.getInstance().getTime();
        String date = sdf.format(today);
        return date;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                sendImageUri = Objects.requireNonNull(result).getUri();
                if (sendImageUri != null) {
                    crop = true;
                    loadSendImage();
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void loadSendImage() {
        if (sendImageUri != null) {
            Glide.with(mContext).asBitmap().load(sendImageUri.toString()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    imageView.setImageBitmap(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    Glide.with(mContext).load(placeholder).into(imageView);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.send_image_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crop_send_image:
                cropSendImage();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void cropSendImage() {
        if (!Permissions.checkPermissions(mContext, Permissions.READ_STORAGE, Permissions.CAMERA)) {
            if (sendImageUri != null)
                CropImage.activity(sendImageUri)
                        .setAspectRatio(1,1)
                        .start(getContext(), this);
        }
        else
            Permissions.ProfileImagePermissionDialog(getContext(), SendImageFragment.this);
    }
}
