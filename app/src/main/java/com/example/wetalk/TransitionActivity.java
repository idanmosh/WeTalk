package com.example.wetalk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.Login.HelloActivity;
import com.example.wetalk.Login.LoginActivity;
import com.example.wetalk.Login.ProfileActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TransitionActivity extends AppCompatActivity {

    private static int WELCOME_TIMEOUT = 1000;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String Login_State = "loginState";
    private static final String Profile_State = "profileState";
    private static final String Main_State = "mainState";
    private static final String IMAGE_KEY = "image_key";


    private SharedPreferences mSharedPreferences;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private boolean mLoginState;
    private boolean mProfileState;
    private boolean mMainState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        fadeActivity();
        mSharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        AccountManager accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        assert accountManager != null;
        Account[] account = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        if (account.length == 0) {
            mSharedPreferences.edit().putBoolean(Main_State, false).apply();
            mSharedPreferences.edit().putString(IMAGE_KEY, null).apply();
        }
        getSharedPreferences();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    private void getSharedPreferences() {
        mLoginState = mSharedPreferences.getBoolean(Login_State, false);
        mProfileState = mSharedPreferences.getBoolean(Profile_State, false);
        mMainState = mSharedPreferences.getBoolean(Main_State,false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mLoginState)
            sendUserToLoginActivity();
        else if (mProfileState)
            sendUserToProfileActivity();
        else if(mMainState) {
            sendUserToMainActivity();
        }
        else
            sendUserToHelloActivity();
    }

    private void sendUserToProfileActivity() {
        new Handler().postDelayed(() -> {
            Intent helloIntent = new Intent(TransitionActivity.this, ProfileActivity.class);
            helloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(helloIntent);
            overridePendingTransition(new Fade().getMode(), new Fade().getMode());
            finish();
        },WELCOME_TIMEOUT);
    }

    private void sendUserToHelloActivity() {
        mSharedPreferences.edit().clear().apply();
        new Handler().postDelayed(() -> {
            Intent helloIntent = new Intent(TransitionActivity.this, HelloActivity.class);
            helloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(helloIntent);
            overridePendingTransition(new Fade().getMode(), new Fade().getMode());
            finish();
        },WELCOME_TIMEOUT);
    }

    private void sendUserToLoginActivity() {
        new Handler().postDelayed(() -> {
            Intent loginIntent = new Intent(TransitionActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            overridePendingTransition(new Fade().getMode(), new Fade().getMode());
            finish();
        },WELCOME_TIMEOUT);
    }

    private void sendUserToMainActivity() {

        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(TransitionActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            overridePendingTransition(new Fade().getMode(), new Fade().getMode());
            finish();
        },WELCOME_TIMEOUT);
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
