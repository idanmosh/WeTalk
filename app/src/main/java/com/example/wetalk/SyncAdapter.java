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

import androidx.annotation.NonNull;

import com.example.wetalk.Classes.Contact;
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
    private DatabaseReference rootRef;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mResolver = context.getContentResolver();
        FirebaseApp.initializeApp(context);
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mResolver = context.getContentResolver();
        FirebaseApp.initializeApp(context);
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if (!Permissions.checkPermissions(mContext, Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS)) {
            syncContactsWithDb(account,extras,authority,provider,syncResult);
        }
    }

    private void syncContactsWithDb(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        List<Contact> tempContactsList = new ArrayList<>();
        List<Contact> appContactsList = new ArrayList<>();
        HashMap<String, String> phoneTable = new HashMap<>();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        Cursor cursor = mResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        assert cursor != null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = mResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[] {id}, null);

                    assert cursorInfo != null;
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

        Cursor c = mResolver.query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.MIMETYPE + " =?",
                new String[] {"vnd.android.cursor.item/com.example.wetalk.profile"},
                null);

        assert c != null;
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndex(ContactsContract.Data.DATA7));
                String phone = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                String name = c.getString(c.getColumnIndex(ContactsContract.Data.DATA2));
                String userId = c.getString(c.getColumnIndex(ContactsContract.Data.DATA4));
                String image = c.getString(c.getColumnIndex(ContactsContract.Data.DATA5));
                String status = c.getString(c.getColumnIndex(ContactsContract.Data.DATA6));

                appContactsList.add(new Contact(userId,id,name,phone,status,image));
            }
        }
        c.close();

        rootRef.child("Contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                rootRef.child(mContext.getString(R.string.USERS)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                        if (dataSnapshot1.exists()) {

                            for (int i = 0; i < appContactsList.size(); i++) {
                                if (!dataSnapshot1.hasChild(generatePhoneNumber(appContactsList.get(i).getPhone())))
                                    deleteContact(appContactsList.get(i));
                            }

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int i = 0; i < tempContactsList.size(); i++) {
                                if (dataSnapshot1.hasChild(generatePhoneNumber(tempContactsList.get(i).getPhone()))) {
                                    tempContactsList.get(i).setUserId(Objects.requireNonNull(dataSnapshot1.child(
                                            generatePhoneNumber(tempContactsList.get(i).getPhone())).getValue()).toString());

                                    if (tempContactsList.get(i).getUserId() != null) {
                                        if (dataSnapshot2.exists()) {
                                            if (dataSnapshot2.hasChild(tempContactsList.get(i).getUserId())) {
                                                tempContactsList.get(i).setStatus(Objects.requireNonNull(dataSnapshot2.child(
                                                        tempContactsList.get(i).getUserId()).child(mContext.getString(R.string.STATUS)).getValue()).toString());

                                                tempContactsList.get(i).setImage(dataSnapshot2.child(
                                                        tempContactsList.get(i).getUserId()).child(mContext.getString(R.string.IMAGE)).getValue().toString());
                                                if (checkIfContactExist(tempContactsList.get(i))) {
                                                    addContact(tempContactsList.get(i));
                                                }
                                                else if (checkForUpdate(tempContactsList.get(i))) {
                                                    updateContact(tempContactsList.get(i));
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

    private boolean checkIfContactExist(Contact contact) {
        Cursor cursor = mResolver.query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + " =? AND " +
                        ContactsContract.Data.MIMETYPE + " =?",
                new String[] {contact.getRawId(), "vnd.android.cursor.item/com.example.wetalk.profile"},
                null);

        boolean check;

        assert cursor != null;
        check = cursor.getCount() == 0;
        cursor.close();
        return check;
    }

    private boolean checkForUpdate(Contact contact) {
        Cursor cursor = mResolver.query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.DATA4 + " =? AND " +
                        ContactsContract.Data.MIMETYPE + " =?",
                new String[] {contact.getUserId(), "vnd.android.cursor.item/com.example.wetalk.profile"},
                null);

        if (contact == null)
            return false;
        else if (contact.getRawId() == null)
            return false;

        assert cursor != null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA7));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA2));
                String userId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4));
                String image = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA5));
                String status = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA6));

                return (!contact.getRawId().equals(id)) || (!contact.getUserId().equals(userId)) ||
                        (!contact.getImage().equals(image)) || (!contact.getName().equals(name)) ||
                        (!contact.getPhone().equals(phone)) || (!contact.getStatus().equals(status));
            }
        }

        cursor.close();
        return false;
    }

    private void updateContact(Contact contact) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.MIMETYPE + " =? AND "
                                        + ContactsContract.Data.DATA4 + " =?",
                                        new String[] {"vnd.android.cursor.item/com.example.wetalk.profile", contact.getUserId()})
                .withValue(ContactsContract.Data.DATA1, contact.getPhone())
                .withValue(ContactsContract.Data.DATA2, contact.getName())
                .withValue(ContactsContract.Data.DATA3, "הודעה אל "+ generatePhoneNumber(contact.getPhone()))
                .withValue(ContactsContract.Data.DATA4, contact.getUserId())
                .withValue(ContactsContract.Data.DATA5, contact.getImage())
                .withValue(ContactsContract.Data.DATA6, contact.getStatus())
                .withValue(ContactsContract.Data.DATA7, contact.getRawId()).build());

        try {
            mResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            mResolver.notifyChange(ContactsContract.Contacts.CONTENT_URI, null, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteContact(Contact contact) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        Cursor cursor = mResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                null,
                ContactsContract.RawContacts.CONTACT_ID + " =? AND " +
                        ContactsContract.RawContacts.ACCOUNT_TYPE + " =? AND " +
                        ContactsContract.RawContacts.ACCOUNT_NAME + " =?",
                new String[] {contact.getRawId(),
                        AccountGeneral.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_NAME},
                null);

        assert cursor != null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Uri rawUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();

                ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).
                        withSelection(ContactsContract.RawContacts._ID + " =? AND "
                                        + ContactsContract.RawContacts.ACCOUNT_TYPE + " =? AND "
                                        + ContactsContract.RawContacts.ACCOUNT_NAME + " =?"
                                ,new String[] {cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID)),
                                        AccountGeneral.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_NAME}).build()); //sets deleted flag to 1

                ops.add(ContentProviderOperation.newDelete(rawUri).
                        withSelection(ContactsContract.RawContacts._ID + " =? AND "
                                        +ContactsContract.RawContacts.ACCOUNT_TYPE + " =? AND "
                                        +ContactsContract.RawContacts.ACCOUNT_NAME + " =?"
                                ,new String[] {cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID)),
                                        AccountGeneral.ACCOUNT_TYPE,AccountGeneral.ACCOUNT_NAME}).build());

                try {
                    mResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                    mResolver.notifyChange(ContactsContract.Contacts.CONTENT_URI, null, false);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
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
                .withValue(ContactsContract.Data.DATA4, contact.getUserId())
                .withValue(ContactsContract.Data.DATA5, contact.getImage())
                .withValue(ContactsContract.Data.DATA6, contact.getStatus())
                .withValue(ContactsContract.Data.DATA7, contact.getRawId())
                .withYieldAllowed(true).build());

        try {
            mResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            mResolver.notifyChange(ContactsContract.Contacts.CONTENT_URI, null, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generatePhoneNumber(String number) {
        StringBuilder phone = new StringBuilder();

        String locale = mContext.getResources().getConfiguration().locale.getCountry();
        String countryCode = "+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(locale);

        if (!number.contains(countryCode))
            number = number.substring(1);

        for (int i=0; i < number.length();i++) {
            if (i == 0 && number.charAt(i) == '+')
                phone.append(number.charAt(i));

            if (number.charAt(i) >= '0' && number.charAt(i) <= '9')
                phone.append(number.charAt(i));
        }


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
