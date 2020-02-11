package com.example.wetalk;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.DBHandler;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final ContentResolver mResolver;
    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mResolver = context.getContentResolver();
        FirebaseApp.initializeApp(context);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mResolver = context.getContentResolver();
        FirebaseApp.initializeApp(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        syncContacts(account,extras,authority,provider,syncResult);

    }

    private void syncContacts(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        List<Contact> tempContactsList;
        DatabaseReference rootRef;
        DBHandler contactsDB = new DBHandler(mContext);

        tempContactsList = new ArrayList<>();
        rootRef = FirebaseDatabase.getInstance().getReference();

        Cursor cursor = mResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (Objects.requireNonNull(cursor).getCount() > 0 && cursor.moveToFirst()) {

            while (cursor.moveToNext()) {
                Contact contact = new Contact();
                contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                contact.setRawId(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));

                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = mResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?",
                            new String[]{contact.getRawId()}, null);

                    if (Objects.requireNonNull(cursorInfo).getCount() > 0 && cursorInfo.moveToFirst()) {

                        while (cursorInfo.moveToNext()) {
                            String phone = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if (phone != null) {
                                if (isValidNumber(phone)) {
                                    contact.setPhone(cursorInfo.getString(
                                            cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                                    tempContactsList.add(contact);
                                }
                            }
                            break;
                        }
                        cursorInfo.close();
                    }
                }
            }
            cursor.close();

            rootRef.child("Contacts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    rootRef.child(mContext.getString(R.string.USERS)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            if (dataSnapshot1.exists()) {
                                for (int i = 0; i < tempContactsList.size(); i++) {
                                    if (dataSnapshot1.hasChild(generatePhoneNumber(tempContactsList.get(i).getPhone()))) {
                                        tempContactsList.get(i).setUserId(Objects.requireNonNull(dataSnapshot1.child(
                                                generatePhoneNumber(tempContactsList.get(i).getPhone())).getValue()).toString());

                                        if (tempContactsList.get(i).getUserId() != null) {
                                            if (dataSnapshot2.exists()) {
                                                if (dataSnapshot2.hasChild(tempContactsList.get(i).getUserId())) {
                                                    tempContactsList.get(i).setStatus(Objects.requireNonNull(dataSnapshot2.child(
                                                            tempContactsList.get(i).getUserId()).child(mContext.getString(R.string.STATUS)).getValue()).toString());

                                                    tempContactsList.get(i).setImage(Objects.requireNonNull(dataSnapshot2.child(
                                                            tempContactsList.get(i).getUserId()).child(mContext.getString(R.string.IMAGE)).getValue()).toString());
                                                }
                                                addContact(tempContactsList.get(i));
                                                contactsDB.addContact(tempContactsList.get(i));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            databaseError.getMessage();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.getMessage();
                }
            });
        }
    }

    private void addContact(Contact contact) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(
                        ContactsContract.RawContacts.CONTENT_URI, true))
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
                        AccountGeneral.ACCOUNT_NAME)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
                        AccountGeneral.ACCOUNT_TYPE)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                        ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());

        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(
                        ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                .build());

        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(
                        ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone())
                .build());

        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(
                        ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                .withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/com.example.wetalk.profile")
                .withValue(ContactsContract.Data.DATA1, contact.getPhone())
                .withValue(ContactsContract.Data.DATA2, contact.getName())
                .withValue(ContactsContract.Data.DATA3, "הודעה אל "+ generatePhoneNumber(contact.getPhone()))
                .withYieldAllowed(true).build());

        try {
            mResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidNumber(String phone) {
        return phone.replaceAll("[0-9+]+","").equals("");
    }

    private String generatePhoneNumber(String number) {
        String phone = number;

        phone = phone.replaceAll("[ -]+", "");
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String SIMCountryISO = Objects.requireNonNull(tm).getSimCountryIso().toUpperCase();
        String countryCode = "+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(SIMCountryISO);

        if (!phone.contains(countryCode))
            phone = countryCode + phone;

        return phone;
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                            "true").build();
        }
        return uri;
    }

    public static void performSync() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(),
                ContactsContract.AUTHORITY, extras);
    }

}
