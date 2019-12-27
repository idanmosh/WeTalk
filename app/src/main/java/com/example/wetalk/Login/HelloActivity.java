package com.example.wetalk.Login;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.Classes.FadeClass;
import com.example.wetalk.R;

public class HelloActivity extends AppCompatActivity {

    private Button mContinueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        mContinueBtn = findViewById(R.id.continue_btn);

        mContinueBtn.setOnClickListener(v -> sendUserToLoginActivity());

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        FadeClass fadeClass = new FadeClass(decor);
        fadeClass.initFade();

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(HelloActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }

}
