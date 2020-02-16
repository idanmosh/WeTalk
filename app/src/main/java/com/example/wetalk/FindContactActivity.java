package com.example.wetalk;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.transition.Fade;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.DBHandler;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.List;
import java.util.Objects;

public class FindContactActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar mToolBar;
    private RecyclerView contactsRecyclerView;
    private List<Contact> tempContactsList;
    private ContentResolver mResolver;
    private DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_contacts);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mResolver = getContentResolver();
        fadeActivity();

        contactsRecyclerView = findViewById(R.id.contacts_recycler_list);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mToolBar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);
        getLoaderManager().initLoader(0, null, this);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Select Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBar.setNavigationOnClickListener(v -> sendUserToMainActivity());
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ContactsContract.RawContacts.CONTENT_URI,
                null,
                ContactsContract.RawContacts.ACCOUNT_TYPE + " =?",
                new String[] {AccountGeneral.ACCOUNT_TYPE},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        DBHandler contactsDB = new DBHandler(this);

        ContactsRecyclerViewAdapter contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(
                FindContactActivity.this, contactsDB.getContacts(),1);
        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);
    }

    private String generatePhoneNumber(String number) {
        StringBuilder phone = new StringBuilder();

        if (number.length() > 9)
            number = number.substring(1);

        for (int i=0; i < number.length();i++) {
            if (i == 0 && number.charAt(i) == '+')
                phone.append(number.charAt(i));

            if (number.charAt(i) >= '0' && number.charAt(i) <= '9')
                phone.append(number.charAt(i));
        }
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String SIMCountryISO = Objects.requireNonNull(tm).getSimCountryIso().toUpperCase();
        String countryCode = "+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(SIMCountryISO);

        if (!phone.toString().contains(countryCode))
            phone.insert(0, countryCode);

        return phone.toString();
    }

    private boolean isValidNumber(String phone) {
        return phone.replaceAll("[0-9+]+","").equals("");
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                            "true").build();
        }
        return uri;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {  }

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
}
