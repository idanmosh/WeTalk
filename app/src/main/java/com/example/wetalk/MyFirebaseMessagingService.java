package com.example.wetalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final static String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sender_id = remoteMessage.getData().getOrDefault("sender_id", "");
        String messageTitle = "";
        ContentResolver mResolver = getApplicationContext().getContentResolver();

        if (!Objects.equals(sender_id, "")) {
            Cursor cursor = mResolver.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.DATA4 + " =? AND " +
                            ContactsContract.Data.MIMETYPE + " =?",
                    new String[] {sender_id, "vnd.android.cursor.item/com.example.wetalk.profile"},
                    null);

            assert cursor != null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                messageTitle = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA2));
            }

            cursor.close();
        }

        String messageBody = Objects.requireNonNull(remoteMessage.getNotification()).getBody();

        Log.i(TAG, messageBody + " " + messageTitle);

        NotificationCompat.Builder mBuilder = new NotificationCompat
                .Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.logo_image)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        createNotificationChannel();

        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
