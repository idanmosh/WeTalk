package com.example.wetalk.Classes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DB_NAME = "ContactsDB";
    private static final String CONTACTS_TABLE = "contacts";
    private static final String MESSAGE_TABLE = "message";

    private static final String USER_ID = "user_id";
    private static final String RAW_ID = "raw_id";
    private static final String NAME = "name";
    private static final String NUMBER = "number";
    private static final String STATUS = "status";
    private static final String IMAGE = "image";
    private static final String LAST_MESSAGE = "lastMessage";
    private static final String LAST_MESSAGE_TIME = "lastMessageTime";
    private static final String UNREAD_MESSAGEES = "unreadMessages";

    public DBHandler(@Nullable Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + CONTACTS_TABLE
                        + "(" + RAW_ID + " TEXT PRIMARY KEY,"
                        + USER_ID + " TEXT,"
                        + NAME + " TEXT,"
                        + NUMBER + " TEXT,"
                        + STATUS + " TEXT,"
                        + IMAGE + " TEXT)";

        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE " + CONTACTS_TABLE;
        db.execSQL(sql);
        onCreate(db);
    }


    public void addContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RAW_ID, contact.getRawId());
        values.put(USER_ID, contact.getUserId());
        values.put(NAME, contact.getName());
        values.put(NUMBER,contact.getPhone());
        values.put(STATUS,contact.getStatus());
        values.put(IMAGE, contact.getImage());

        db.insert(CONTACTS_TABLE, null, values);
        db.close();
    }

    public Contact getContactByPhone(String phone) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(CONTACTS_TABLE,
                new String[] {RAW_ID, USER_ID, NAME, NUMBER, STATUS, IMAGE},
                NUMBER + " =?",
                new String[] {phone},
                null, null,null,null);

        Contact contact;

        if (cursor != null) {
            if (cursor.getCount() > 0)
                cursor.moveToFirst();
            else
                return null;

            contact = new Contact(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            );

            cursor.close();
            return contact;
        }

        return null;
    }

    public Contact getContact(String id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(CONTACTS_TABLE,
                new String[] {RAW_ID, USER_ID, NAME, NUMBER, STATUS, IMAGE},
                RAW_ID + " =?",
                new String[] {id},
                null, null,null,null);

        Contact contact;

        if (cursor != null) {
            cursor.moveToFirst();

            contact = new Contact(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            );

            cursor.close();
            return contact;
        }

        return null;
    }

    public List<Contact> getContacts() {
        SQLiteDatabase db = getReadableDatabase();
        List<Contact> contactList = new ArrayList<>();

        String query = "SELECT * FROM " + CONTACTS_TABLE;

        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setRawId(cursor.getString(0));
                contact.setUserId(cursor.getString(1));
                contact.setName(cursor.getString(2));
                contact.setPhone(cursor.getString(3));
                contact.setStatus(cursor.getString(4));
                contact.setImage(cursor.getString(5));
                contactList.add(contact);
            }
            while (cursor.moveToNext());
        }

        return contactList;
    }

    public void deleteContacts() {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(CONTACTS_TABLE, null, null);
        db.close();
    }

    public void updateContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RAW_ID, contact.getRawId());
        values.put(USER_ID, contact.getUserId());
        values.put(NAME, contact.getName());
        values.put(NUMBER,contact.getPhone());
        values.put(STATUS,contact.getStatus());
        values.put(IMAGE, contact.getImage());

        db.update(CONTACTS_TABLE, values, NUMBER + " =?", new String[] {contact.getPhone()});
        db.close();
    }


    public void deleteContact(String id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(CONTACTS_TABLE,RAW_ID + " =?",new String[] {id});
        db.close();
    }


}
