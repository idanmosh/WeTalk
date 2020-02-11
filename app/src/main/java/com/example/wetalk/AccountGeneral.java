package com.example.wetalk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;

public final class AccountGeneral {

    public static final String ACCOUNT_NAME = "WeTalk";

    public static final String ACCOUNT_TYPE = "com.example.wetalk";

    public static Account getAccount() {
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    public static void createSyncAccount(Context context) {
        boolean created;

        Account account = getAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        assert accountManager != null;
        if (accountManager.addAccountExplicitly(account, null, null)) {
            final long SYNC_FREQUENCY = 60 * 180;

            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);

            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);

            ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, new Bundle(), SYNC_FREQUENCY);

            created = true;
        }
        else
            created = false;

        if (created)
            SyncAdapter.performSync();
    }
}
