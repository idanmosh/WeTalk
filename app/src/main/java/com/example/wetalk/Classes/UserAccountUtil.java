package com.example.wetalk.Classes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class UserAccountUtil {
    public static Account getAccount(Context context) {
        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Script", "GET_ACCOUNTS not present.");
        }

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.WeTalk");
        if (accounts.length > 0) {
            return accounts[0];
        } else {
            return null;
        }
    }
}
