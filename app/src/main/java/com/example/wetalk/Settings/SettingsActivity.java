package com.example.wetalk.Settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.AppDir;
import com.example.wetalk.Classes.User;
import com.example.wetalk.MainActivity;
import com.example.wetalk.Permissions.Permissions;
import com.example.wetalk.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final String[] GENDER = new String[] {"None", "Male", "Female"};

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private FloatingActionButton share_btn, crop_btn;
    private TextView mUserPhoneNumber, mUserName, mUserStatus, mUserGender;
    private CircleImageView mUserProfileImage;
    private CardView name_cardView, about_cardView, gender_cardView;
    private TextInputEditText input;

    private SharedPreferences mSharedPreferences;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String NAME_KEY = "name_key";
    private static final String IMAGE_KEY = "image_key";
    private static final String STATUS_KEY = "status_key";
    private static final String PHONE_KEY = "phone_key";
    private static final String GENDER_KEY = "gender_key";

    private User user;
    private Uri resultUri;
    private AppDir appDir;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        fadeActivity();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mUserProfileImage = findViewById(R.id.profile_image);
        mUserName = findViewById(R.id.user_name);
        mUserStatus = findViewById(R.id.user_status);
        mUserPhoneNumber = findViewById(R.id.user_telephone);
        mUserGender = findViewById(R.id.user_gender);
        share_btn = findViewById(R.id.share_profile_picture_btn);
        crop_btn = findViewById(R.id.crop_profile_picture_btn);
        mProgressBar = findViewById(R.id.profile_progressbar);
        name_cardView = findViewById(R.id.user_name_card_view);
        about_cardView = findViewById(R.id.user_status_card_view);
        gender_cardView = findViewById(R.id.user_gender_card_view);

        mSharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        user = new User();
        retrieveUserInfo();
        retrieveUserPic();

        mToolbar = findViewById(R.id.settings_page_toolbar);
        setSupportActionBar(mToolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mToolbar.setNavigationOnClickListener(v -> sendUserToMainActivity());

        mUserProfileImage.setOnClickListener(v -> sendUserToProfileImageActivity());

        share_btn.setOnClickListener(v -> checkSharePermissions());

        crop_btn.setOnClickListener(v -> checkCropPermissions());

        name_cardView.setOnClickListener(v -> startNameDialog());

        about_cardView.setOnClickListener(v -> startAboutDialog());

        gender_cardView.setOnClickListener(v -> startGenderDialog());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void startGenderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.PICK_GENDER);
        builder.setSingleChoiceItems(GENDER, getChoice(), (dialogInterface, i) -> {
            mSharedPreferences.edit().putString(GENDER_KEY, GENDER[i]).apply();
            mUserGender.setText(GENDER[i]);
            dialogInterface.dismiss();
        });
        builder.show();
    }

    private int getChoice() {
        if (mUserGender.getText().toString().equals(GENDER[0]))
            return 0;
        else if (mUserGender.getText().toString().equals(GENDER[1]))
            return 1;
        else
            return 2;
    }

    @SuppressLint("InflateParams")
    private void startAboutDialog() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_input_layout, null);
        input = view.findViewById(R.id.input);
        input.setText(mUserStatus.getText().toString());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.TYPE_YOUR_STATUS)
                .setPositiveButton(R.string.SAVE,null)
                .setNegativeButton(R.string.CANCEL,null)
                .setView(view).show();

        Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveBtn.setOnClickListener(v -> {
            if (Objects.requireNonNull(input.getText()).toString().length() > 0) {
                mSharedPreferences.edit().putString(STATUS_KEY, input.getText().toString()).apply();
                mUserStatus.setText(input.getText().toString());
                saveToDb();
                dialog.dismiss();
            }
            else
                Toast.makeText(this, R.string.STATUS_EMPTY, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToDb() {
        rootRef.child(getString(R.string.USERS)).child(currentUserId).child(getString(R.string.STATUS))
                .setValue(mUserStatus.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        return;
                    }
                });
    }

    @SuppressLint("InflateParams")
    private void startNameDialog() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_input_layout, null);
        input = view.findViewById(R.id.input);
        input.setText(mUserName.getText().toString());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.TYPE_YOUR_NAME)
                .setPositiveButton(R.string.SAVE,null)
                .setNegativeButton(R.string.CANCEL,null)
                .setView(view).show();

        Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveBtn.setOnClickListener(v -> {
            if (Objects.requireNonNull(input.getText()).toString().length() > 0) {
                mSharedPreferences.edit().putString(NAME_KEY, input.getText().toString()).apply();
                mUserName.setText(input.getText().toString());
                dialog.dismiss();
            }
            else
                Toast.makeText(this, R.string.NAME_EMPTY, Toast.LENGTH_SHORT).show();
        });
    }

    private void checkSharePermissions() {
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE)) {
            appDir = new AppDir();
            appDir.saveProfileImage(mUserProfileImage);
            shareContent();
        }
        else
            Permissions.ProfileShareImagePermissionDialog(this, SettingsActivity.this);
    }

    private void checkCropPermissions() {
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.CAMERA))
            cropImage();
        else
            Permissions.ProfileImagePermissionDialog(this, SettingsActivity.this);
    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SettingsActivity.this);
    }

    private void shareContent() {
        Uri uri = appDir.getProfileImage();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setType(getString(R.string.IMAGE_TYPE));
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, getString(R.string.SHARE_IMAGE_TITLE)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.WRITE_STORAGE))
            appDir = new AppDir();
        retrieveUserPic();
        retrieveUserInfo();
    }

    private void retrieveUserPic() {
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

        if (requestCode == Permissions.EXTERNAL_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                appDir = new AppDir();
                appDir.saveProfileImage(mUserProfileImage);
                shareContent();
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

                resultUri = Objects.requireNonNull(result).getUri();

                File actualImage = new File(Objects.requireNonNull(resultUri.getPath()));

                try {
                    Bitmap compressedImage = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(50)
                            .compressToBitmap(actualImage);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    byte[] final_image = outputStream.toByteArray();

                    StorageReference filePath = userProfileImageRef.child(currentUserId + getString(R.string.JPG));

                    UploadTask uploadTask = filePath.putBytes(final_image);

                   uploadTask.addOnSuccessListener(taskSnapshot -> {
                        if (taskSnapshot.getMetadata() != null) {
                            Task<Uri> result1 = taskSnapshot.getStorage().getDownloadUrl();
                            result1.addOnSuccessListener(uri -> {
                                mSharedPreferences.edit().putString(IMAGE_KEY, resultUri.toString()).apply();
                                user.setImage(resultUri.toString());
                                rootRef.child(getString(R.string.USERS)).child(currentUserId).child(getString(R.string.IMAGE)).setValue(uri.toString())
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                loadImage();
                                                Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully...", Toast.LENGTH_SHORT).show();
                                                mProgressBar.setVisibility(View.GONE);
                                            }
                                        });
                                mProgressBar.setVisibility(View.GONE);
                            });
                        }
                        else {
                            String message = Objects.requireNonNull(taskSnapshot.getError()).toString();
                            Toast.makeText(SettingsActivity.this, getString(R.string.ERROR) + message, Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadImage() {
        if (user.getImage() != null) {
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

    private void retrieveUserInfo() {
        String phone = mSharedPreferences.getString(PHONE_KEY, null);
        phone = Objects.requireNonNull(phone).substring(0,4) + " " + phone.substring(4,6) + "-" + phone.substring(6,9) + "-" + phone.substring(9);
        mUserName.setText(mSharedPreferences.getString(NAME_KEY, null));
        mUserStatus.setText(mSharedPreferences.getString(STATUS_KEY,null));
        mUserPhoneNumber.setText(phone);
        mUserGender.setText(mSharedPreferences.getString(GENDER_KEY, GENDER[0]));
    }

    private void sendUserToProfileImageActivity() {
        Intent profileIntent = new Intent(SettingsActivity.this, ProfileImageActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SettingsActivity.this,
                mUserProfileImage, Objects.requireNonNull(ViewCompat.getTransitionName(mUserProfileImage)));
        startActivity(profileIntent, optionsCompat.toBundle());
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_down, R.anim.slide_down);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainActivity();
    }

    private void fadeActivity() {
        getWindow().setEnterTransition(null);
        getWindow().setExitTransition(null);
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.main_app_bar), true);
        fade.excludeTarget(decor.findViewById(R.id.profile_image_layout), true);
        fade.excludeTarget(decor.findViewById(R.id.main_page_toolbar), true);
        fade.excludeTarget(decor.findViewById(R.id.AppBarLayout), true);
        fade.excludeTarget(decor.findViewById(R.id.main_tabs),true);
        fade.excludeTarget(decor.findViewById(R.id.settings_page_toolbar),true);
        fade.excludeTarget(decor.findViewById(R.id.shared_toolbar),true);
        fade.excludeTarget(android.R.id.statusBarBackground,true);
        fade.excludeTarget(android.R.id.navigationBarBackground,true);
        fade.excludeTarget(decor.findViewById(R.id.profile_image_layout), true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }
}
