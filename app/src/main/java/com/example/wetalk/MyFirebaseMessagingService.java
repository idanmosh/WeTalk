package com.example.wetalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.wetalk.Classes.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String ContactPREFERENCES = "ContactsPrefs";
    private SharedPreferences mContactsSharedPreferences;
    private final static String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private String image;
    private String messageTitle;
    private String sender_id;
    private ContentResolver mResolver;
    private NotificationCompat.Builder mBuilder, mBuilderGroup;
    private Bitmap circleImage;
    private Contact mContact;
    private String messageBody;
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private String messageId;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        mContactsSharedPreferences = getSharedPreferences(ContactPREFERENCES, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        messageId = remoteMessage.getData().get("message_id");
        sender_id = remoteMessage.getData().get("sender_id");
        messageTitle = "";
        image = "";
        mResolver = getApplicationContext().getContentResolver();
        messageBody = remoteMessage.getData().get("body");

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
        messageSent();
        setContactState();
    }

    private void setContactState() {
        mContactsSharedPreferences.edit().putBoolean(mContact.getUserId() + "_state", true).apply();
    }

    private void messageSent() {
        Map<String, Object> map = new HashMap<>();
        map.put("send", "sent");
        ref.child(getString(R.string.USERS))
                .child(sender_id).child("Messages")
                .child(mAuth.getCurrentUser().getUid())
                .child(messageId).updateChildren(map).addOnCompleteListener(task -> {});
    }

    private void createSingleChatIntent() {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        chatIntent.putExtra("CONTACT", mContact);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(chatIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        createSingleChatBuilder(resultPendingIntent);
    }

    private void createSingleChatBuilder(PendingIntent resultPendingIntent) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder = new NotificationCompat
                .Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.logo_image)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .setGroup(mContact.getUserId())
                .setGroupSummary(true)
                .setSound(defaultSoundUri)
                .setOnlyAlertOnce(false)
                .setChannelId(getString(R.string.default_notification_channel_id))
                .setContentIntent(resultPendingIntent);

        DatabaseReference unreadMessages = ref.child(getString(R.string.USERS))
                .child(mContact.getUserId()).child("Messages")
                .child(mAuth.getUid());
        Query query = unreadMessages.orderByChild("state").equalTo("unread").limitToFirst(1000);
        query.keepSynced(true);
        query.addValueEventListener(readSenderMessagesListener);
    }

    private ValueEventListener readSenderMessagesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Stack<String> stack = new Stack<>();
            Stack<String> sortStack = new Stack<>();
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            int count = 0;
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (snapshot.exists() && snapshot.hasChild("state")) {
                    String messageState = snapshot.child("state").getValue().toString();
                    if (messageState.equals("unread")) {
                        String message = snapshot.child("message").getValue().toString();
                        stack.push(message);
                        count++;
                    }
                }
            }
            mContactsSharedPreferences.edit().putInt(mContact.getUserId() + "_unreadMessages", count).apply();
            int counter = 0;
            while ((!stack.isEmpty()) && (counter < 7)) {
                sortStack.push(stack.pop());
                counter++;
            }
            counter = 0;
            while ((!sortStack.isEmpty()) && (counter < 7)) {
                style.addLine(sortStack.pop());
                counter++;
            }
            if (count > 1) {
                mBuilder.setContentText(count + " הודעות חדשות");
            }
            if (dataSnapshot.exists())
                mBuilder.setStyle(style);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            databaseError.getMessage();
        }
    };

    private void createCircleImage() {
        circleImage = null;
        try {
            InputStream srt = new URL(image).openStream();
            circleImage = BitmapFactory.decodeStream(srt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (circleImage == null)
            mBuilder.setLargeIcon(getCircleBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.profile_image)));
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
                mContact = new Contact(userId,id,messageTitle,phone,status,image,null,0);
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
            channel.enableVibration(true);
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
