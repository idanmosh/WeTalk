package com.example.wetalk.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.example.wetalk.Classes.FadeClass;
import com.example.wetalk.Classes.User;
import com.example.wetalk.MainActivity;
import com.example.wetalk.Permissions.Permissions;
import com.example.wetalk.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PROFILE_REQUEST_CODE = 100;

    private EditText mUserName, mUserStatus;
    private CircleImageView mUserProfileImage;
    private Button nextProfileBtn;
    private ProgressBar mProgressBar;

    private SharedPreferences mSharedPreferences;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String NAME_KEY = "name_key";
    private static final String IMAGE_KEY = "image_key";
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
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
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
        waitImageLoad(3);

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        FadeClass fadeClass = new FadeClass(decor);
        fadeClass.initFade();

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        nextProfileBtn = findViewById(R.id.next_profile_btn);

        nextProfileBtn.setOnClickListener(v -> updateProfile());

        mUserProfileImage.setOnClickListener(v -> {
            if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.CAMERA))
                cropImage();
            else
                Permissions.ProfileImagePermissionDialog(this, ProfileActivity.this);
        });

        showPermissionsDialog();
    }

    private void showPermissionsDialog() {
        Permissions.ProfilePermissionsDialog(this, ProfileActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Permissions.IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cropImage();
            }
            else {
                Toast.makeText(this, "You can't get access to camera and media storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getSharedPreferences() {
        mProfileActivityInProgress = mSharedPreferences.getBoolean(Profile_State, false);
        user.setImage(mSharedPreferences.getString(IMAGE_KEY,""));
        mUserName.setText(mSharedPreferences.getString(NAME_KEY, ""));
        mUserStatus.setText(mSharedPreferences.getString(STATUS_KEY, ""));
    }

    private void setSharedPreferences() {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
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

    private void waitImageLoad(int time) {
        if (time == 1){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgressBar.setVisibility(View.VISIBLE);

                Uri resultUri = Objects.requireNonNull(result).getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserId + getString(R.string.JPG));

                filePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot.getMetadata() != null) {
                        Task<Uri> result1 = taskSnapshot.getStorage().getDownloadUrl();
                        result1.addOnSuccessListener(uri -> {
                            user.setImage(uri.toString());
                            mSharedPreferences.edit().putString(IMAGE_KEY, uri.toString()).apply();
                            rootRef.child(getString(R.string.USERS)).child(currentUserId).child(getString(R.string.IMAGE)).setValue(user.getImage())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            loadImage();
                                            waitImageLoad(3);
                                            Toast.makeText(ProfileActivity.this, "Profile image uploaded successfully...", Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    });
                        });
                    }
                    else {
                        String message = Objects.requireNonNull(taskSnapshot.getError()).toString();
                        Toast.makeText(ProfileActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void retrieveUserProfilePic() {
        user.setImage(mSharedPreferences.getString(IMAGE_KEY,""));

        if (user.getImage().equals("")) {
            rootRef.child(getString(R.string.USERS)).child(currentUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild(getString(R.string.IMAGE))) {
                                    user.setImage(Objects.requireNonNull(dataSnapshot.child(getString(R.string.IMAGE)).getValue()).toString());
                                    mSharedPreferences.edit().putString(IMAGE_KEY, user.getImage()).apply();
                                    loadImage();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
        }
        else {
            loadImage();
        }
    }

    private void loadImage() {
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mProfileActivityInProgress)
            setSharedPreferences();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void updateProfile() {
        user.setName(mUserName.getText().toString());
        user.setStatus(mUserStatus.getText().toString());

        if ((user.getName().isEmpty()) || (user.getName().equals(""))) {
            Toast.makeText(this, R.string.USER_NAME_MISSING, Toast.LENGTH_SHORT).show();
        }
        else if ((user.getStatus().isEmpty()) || (user.getStatus().equals(""))) {
            Toast.makeText(this, R.string.STATUS_MISSING, Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(getString(R.string.NAME), user.getName());
            profileMap.put(getString(R.string.STATUS), user.getStatus());
            rootRef.child(getString(R.string.USERS)).child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, R.string.PROFILE_UPDATED, Toast.LENGTH_SHORT).show();
                            waitImageLoad(1);
                            sendUserToMainActivity();
                        }
                        else {
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(ProfileActivity.this, getString(R.string.ERROR) + message, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        mSharedPreferences.edit().clear().apply();
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }
}
