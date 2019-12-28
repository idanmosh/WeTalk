package com.example.wetalk.Settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wetalk.Classes.FadeClass;
import com.example.wetalk.Classes.User;
import com.example.wetalk.Permissions.Permissions;
import com.example.wetalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ProfileImageActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView mImage;
    private CircleImageView mImage2;

    private SharedPreferences mSharedPreferences;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String IMAGE_KEY = "image_key";
    private Uri resultUri;

    private User user;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mImage = findViewById(R.id.shared_profile_image);
        mImage2 = findViewById(R.id.circle_shred_profile_image);

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        FadeClass fadeClass = new FadeClass(decor);
        fadeClass.initFade();

        mSharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        user = new User();

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        retrieveUserProfilePic();

        mToolbar = findViewById(R.id.shared_toolbar);
        setSupportActionBar(mToolbar);

        loadingBar = new ProgressDialog(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mToolbar.setNavigationOnClickListener(v -> sendUserToSettingsActivity());
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
        if (user.getImage() != null) {
            Glide.with(getApplicationContext()).asBitmap().load(user.getImage()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    mImage.setImageBitmap(resource);
                    mImage2.setImageBitmap(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    Glide.with(getApplicationContext()).load(placeholder).into(mImage);
                    Glide.with(getApplicationContext()).load(placeholder).into(mImage2);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_picture_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile_picture:
                checkPermissions();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void checkPermissions() {
        if (!Permissions.checkPermissions(this, Permissions.READ_STORAGE, Permissions.CAMERA))
            cropImage();
        else
            Permissions.ProfileImagePermissionDialog(this, ProfileImageActivity.this);
    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(ProfileImageActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle(getString(R.string.SET_PROFILE_IMAGE));
                loadingBar.setMessage(getString(R.string.PLEASE_WAIT_PROFILE_UPDATED));
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                resultUri = Objects.requireNonNull(result).getUri();
                mSharedPreferences.edit().putString(IMAGE_KEY, resultUri.toString()).apply();
                user.setImage(resultUri.toString());
                loadImage();
                loadingBar.dismiss();
                Toast.makeText(ProfileImageActivity.this, R.string.PROFILE_IMAGE_UPLOADED_SUCCESS, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendUserToSettingsActivity() {
        mImage.setVisibility(INVISIBLE);
        mImage2.setVisibility(VISIBLE);
        finishAfterTransition();
    }

}
