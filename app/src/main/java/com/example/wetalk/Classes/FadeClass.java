package com.example.wetalk.Classes;

import android.transition.Fade;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.wetalk.R;

public class FadeClass {

    private View decor;

    public FadeClass(@NonNull View decor) {
        this.decor = decor;
    }

    public void initFade() {
        Fade fade = new Fade();
        fade.excludeTarget(decor.findViewById(R.id.main_page_toolbar), true);
        fade.excludeTarget(decor.findViewById(R.id.AppBarLayout), true);
        fade.excludeTarget(decor.findViewById(R.id.main_tabs),true);
        fade.excludeTarget(android.R.id.statusBarBackground,true);
        fade.excludeTarget(android.R.id.navigationBarBackground,true);
    }
}
