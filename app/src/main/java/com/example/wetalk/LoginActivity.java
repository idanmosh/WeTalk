package com.example.wetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
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

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;
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

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneNumber = ccp.getSelectedCountryCodeWithPlus() + mPhoneNumber.getText().toString();

                if (!phoneNumber.isEmpty()) {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait while we authenticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    startPhoneNumberVerification();
                }
                else
                    Toast.makeText(LoginActivity.this, "Please enter your phone number first...", Toast.LENGTH_SHORT).show();

            }
        });

        mVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Invalid phone number, please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show();

                mSendLayout.setVisibility(View.VISIBLE);
                mVerifyLayout.setVisibility(View.INVISIBLE);
            }
        };
    }

    private void startPhoneNumberVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, Object> loginMap = new HashMap<>();
                            loginMap.put("phone", phoneNumber);
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(currentUserId).updateChildren(loginMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loadingBar.dismiss();
                                                Toast.makeText(LoginActivity.this, "You are logged in successfully...", Toast.LENGTH_SHORT).show();
                                                sendUserToMainActivity();
                                            }
                                            else {
                                                String message = task.getException().toString();
                                                Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser != null)
            sendUserToMainActivity();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
