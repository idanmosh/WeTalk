package com.example.wetalk;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.transition.Fade;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetalk.Adapters.ContactsRecyclerViewAdapter;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Permissions.Permissions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FindContactActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ContactsRecyclerViewAdapter.ListItemClickListener   {

    private Toolbar mToolBar;
    private RecyclerView contactsRecyclerView;
    private ContentResolver mResolver;
    private DatabaseReference rootRef;
    private String mUserId;
    private final List<Contact> contactsList = new ArrayList<>();
    private ContactsRecyclerViewAdapter contactsRecyclerViewAdapter;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_contacts);

        mUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mResolver = getContentResolver();
        fadeActivity();

        contactsRecyclerView = findViewById(R.id.contacts_recycler_list);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mToolBar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Select Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBar.setNavigationOnClickListener(v -> sendUserToMainActivity());

        if (!Permissions.checkPermissions(getApplicationContext(), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS))
            getSupportLoaderManager().initLoader(0, null, this);

        contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(
                contactsList,1, this);
        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onStart() {
        super.onStart();
        if (!Permissions.checkPermissions(getApplicationContext(), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS))
            getSupportLoaderManager().initLoader(0, null, this);
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

    private void sendUserToMainActivity() {
        Intent findContactIntent = new Intent(FindContactActivity.this, MainActivity.class);
        findContactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findContactIntent);
        overridePendingTransition(R.anim.slide_down, R.anim.slide_down);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainActivity();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(getApplicationContext(),
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.MIMETYPE + " =?",
                new String[] {"vnd.android.cursor.item/com.example.wetalk.profile"},
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor c) {
        contactsList.clear();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndex(ContactsContract.Data.DATA7));
                String phone = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                String name = c.getString(c.getColumnIndex(ContactsContract.Data.DATA2));
                String userId = c.getString(c.getColumnIndex(ContactsContract.Data.DATA4));
                String image = c.getString(c.getColumnIndex(ContactsContract.Data.DATA5));
                String status = c.getString(c.getColumnIndex(ContactsContract.Data.DATA6));

                if (!mUserId.equals(userId))
                    contactsList.add(new Contact(userId,id,name,phone,status,image));
            }
        }

        contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(
                contactsList,1, this);
        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        contactsList.clear();
    }

    @Override
    public void onItemClick(int position) {
        Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        chatIntent.putExtra("CONTACT", contactsList.get(position));
        startActivity(chatIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        finish();
    }
}
