package com.example.wetalk.Login;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.R;

public class HelloActivity extends AppCompatActivity {

    private Button mContinueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        fadeActivity();

        mContinueBtn = findViewById(R.id.continue_btn);

        mContinueBtn.setOnClickListener(v -> sendUserToLoginActivity());


    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(HelloActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
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
