package com.example.wetalk;

import android.net.Uri;

public class ContactContract {
    public static final String CONTENT_AUTHORITY = "com.example.sync";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ARTICLES = "contacts";

    // Database information
    public static final String DB_NAME = "contacts_db";
    public static final int DB_VERSION = 1;


    /**
     * This represents our SQLite table for our contacts.
     */
    public static abstract class Contacts {
        public static final String NAME = "contacts";
        public static final String COL_ID = "contactId";
        public static final String COL_USER_ID = "contact_userId";
        public static final String COL_NAME = "contactName";
        public static final String COL_IMAGE = "contactImage";
        public static final String COL_STATUS = "contactStatus";
        public static final String COL_PHONE = "contactPhone";

        // ContentProvider information for contacts
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTICLES).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_ARTICLES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_ARTICLES;
    }
}
