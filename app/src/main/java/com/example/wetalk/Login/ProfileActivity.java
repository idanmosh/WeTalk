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
import com.example.wetalk.Classes.AppDir;
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

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

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
    private static final String Main_State = "mainState";

    private boolean mProfileActivityInProgress;

    private AppDir appDir;
    private Uri resultUri;
    private User user;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRef;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fadeActivity();

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
        showPermissionsDialog();

        getSharedPreferences();
        retrieveUserProfilePic();

        nextProfileBtn = findViewById(R.id.next_profile_btn);

        nextProfileBtn.setOnClickListener(v -> updateProfile());

        mUserProfileImage.setOnClickListener(v -> {
            if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.CAMERA))
                cropImage();
            else
                Permissions.ProfileImagePermissionDialog(this, ProfileActivity.this);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE))
            appDir = new AppDir();
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
        }
    }

    private void getSharedPreferences() {
        mProfileActivityInProgress = mSharedPreferences.getBoolean(Profile_State, false);
        user.setImage(mSharedPreferences.getString(IMAGE_KEY,null));
        mUserName.setText(mSharedPreferences.getString(NAME_KEY, null));
        mUserStatus.setText(mSharedPreferences.getString(STATUS_KEY, null));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgressBar.setVisibility(View.VISIBLE);

                resultUri = Objects.requireNonNull(result).getUri();

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
                                            Toast.makeText(ProfileActivity.this, "Profile image uploaded successfully...", Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    });
                        });
                    }
                    else {
                        String message = Objects.requireNonNull(taskSnapshot.getError()).toString();
                        Toast.makeText(ProfileActivity.this, getString(R.string.ERROR) + message, Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void retrieveUserProfilePic() {
        user.setImage(mSharedPreferences.getString(IMAGE_KEY, null));

        if (user.getImage() == null) {
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
    protected void onPause() {
        super.onPause();
        if (mProfileActivityInProgress)
            setSharedPreferences();
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
        if (mProfileActivityInProgress)
            setSharedPreferences();
    }

    private void updateProfile() {
        if (mUserName.getText().toString().isEmpty())
            Toast.makeText(this, "User name is missing...", Toast.LENGTH_SHORT).show();
        else if (mUserStatus.getText().toString().isEmpty()) {
            Toast.makeText(this, "Status is missing...", Toast.LENGTH_SHORT).show();
        }
        else {
            mSharedPreferences.edit().putString(STATUS_KEY, mUserStatus.getText().toString()).apply();
            mSharedPreferences.edit().putString(NAME_KEY, mUserName.getText().toString()).apply();
            if (user.getImage() != null)
                mSharedPreferences.edit().putString(IMAGE_KEY, user.getImage()).apply();
            mSharedPreferences.edit().putBoolean(Profile_State, false).apply();
            sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {
        mSharedPreferences.edit().putBoolean(Profile_State, false).apply();
        mSharedPreferences.edit().putBoolean(Main_State, true).apply();
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }

    private void fadeActivity() {
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.main_app_bar), true);
        fade.excludeTarget(decor.findViewById(R.id.main_page_toolbar), true);
        fade.excludeTarget(decor.findViewById(R.id.AppBarLayout), true);
        fade.excludeTarget(decor.findViewById(R.id.main_tabs),true);
        fade.excludeTarget(decor.findViewById(R.id.settings_page_toolbar),true);
        fade.excludeTarget(decor.findViewById(R.id.shared_toolbar),true);
        fade.excludeTarget(android.R.id.statusBarBackground,true);
        fade.excludeTarget(android.R.id.navigationBarBackground,true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }
}
