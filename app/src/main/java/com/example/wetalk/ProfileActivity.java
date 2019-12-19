package com.example.wetalk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private EditText mUserName, mUserStatus;
    private CircleImageView mUserProfileImage;
    private Button nextProfileBtn;
    private ProgressBar mProgressBar;

    private SharedPreferences mSharedPreferences;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String IMAGE_KEY = "image_key";
    private static final String NAME_KEY = "name_key";
    private static final String STATUS_KEY = "status_key";
    private static final String Profile_State = "profileState";

    private boolean mProfileActivityInProgress;

    private User user;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRef;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mUserProfileImage = findViewById(R.id.profile_image);
        mUserName = findViewById(R.id.set_user_name);
        mUserStatus = findViewById(R.id.set_user_status);
        mProgressBar = findViewById(R.id.profile_progressbar);

        mSharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        user = new User();
        getSharedPreferences();
        retrieveUserProfilePic();

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.main_page_toolbar), true);
        fade.excludeTarget(decor.findViewById(R.id.AppBarLayout), true);
        fade.excludeTarget(decor.findViewById(R.id.shared_toolbar), true);
        fade.excludeTarget(decor.findViewById(R.id.main_tabs),true);
        fade.excludeTarget(android.R.id.statusBarBackground,true);
        fade.excludeTarget(android.R.id.navigationBarBackground,true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        nextProfileBtn = findViewById(R.id.next_profile_btn);

        nextProfileBtn.setOnClickListener(v -> updateProfile());

        mUserProfileImage.setOnClickListener(v -> cropImage());
    }

    private void getSharedPreferences() {
        mProfileActivityInProgress = mSharedPreferences.getBoolean(Profile_State, false);
        user.setImage(mSharedPreferences.getString(IMAGE_KEY, ""));
        mUserName.setText(mSharedPreferences.getString(NAME_KEY, ""));
        mUserStatus.setText(mSharedPreferences.getString(STATUS_KEY, ""));
    }

    private void setSharedPreferences() {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(IMAGE_KEY, user.getImage());
        mEditor.putString(NAME_KEY, mUserName.getText().toString());
        mEditor.putString(STATUS_KEY, mUserStatus.getText().toString());
        mEditor.apply();
    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(ProfileActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgressBar.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot.getMetadata() != null) {
                        Task<Uri> result1 = taskSnapshot.getStorage().getDownloadUrl();
                        result1.addOnSuccessListener(uri -> {
                            Toast.makeText(ProfileActivity.this, "Profile image uploaded successfully...", Toast.LENGTH_SHORT).show();
                            user.setImage(uri.toString());
                            rootRef.child("Users").child(currentUserId).child("image").setValue(user.getImage())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            mProgressBar.setVisibility(View.GONE);
                                            retrieveUserProfilePic();
                                        }
                                    });
                        });
                    }
                    else {
                        String message = taskSnapshot.getError().toString();
                        Toast.makeText(ProfileActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void retrieveUserProfilePic() {
        if ((user.getImage() != null) && (!user.getImage().equals(""))) {
            Glide.with(getApplicationContext()).asBitmap().load(user.getImage()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    mUserProfileImage.setImageBitmap(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    Glide.with(getApplicationContext()).load(placeholder).into(mUserProfileImage);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProfileActivityInProgress)
            setSharedPreferences();
    }

    private void updateProfile() {
        user.setName(mUserName.getText().toString());
        user.setStatus(mUserStatus.getText().toString());

        if ((user.getName().isEmpty()) || (user.getName().equals(""))) {
            Toast.makeText(this, "User name is missing...", Toast.LENGTH_SHORT).show();
        }
        else if ((user.getStatus().isEmpty()) || (user.getStatus().equals(""))) {
            Toast.makeText(this, "Status is missing...", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("name", user.getName());
            profileMap.put("status", user.getStatus());
            rootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else {
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        mSharedPreferences.edit().putBoolean(Profile_State, false).apply();
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }
}
