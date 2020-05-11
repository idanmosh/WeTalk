package com.example.wetalk.Permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.wetalk.R;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

public final class Permissions  {

    public static final int PROFILE_REQUEST_CODE = 100;
    public static final int IMAGE_REQUEST = 101;
    public static final int EXTERNAL_REQUEST = 102;
    public static final int CALL_REQUEST = 103;
    public static final int AUDIO_REQUEST = 104;


    public static final String READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
    public static final String GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;
    public static final String INTERNET = Manifest.permission.INTERNET;
    public static final String ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String MODIFY_AUDIO_SETTINGS = Manifest.permission.MODIFY_AUDIO_SETTINGS;
    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final String CAMERA = Manifest.permission.CAMERA;
    public static String[]  PermissionsCall = {INTERNET,ACCESS_NETWORK_STATE,RECORD_AUDIO,MODIFY_AUDIO_SETTINGS,READ_PHONE_STATE,CALL_PHONE};
    public static boolean boolCallPermission = false;

    private Permissions() { }

    public static void ProfilePermissionsDialog(@NonNull Context context, @NonNull AppCompatActivity activity) {
        if (checkPermissions(context, READ_STORAGE, READ_CONTACTS)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To add contacts to chat," +
                            " and recover your user information, allow WeTalk to" +
                            " access your contacts, photos, media, and files from your device.")
                    .setIcon(R.drawable.storage_contacts_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(activity,
                            new String[]{READ_STORAGE,WRITE_STORAGE,WRITE_CONTACTS,READ_CONTACTS,GET_ACCOUNTS}, PROFILE_REQUEST_CODE))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to contacts, photos," +
                                " media, and files from your device.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);
            dialog.show();
        }
    }

    public static void callPermissionsDialog(@NonNull Context context, @NonNull AppCompatActivity activity) {
        if (!checkPermissionsForCall(context, PermissionsCall)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To call to contacts, allow WeTalk to create and received call from your device.")
                    .setIcon(R.drawable.ic_phone_white)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(activity, PermissionsCall , CALL_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to create and received call from contacts from your device.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static void ProfileImagePermissionDialog(@NonNull Context context, AppCompatActivity activity) {
        if (checkPermissions(context, READ_STORAGE, CAMERA)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To take a photo or select a photo from the gallery" +
                            ", allow WeTalk to access your camera ,photos, media, and files from your device.")
                    .setIcon(R.drawable.camera_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(activity, new String[]{READ_STORAGE,WRITE_STORAGE,CAMERA}, IMAGE_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to camera and media" +
                                " storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static void audioPermissionDialog(@NonNull Context context, AppCompatActivity activity) {
        if (checkPermissions(context, READ_STORAGE, WRITE_STORAGE)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To record audio and send audio allow WeTalk to access your " +
                            "phone microphone and access to write into phone storage.")
                    .setIcon(R.drawable.camera_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(activity, new String[]{READ_STORAGE,
                            WRITE_STORAGE,RECORD_AUDIO}, AUDIO_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access phone microphone and write into phone storage," +
                                " you must confirm the permissions.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static void ImagePermissionDialog(@NonNull Context context, AppCompatActivity activity) {
        if (checkPermissions(context, READ_STORAGE, WRITE_STORAGE)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To take a photo or select a photo from the gallery" +
                            ", allow WeTalk to access your camera ,photos, media, and files from your device.")
                    .setIcon(R.drawable.camera_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(activity, new String[]{READ_STORAGE,WRITE_STORAGE}, IMAGE_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to camera and media" +
                                " storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static void ImagePermissionDialog(@NonNull Context context, Fragment fragment) {
        if (checkPermissions(context, READ_STORAGE, WRITE_STORAGE)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To take a photo or select a photo from the gallery" +
                            ", allow WeTalk to access your camera ,photos, media, and files from your device.")
                    .setIcon(R.drawable.camera_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(fragment, new String[]{READ_STORAGE,WRITE_STORAGE}, IMAGE_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to camera and media" +
                                " storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static void ProfileShareImagePermissionDialog(@NonNull Context context, AppCompatActivity activity) {
        if (checkPermissions(context, READ_STORAGE, WRITE_STORAGE)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To share a photo or select a photo from the gallery" +
                            ", allow WeTalk to access your photos, media, and files from your device.")
                    .setIcon(R.drawable.camera_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(activity, new String[]{READ_STORAGE,WRITE_STORAGE,
                            WRITE_CONTACTS,READ_CONTACTS,GET_ACCOUNTS}, EXTERNAL_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to media" +
                                " storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static void ProfileImagePermissionDialog(@NonNull Context context, Fragment fragment) {
        if (checkPermissions(context, READ_STORAGE, CAMERA)) {
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To take a photo or select a photo from the gallery" +
                            ", allow WeTalk to access your camera ,photos, media, and files from your device.")
                    .setIcon(R.drawable.camera_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(fragment, new String[]{READ_STORAGE,WRITE_STORAGE,CAMERA}, IMAGE_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to camera and media" +
                                " storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static void ContactsPermissionDialog(@NonNull Context context, Fragment fragment){
        if (checkPermissions(context,WRITE_CONTACTS,READ_CONTACTS)){
            MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(context)
                    .setDescription("To read contacts from your phone book , allow WeTalk to access your phone book contacts.")
                    .setIcon(R.drawable.storage_contacts_permission)
                    .setPositiveText(R.string.continue_btn)
                    .onPositive((dialog1, which) -> requestPermissions(fragment, new String[]{WRITE_CONTACTS,READ_CONTACTS,
                            WRITE_STORAGE,READ_STORAGE,GET_ACCOUNTS}, EXTERNAL_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> Toast.makeText(context, "You can't get access to your phone book contacts" +
                                ", you must confirm the permissions.", Toast.LENGTH_SHORT).show())
                    .setCancelable(false);

            dialog.show();
        }
    }

    public static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context.checkSelfPermission(permission)
                != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissions(@NonNull Context context, @NonNull String permission1, @NonNull String permission2) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (context.checkSelfPermission(permission1)
                + context.checkSelfPermission(permission2))
                != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissionsForCall(@NonNull Context context, @NonNull String[] permissions) {
        for (int i = 0; i<permissions.length; i++){
            boolCallPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && (context.checkSelfPermission(permissions[i])) == PackageManager.PERMISSION_GRANTED;
            if(!boolCallPermission)
                return boolCallPermission;
        }
        return true;
    }

    public static void requestPermission(AppCompatActivity activity, @NonNull String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity,new String[]{permission}, requestCode);
    }

    public static void requestPermissions(Fragment fragment, @NonNull String[] permissions, int requestCode) {
        fragment.requestPermissions(permissions, requestCode);
    }

    public static void requestPermissions(AppCompatActivity activity, @NonNull String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

}
