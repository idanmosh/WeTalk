package com.example.wetalk;

import android.os.Bundle;
import android.transition.Fade;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetalk.Classes.Contacts;

import java.util.List;
import java.util.Objects;

public class FindContactActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView contactsRecyclerView;
    private List<Contacts> contactsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_contacts);

        fadeActivity();

        contactsRecyclerView = findViewById(R.id.contacts_recycler_list);
        ContactsRecyclerViewAdapter contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(getApplicationContext(), contactsList);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);

        mToolBar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Select Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
