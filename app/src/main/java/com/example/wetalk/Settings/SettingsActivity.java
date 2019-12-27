package com.example.wetalk.Settings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.FadeClass;
import com.example.wetalk.Classes.User;
import com.example.wetalk.MainActivity;
import com.example.wetalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button updateProfileBtn;
    private EditText mUserName, mUserStatus;
    private TextView mUserPhoneNumber;
    private CircleImageView mUserProfileImage;
    private ImageView mUserProfileImage2;

    private Uri profilePic;

    private User user;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getWindow().setEnterTransition(null);
        getWindow().setExitTransition(null);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mUserProfileImage = findViewById(R.id.profile_image);
        mUserProfileImage2 = findViewById(R.id.profile_image2);
        mUserName = findViewById(R.id.set_user_name);
        mUserStatus = findViewById(R.id.set_user_status);
        mUserPhoneNumber = findViewById(R.id.profile_phone_number);

        user = new User();
        retrieveUserInfo();


        updateProfileBtn = findViewById(R.id.update_profile_btn);

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mToolbar.setNavigationOnClickListener(v -> sendUserToMainActivity());

        updateProfileBtn.setOnClickListener(v -> updateSettings());

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        FadeClass fadeClass = new FadeClass(decor);
        fadeClass.initFade();

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        mUserProfileImage.setOnClickListener(v -> sendUserToProfileImageActivity());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadImage() {
        user.setImage(profilePic.toString());
        Glide.with(getApplicationContext()).asBitmap().load(user.getImage()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                mUserProfileImage.setImageBitmap(resource);
                mUserProfileImage2.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                Glide.with(getApplicationContext()).load(placeholder).into(mUserProfileImage);
                Glide.with(getApplicationContext()).load(placeholder).into(mUserProfileImage2);
            }
        });
    }

    private void retrieveUserInfo() {
        rootRef.child(getString(R.string.USERS)).child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild(getString(R.string.IMAGE))) {
                                profilePic =  Uri.parse(Objects.requireNonNull(dataSnapshot.child(getString(R.string.IMAGE)).getValue()).toString());
                                loadImage();
                            }

                            if (dataSnapshot.hasChild(getString(R.string.NAME)) && dataSnapshot.hasChild(getString(R.string.STATUS))) {
                                user.setName(Objects.requireNonNull(dataSnapshot.child(getString(R.string.NAME)).getValue()).toString());
                                user.setStatus(Objects.requireNonNull(dataSnapshot.child(getString(R.string.STATUS)).getValue()).toString());

                                mUserStatus.setText(user.getStatus());
                                mUserName.setText(user.getName());
                            }

                            if (dataSnapshot.hasChild(getString(R.string.PHONE))) {
                                user.setPhone(Objects.requireNonNull(dataSnapshot.child(getString(R.string.PHONE)).getValue()).toString());

                                mUserPhoneNumber.setVisibility(View.VISIBLE);
                                mUserPhoneNumber.setText(user.getPhone());
                            }

                        }
                        else
                            Toast.makeText(SettingsActivity.this, R.string.UPDATE_MESSAGE, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private void updateSettings() {
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
            profileMap.put(getString(R.string.NAME), user.getName());
            profileMap.put(getString(R.string.STATUS), user.getStatus());
            rootRef.child(getString(R.string.USERS)).child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendUserToProfileImageActivity() {
        Intent profileIntent = new Intent(SettingsActivity.this, ProfileImageActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SettingsActivity.this,
                mUserProfileImage, Objects.requireNonNull(ViewCompat.getTransitionName(mUserProfileImage)));
        mUserProfileImage.setVisibility(View.INVISIBLE);
        mUserProfileImage2.setVisibility(View.VISIBLE);
        startActivity(profileIntent, optionsCompat.toBundle());
        mUserProfileImage.setVisibility(View.VISIBLE);
        mUserProfileImage2.setVisibility(View.INVISIBLE);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }
}
