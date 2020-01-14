package com.example.wetalk.Classes;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class AppDir {

    private static final String appRoot = "/WeTalk";
    private static final String appMediaStorage = "/Media";
    private static final String imageRoot = "/image.jpg";
    private File appDir;
    private File mediaDir;


    public AppDir() {
        appDir = new File(Environment.getExternalStorageDirectory() + appRoot);
        if (!appDir.exists())
            appDir.mkdir();
        if (appDir.exists())
            initAppDir();
    }

    private void initAppDir() {
        mediaDir = new File(appDir.getAbsolutePath() + appMediaStorage);
        if (!mediaDir.exists())
            mediaDir.mkdir();

    }

    public void saveProfileImage(ImageView imageView) {
        if (mediaDir.exists()){
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            OutputStream outputStream = null;
            File file = new File(mediaDir.getAbsolutePath(), imageRoot);
            try {
                outputStream = new FileOutputStream(file);
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            try {
                Objects.requireNonNull(outputStream).flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Uri getProfileImage() {
        File file = new File(mediaDir.getAbsolutePath(), imageRoot);
        Uri uri = null;
        if (file.exists()) {
            uri = Uri.fromFile(file);
        }

        return uri;
    }
}

