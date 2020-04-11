package com.example.wetalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.wetalk.Classes.Contact;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final static String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private String image;
    private String messageTitle;
    private String sender_id;
    private ContentResolver mResolver;
    private NotificationCompat.Builder mBuilder, mBuilderGroup;
    private Bitmap circleImage;
    private Contact mContact;
    private String messageBody;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        sender_id = remoteMessage.getData().getOrDefault("sender_id", "");
        messageTitle = "";
        image = "";
        mResolver = getApplicationContext().getContentResolver();
        messageBody = Objects.requireNonNull(remoteMessage.getNotification()).getBody();

        findSenderDetails();

        createSingleChatIntent();

        /*NotificationCompat.InboxStyle userInbox =
                new NotificationCompat.InboxStyle()
                        .setBigContentTitle(groupMessages())
                        .setSummaryText("janedoe@example.com").addLine(messageBody);

        mBuilderGroup =
                new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                        .setContentTitle("title")
                        .setContentText("text")
                        .setSmallIcon(R.drawable.logo_image)
                        .setStyle(userInbox)
                        .setGroup(sender_id)
                        .setAutoCancel(true)
                        .setGroupSummary(true);
        */
        createCircleImage();

        createNotificationChannel();

        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);

        //mNotifyMgr.notify(Integer.parseInt(mContact.getRawId()), mBuilderGroup.build());
        mNotifyMgr.notify(Integer.parseInt(mContact.getRawId()), mBuilder.build());
    }

    private void createSingleChatIntent() {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("CONTACT", mContact);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(chatIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        createSingleChatBuilder(resultPendingIntent);
    }

    private void createSingleChatBuilder(PendingIntent resultPendingIntent) {
        mBuilder = new NotificationCompat
                .Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.logo_image)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setGroup(sender_id)
                .setContentIntent(resultPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOnlyAlertOnce(true);
    }

    private void createCircleImage() {
        circleImage = null;
        try {
            InputStream srt = new URL(image).openStream();
            circleImage = BitmapFactory.decodeStream(srt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (circleImage == null)
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.profile_image));
        else{
            mBuilder.setLargeIcon(getCircleBitmap(circleImage));
        }
    }

    private void findSenderDetails() {
        if (!Objects.equals(sender_id, "")) {
            Cursor cursor = mResolver.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.DATA4 + " =? AND " +
                            ContactsContract.Data.MIMETYPE + " =?",
                    new String[] {sender_id, "vnd.android.cursor.item/com.example.wetalk.profile"},
                    null);

            assert cursor != null;
            if (cursor.moveToFirst()) {
                messageTitle = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA2));
                image = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA5));
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA7));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
                String userId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4));
                String status = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA6));
                mContact = new Contact(userId,id,messageTitle,phone,status,image);
            }

            cursor.close();
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
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
