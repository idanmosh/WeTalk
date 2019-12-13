package com.example.wetalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private boolean mVerificationInProgress = false;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    FirebaseUser currentUser;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private CountryCodePicker ccp;

    private Button mSend, mVerify;
    private EditText mPhoneNumber, mVerificationCode;
    private LinearLayout mSendLayout, mVerifyLayout;
    private ProgressDialog loadingBar;

    private String mVerificationId, phoneNumber, verificationCode;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

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

        ccp = findViewById(R.id.ccp);
        ccp.setDefaultCountryUsingNameCode("IL");
        ccp.resetToDefaultCountry();

        mSend = findViewById(R.id.send_code_btn);
        mVerify = findViewById(R.id.verify_code_btn);
        mPhoneNumber = findViewById(R.id.phone_number);
        mVerificationCode = findViewById(R.id.code);
        mSendLayout = findViewById(R.id.send_layout);
        mVerifyLayout = findViewById(R.id.verify_layout);

        loadingBar = new ProgressDialog(this);

        mSend.setOnClickListener(v -> {

            phoneNumber = ccp.getSelectedCountryCodeWithPlus() + mPhoneNumber.getText().toString();

            if (!phoneNumber.isEmpty()) {
                loadingBar.setTitle("Phone Verification");
                loadingBar.setMessage("please wait while we authenticating your phone...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                startPhoneNumberVerification(phoneNumber);
            }
            else
                Toast.makeText(LoginActivity.this, "Please enter your phone number first...", Toast.LENGTH_SHORT).show();

        });

        mVerify.setOnClickListener(v -> {
            mVerifyLayout.setVisibility(View.INVISIBLE);

            verificationCode = mVerificationCode.getText().toString();

            if (!verificationCode.isEmpty()) {
                loadingBar.setTitle("Verification Code");
                loadingBar.setMessage("please wait while we verifying verification code...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }
            else {
                Toast.makeText(LoginActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);

                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                mSendLayout.setVisibility(View.INVISIBLE);
                mVerifyLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                mVerificationInProgress = false;
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mVerificationInProgress = false;
                Toast.makeText(LoginActivity.this, "Invalid phone number, please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                mSendLayout.setVisibility(View.VISIBLE);
                mVerifyLayout.setVisibility(View.INVISIBLE);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(ccp.getSelectedCountryCodeWithPlus() + mPhoneNumber.getText().toString());
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumber.getText().toString();
        return TextUtils.isEmpty(phoneNumber);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> loginMap = new HashMap<>();
                        loginMap.put("phone", phoneNumber);
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        rootRef.child("Users").child(currentUserId).updateChildren(loginMap)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "You are logged in successfully...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        sendUserToSettingsActivity();
                                    }
                                    else {
                                        String message = task1.getException().toString();
                                        Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(LoginActivity.this, SettingsActivity.class);
        settingsIntent.putExtra("FLAG", true);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }
}
