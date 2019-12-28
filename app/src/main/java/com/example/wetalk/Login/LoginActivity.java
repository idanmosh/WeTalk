package com.example.wetalk.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.Classes.FadeClass;
import com.example.wetalk.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String DEFAULT_COUNTRY = "IL";
    private final static String VERIFY_TITLE = "verify_title";
    private final static String USERS = "Users";
    private final static String PHONE = "phone";
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String Login_State = "loginState";
    private static final String Profile_State = "profileState";
    private static final String VerificationId_Key = "verificationId_key";
    private static final String PHONE_KEY = "phone_key";

    private boolean mVerificationInProgress;

    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private CountryCodePicker ccp;
    private Button mSend, mVerify;
    private EditText mPhoneNumber, mVerificationCode;
    private TextView mTitle;
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
        ccp.setDefaultCountryUsingNameCode(DEFAULT_COUNTRY);
        ccp.resetToDefaultCountry();

        mSend = findViewById(R.id.send_code_btn);
        mVerify = findViewById(R.id.verify_code_btn);
        mPhoneNumber = findViewById(R.id.phone_number);
        mVerificationCode = findViewById(R.id.code);
        mSendLayout = findViewById(R.id.send_layout);
        mVerifyLayout = findViewById(R.id.verify_layout);
        mTitle = findViewById(R.id.login_title);

        mSharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        getSharedPreferences();
        if (mVerificationInProgress) {
            mSendLayout.setVisibility(View.INVISIBLE);
            mVerifyLayout.setVisibility(View.VISIBLE);
        }

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        FadeClass fadeClass = new FadeClass(decor);
        fadeClass.initFade();

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        loadingBar = new ProgressDialog(this);

        mSend.setOnClickListener(v -> {

            if (mPhoneNumber.getText().toString().length() == 10)
                phoneNumber = ccp.getSelectedCountryCodeWithPlus() + mPhoneNumber.getText().toString().substring(1);
            else
                phoneNumber = ccp.getSelectedCountryCodeWithPlus() + mPhoneNumber.getText().toString();

            if ((!mPhoneNumber.getText().toString().isEmpty()) && (mPhoneNumber.getText().toString().length() >= 9)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage("We are about to verify the number:\n\n" + phoneNumber +
                        "\n\nContinue or do you want to edit the number?");
                builder.setCancelable(false);

                builder.setPositiveButton("Confirm", (dialog, which) -> {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait while we authenticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    startPhoneNumberVerification();
                });

                builder.setNeutralButton("Edit", (dialog, which) -> dialog.cancel());
                AlertDialog dialog = builder.create();
                dialog.show();
            } else
                Toast.makeText(LoginActivity.this, R.string.PLEASE_ENTER_VALID, Toast.LENGTH_SHORT).show();
        });

        mVerify.setOnClickListener(v -> {
            verificationCode = mVerificationCode.getText().toString();

            if (!verificationCode.isEmpty()) {
                loadingBar.setTitle(getString(R.string.VERIFICATION_CODE));
                loadingBar.setMessage(getString(R.string.PLEASE_WAIT));
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            } else {
                Toast.makeText(LoginActivity.this, R.string.WRITE_VERIFICATION_CODE, Toast.LENGTH_SHORT).show();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);

                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, R.string.CODE_HAS_BEEN_SENT, Toast.LENGTH_SHORT).show();

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
                Toast.makeText(LoginActivity.this, R.string.INVALID_PHONE_NUMBER, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                mSendLayout.setVisibility(View.VISIBLE);
                mVerifyLayout.setVisibility(View.INVISIBLE);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSharedPreferences();
    }

    private void startPhoneNumberVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        mSharedPreferences.edit().putString(PHONE_KEY, phoneNumber).apply();
        mVerificationInProgress = true;
        mTitle.setText(String.format("Verify  %s", phoneNumber));
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> loginMap = new HashMap<>();
                        loginMap.put(PHONE, phoneNumber);
                        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        rootRef.child(USERS).child(currentUserId).updateChildren(loginMap)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, R.string.YOUR_LOGGED_IN_SUCCESS, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        mSharedPreferences.edit().putBoolean(Login_State, false).apply();
                                        mVerificationInProgress = false;
                                        sendUserToProfileActivity();
                                    }
                                    else {
                                        String message = Objects.requireNonNull(task1.getException()).toString();
                                        Toast.makeText(LoginActivity.this, getString(R.string.ERROR) + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        String message = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                });
    }

    private void getSharedPreferences() {
        mVerificationInProgress = mSharedPreferences.getBoolean(Login_State, false);
        mVerificationId = mSharedPreferences.getString(VerificationId_Key, null);
        mTitle.setText(mSharedPreferences.getString(VERIFY_TITLE, getString(R.string.ENTER_YOUR_PHONE_NUMBER)));
    }

    private void setSharedPreferences() {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(Login_State, mVerifyLayout.isShown());
        mEditor.putString(VerificationId_Key, mVerificationId);
        mEditor.putString(VERIFY_TITLE, mTitle.getText().toString());
        mEditor.apply();
    }

    private void sendUserToProfileActivity() {
        mSharedPreferences.edit().putBoolean(Profile_State, true).apply();
        Intent helloIntent = new Intent(LoginActivity.this, ProfileActivity.class);
        helloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(helloIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVerificationInProgress)
            setSharedPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVerificationInProgress)
            setSharedPreferences();
    }

    @Override
    public void onBackPressed() {
        if (!mVerificationInProgress)
            super.onBackPressed();
    }
}
