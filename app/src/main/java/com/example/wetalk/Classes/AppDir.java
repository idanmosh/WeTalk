package com.example.wetalk.Classes;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.example.wetalk.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import static android.app.DownloadManager.Request.NETWORK_MOBILE;
import static android.app.DownloadManager.Request.NETWORK_WIFI;
import static android.app.DownloadManager.Request.VISIBILITY_HIDDEN;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.getExternalStorageDirectory;

public class AppDir {

    private static final String appRoot = "WeTalk";
    private static final String appMediaStorage = "WeTalk Media";
    private static final String appAudioStorage = "WeTalk Audio";
    private static final String appDocsStorage = "WeTalk Docs";
    private static final String imageRoot = "image.jpg";
    private File appDir;
    private File mediaDir;
    private File audioDir;
    private File docsDir;

    public AppDir() {
        appDir = new File(getExternalStorageDirectory(), appRoot);
        if (!appDir.exists())
            appDir.mkdir();
        if (appDir.exists())
            initAppDir();
    }

    private void initAppDir() {
        mediaDir = new File(appDir, appMediaStorage);
        audioDir = new File(appDir, appAudioStorage);
        docsDir = new File(appDir, appDocsStorage);
        if (!mediaDir.exists())
            mediaDir.mkdir();
        if (!audioDir.exists())
            audioDir.mkdir();
        if (!docsDir.exists())
            docsDir.mkdir();
    }

    public Uri getAudioFromStorage(String fileName,  Context context) {
        Uri uri = null;
        if (audioDir.exists()) {
            File file = new File(audioDir, fileName);
            if (file.exists())
                uri = Uri.fromFile(file);
        }
        return uri;
    }

    public void saveAudioToStorage(Context context, String fileName, String url) {
        if (audioDir.exists()) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);

            File file = new File(audioDir, fileName + ".m4a");

            request.setNotificationVisibility(VISIBILITY_HIDDEN);
            request.setAllowedNetworkTypes(NETWORK_WIFI | NETWORK_MOBILE);
            request.setDestinationUri(Uri.fromFile(file));
            request.setAllowedOverRoaming(false);
            request.setVisibleInDownloadsUi(false);
            request.allowScanningByMediaScanner();
            downloadManager.enqueue(request);
        }
    }

    public void saveDocToStorage(Context context, String[] fileNames, String url) {
        if (docsDir.exists()) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);

            File file;
            if (fileNames[0].contains("http"))
                file = new File(docsDir, fileNames[1] + fileNames[2]);
            else
                file = new File(docsDir, fileNames[0]);

            request.setNotificationVisibility(VISIBILITY_HIDDEN);
            request.setAllowedNetworkTypes(NETWORK_WIFI | NETWORK_MOBILE);
            request.setDestinationUri(Uri.fromFile(file));
            request.setAllowedOverRoaming(false);
            request.setVisibleInDownloadsUi(false);
            request.allowScanningByMediaScanner();
            downloadManager.enqueue(request);
        }
    }

    public Uri getDocFromStorage(String fileName, Context context) {
        Uri uri = null;
        if (docsDir.exists()) {
            File file = new File(docsDir, fileName);
            if (file.exists())
                uri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".provider", file);
        }
        return uri;
    }

    public void saveImageToStorage(ImageView imageView, String path, Context context) {
        if (mediaDir.exists())  {
            Drawable drawable = imageView.getDrawable();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            OutputStream outputStream = null;
            File file = new File(mediaDir,path + context.getString(R.string.JPG));
            try {
                outputStream = new FileOutputStream(file);
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG,75,outputStream);
            try {
                Objects.requireNonNull(outputStream).flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null,
                    (path1, uri) -> {});
        }
    }

    public Uri getImageFromStorage(String path, Context context) {
        Uri fileUri = null;
        if (mediaDir.exists()) {
            File file = new File(mediaDir, path + context.getString(R.string.JPG));
            if (file.exists())
                fileUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".provider", file);
        }
        return fileUri;
    }

    public void saveProfileImage(ImageView imageView) {
        if (mediaDir.exists()){
            Drawable drawable = imageView.getDrawable();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            OutputStream outputStream = null;
            File file = new File(mediaDir, imageRoot);
            try {
                outputStream = new FileOutputStream(file);
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG,75,outputStream);
            try {
                Objects.requireNonNull(outputStream).flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Uri getProfileImage() {
        Uri uri = null;
        if (mediaDir.exists()) {
            File file = new File(mediaDir, imageRoot);
            if (file.exists())
                uri = Uri.fromFile(file);
        }
        return uri;
    }

    public File getMediaDir() {
        return mediaDir;
    }

    public File getDocDir() {
        return docsDir;
    }

    public File getAudioDir() {
        return audioDir;
    }
}

