package com.example.wetalk.Settings;

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

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button updateProfileBtn;
    private EditText mUserName, mUserStatus;
    private TextView mUserPhoneNumber;
    private CircleImageView mUserProfileImage;
    private ImageView mUserProfileImage2;

    private SharedPreferences mSharedPreferences;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String NAME_KEY = "name_key";
    private static final String IMAGE_KEY = "image_key";
    private static final String STATUS_KEY = "status_key";
    private static final String PHONE_KEY = "phone_key";

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

        mSharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        user = new User();
        retrieveUserInfo();
        retrieveUserPic();

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

    private void loadImage() {
        if (user.getImage() != null) {
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
    }

    private void retrieveUserInfo() {
        mUserName.setText(mSharedPreferences.getString(NAME_KEY, null));
        mUserStatus.setText(mSharedPreferences.getString(STATUS_KEY,null));
        mUserPhoneNumber.setText(mSharedPreferences.getString(PHONE_KEY, null));
    }

    private void updateSettings() {
        if (mUserName.getText().toString().isEmpty())
            Toast.makeText(this, "User name is missing...", Toast.LENGTH_SHORT).show();
        else if (mUserStatus.getText().toString().isEmpty()) {
            Toast.makeText(this, "Status is missing...", Toast.LENGTH_SHORT).show();
        }
        else {
            mSharedPreferences.edit().putString(STATUS_KEY, mUserStatus.getText().toString()).apply();
            mSharedPreferences.edit().putString(NAME_KEY, mUserName.getText().toString()).apply();
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
        overridePendingTransition(R.anim.slide_down, R.anim.slide_down);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainActivity();
    }
}
