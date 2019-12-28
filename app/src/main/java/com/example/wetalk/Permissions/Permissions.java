package com.example.wetalk.Permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.wetalk.R;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

public final class Permissions  {

    public static final int PROFILE_REQUEST_CODE = 100;
    public static final int IMAGE_REQUEST = 101;

    public static final String READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String CAMERA = Manifest.permission.CAMERA;

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
                                new String[]{READ_STORAGE,WRITE_STORAGE,READ_CONTACTS}, PROFILE_REQUEST_CODE))
                        .setNegativeText(R.string.decline)
                        .onNegative((dialog12, which) -> {
                            Toast.makeText(context, "You can't get access to contacts, photos," +
                                    " media, and files from your device.", Toast.LENGTH_SHORT).show();
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
                    .onPositive((dialog1, which) -> requestPermissions(activity, new String[]{READ_STORAGE,CAMERA}, IMAGE_REQUEST))
                    .setNegativeText(R.string.decline)
                    .onNegative((dialog12, which) -> {
                        Toast.makeText(context, "You can't get access to camera and media" +
                                " storage, you must confirm the permissions.", Toast.LENGTH_SHORT).show();
                    })
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

    public static void requestPermission(AppCompatActivity activity, @NonNull String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity,new String[]{permission}, requestCode);
    }

    public static void requestPermissions(AppCompatActivity activity, @NonNull String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

}
