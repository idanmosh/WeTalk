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
import com.example.wetalk.Permissions.Permissions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final ContentResolver mResolver;
    private Context mContext;
    private DBHandler contactsDB;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mResolver = context.getContentResolver();
        FirebaseApp.initializeApp(context);
        contactsDB = new DBHandler(mContext);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mResolver = context.getContentResolver();
        FirebaseApp.initializeApp(context);
        contactsDB = new DBHandler(mContext);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        syncContacts(account,extras,authority,provider,syncResult);

    }

    private void syncContacts(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        List<Contact> tempContactsList;
        DatabaseReference rootRef;
        HashMap<String, String> phoneTable = new HashMap<>();

        tempContactsList = new ArrayList<>();
        rootRef = FirebaseDatabase.getInstance().getReference();

        if (Permissions.checkPermissions(mContext, Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS)) {
            return;
        }

        Cursor cursor = mResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = mResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[] {id}, null);

                    while (cursorInfo.moveToNext()) {
                        String phone = generatePhoneNumber(
                                cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        if (!phoneTable.containsKey(phone)) {
                            phoneTable.put(phone, phone);
                            Contact contact = new Contact();
                            contact.setRawId(id);
                            contact.setPhone(cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                            tempContactsList.add(contact);
                        }
                    }
                    cursorInfo.close();
                }
            }
            cursor.close();
        }

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
                                                if (contactsDB.getContactByPhone(tempContactsList.get(i).getPhone()) == null) {
                                                    contactsDB.addContact(tempContactsList.get(i));
                                                    addContact(tempContactsList.get(i));
                                                }
                                                else {
                                                    contactsDB.updateContact(tempContactsList.get(i));
                                                }
                                            }
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

    private void deleteContact(Contact contact) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        Cursor cursor =  mResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                null,
                ContactsContract.RawContacts.ACCOUNT_TYPE + " =?",
                new String[] {AccountGeneral.ACCOUNT_TYPE},
                null);

        while (cursor.moveToNext()) {
            Uri rawUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();

            ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).
                    withSelection(ContactsContract.RawContacts._ID + "=? AND "
                                    +ContactsContract.RawContacts.ACCOUNT_TYPE+ "=? AND "
                                    +ContactsContract.RawContacts.ACCOUNT_NAME+ "=?"
                            ,new String[] {cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID)),
                                    AccountGeneral.ACCOUNT_TYPE,AccountGeneral.ACCOUNT_NAME}).build()); //sets deleted flag to 1

            ops.add(ContentProviderOperation.newDelete(rawUri).
                    withSelection(ContactsContract.RawContacts._ID + "=? AND "
                                    +ContactsContract.RawContacts.ACCOUNT_TYPE+ "=? AND "
                                    +ContactsContract.RawContacts.ACCOUNT_NAME+ "=?"
                            ,new String[] {cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID)),
                                    AccountGeneral.ACCOUNT_TYPE,AccountGeneral.ACCOUNT_NAME}).build());

            try {
                mResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        cursor.close();
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
        StringBuilder phone = new StringBuilder();

        if (number.length() > 9)
            number = number.substring(1);

        for (int i=0; i < number.length();i++) {
            if (i == 0 && number.charAt(i) == '+')
                phone.append(number.charAt(i));

            if (number.charAt(i) >= '0' && number.charAt(i) <= '9')
                phone.append(number.charAt(i));
        }
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String SIMCountryISO = Objects.requireNonNull(tm).getSimCountryIso().toUpperCase();
        String countryCode = "+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(SIMCountryISO);

        if (!phone.toString().contains(countryCode))
            phone.insert(0, countryCode);

        return phone.toString();
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
