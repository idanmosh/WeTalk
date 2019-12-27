package com.example.wetalk;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.wetalk.Classes.FadeClass;
import com.example.wetalk.Settings.SettingsActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final static String PROFILE_IMAGE = "Profile Images";
    private final static String TITLE = "WeTalk";
    private final static String USERS = "Users";

    private Toolbar mToolbar, mSearchToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRef;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    private void initialize() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child(PROFILE_IMAGE);

        mToolbar = findViewById(R.id.main_page_toolbar);
        mSearchToolbar = findViewById(R.id.search_page_toolbar);
        setSupportActionBar(mSearchToolbar);
        setSupportActionBar(mToolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle(TITLE);

        mViewPager = findViewById(R.id.main_tabs_pager);
        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccessorAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        FadeClass fadeClass = new FadeClass(decor);
        fadeClass.initFade();

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                sendUserToSettingsActivity();
                return true;
            case R.id.delete_account:
                deleteUserData();
                return true;
            case R.id.new_group:
                return true;
            case R.id.search:
                sendUserToFindFriendsActivity();
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteUserData() {
        if (currentUser != null) {
            rootRef.child(USERS).child(currentUser.getUid()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    StorageReference filePath = userProfileImageRef.child(currentUser.getUid() + getString(R.string.JPG));
                    filePath.delete().addOnCompleteListener(task1 -> deleteUserAccount());
                }
            });
        }
    }

    private void deleteUserAccount(){
        currentUser.delete()
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Toast.makeText( MainActivity.this, "User account deleted.", Toast.LENGTH_SHORT).show();
                        sendUserToTransitionActivity();
                    }
                });
    }

    private void sendUserToTransitionActivity() {
        Intent transitionIntent = new Intent(MainActivity.this, TransitionActivity.class);
        transitionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(transitionIntent);
        overridePendingTransition(new Fade().getMode(), new Fade().getMode());
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }
    private void sendUserToFindFriendsActivity() {
        Intent findsFriends = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findsFriends);
    }
}
