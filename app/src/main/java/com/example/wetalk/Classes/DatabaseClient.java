package com.example.wetalk.Classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.wetalk.ContactContract;

public final class DatabaseClient extends SQLiteOpenHelper {
    private static volatile DatabaseClient instance;
    private final SQLiteDatabase db;

    public DatabaseClient(@Nullable Context context) {
        super(context, ContactContract.DB_NAME, null, ContactContract.DB_VERSION);
        this.db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE [" + ContactContract.Contacts.NAME + "] " +
                "([" + ContactContract.Contacts.COL_ID + "] TEXT UNIQUE PRIMARY KEY," +
                "[" + ContactContract.Contacts.COL_NAME + "] TEXT NOT NULL," +
                "[" + ContactContract.Contacts.COL_USER_ID + "] TEXT NOT NULL," +
                "[" + ContactContract.Contacts.COL_STATUS + "] TEXT NOT NULL," +
                "[" + ContactContract.Contacts.COL_PHONE + "] TEXT NOT NULL," +
                "[" + ContactContract.Contacts.COL_IMAGE + "] TEXT NOT NULL);");
    }

    public static DatabaseClient getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseClient.class) {
                if (instance == null) {
                    instance = new DatabaseClient(c);
                }
            }
        }
        return instance;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update any SQLite tables here
        db.execSQL("DROP TABLE IF EXISTS [" + ContactContract.Contacts.NAME + "];");
        onCreate(db);
    }

    public SQLiteDatabase getDb() {
        return db;
    }

}
