package com.example.wetalk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.wetalk.Classes.Contact;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SendDocFragment extends Fragment {

    private static final String DATE_FORMAT = "h:mm a dd MMMM yyyy";

    private static final String ContactPREFERENCES = "ContactsPrefs";
    private final Contact mContact;
    private final Context mContext;
    private Uri sendDocUri;
    private final OnBackPressedFragment listener;
    private SharedPreferences mContactsSharedPreferences;

    private TextView taskPercent;
    private ProgressBar mProgressBar;
    private Toolbar sendDocToolbar;
    private View sendDocView;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String senderId;
    private TextView docName;
    private FloatingActionButton sendDocBtn;

    public SendDocFragment(Contact mContact, Context mContext , Uri sendDocUri, OnBackPressedFragment listener) {
        this.mContact = mContact;
        this.mContext = mContext;
        this.sendDocUri = sendDocUri;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sendDocView = inflater.inflate(R.layout.fragment_send_doc, container, false);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        senderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mContactsSharedPreferences = mContext.getSharedPreferences(ContactPREFERENCES, Context.MODE_PRIVATE);

        sendDocToolbar = sendDocView.findViewById(R.id.send_doc_toolbar);
        sendDocToolbar.inflateMenu(R.menu.empty_menu);
        ((AppCompatActivity)getActivity()).setSupportActionBar(sendDocToolbar);
        Objects.requireNonNull((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        sendDocToolbar.setNavigationOnClickListener(v -> listener.onBackPressedFragment());

        taskPercent = sendDocView.findViewById(R.id.send_doc_task_percent);
        mProgressBar = sendDocView.findViewById(R.id.image_progressbar);

        docName = sendDocView.findViewById(R.id.doc_name);
        sendDocBtn = sendDocView.findViewById(R.id.btnSendDoc);

        String[] doc = getDocType();
        docName.setText(doc[1]);
        sendDocBtn.setOnClickListener(v -> {
            if (!doc[0].equals("UNKNOWN"))
                sendDocMessage(doc);
            else {
                Toast.makeText(mContext, "Error in trying send the file.", Toast.LENGTH_SHORT).show();
                listener.onBackPressedFragment();
            }
        });

        setHasOptionsMenu(true);

        return sendDocView;
    }

    private void sendDocMessage(String[] doc) {
        mProgressBar.setVisibility(VISIBLE);
        taskPercent.setVisibility(VISIBLE);

        DatabaseReference senderMessageKeyRef = rootRef.child(getString(R.string.USERS))
                .child(senderId).child("Messages").child(mContact.getUserId()).push();
        String senderMessageKey = senderMessageKeyRef.getKey();
        DatabaseReference receiverMessageKeyRef = rootRef.child(getString(R.string.USERS))
                .child(mContact.getUserId()).child("Messages").child(senderId).child(Objects.requireNonNull(senderMessageKey));
        String date = getDate();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("Doc Files").child(doc[1]);

        UploadTask senderUploadTask = storageRef.putFile(sendDocUri);

        senderUploadTask.addOnSuccessListener(taskSnapshot -> {
            if (taskSnapshot.getMetadata() != null) {
                Task<Uri> senderResult = taskSnapshot.getStorage().getDownloadUrl();
                senderResult.addOnSuccessListener(uri -> {
                    Map<String, Object> senderMessageTextBody = new HashMap<>();
                    senderMessageTextBody.put("message", doc[1]);
                    senderMessageTextBody.put("type", doc[0]);
                    senderMessageTextBody.put("from", senderId);
                    senderMessageTextBody.put("date", date);
                    senderMessageTextBody.put("state", "unread");
                    senderMessageTextBody.put("link", uri.toString());

                    Map<String, Object> receiverMessageTextBody = new HashMap<>();
                    receiverMessageTextBody.put("message", doc[1]);
                    receiverMessageTextBody.put("type", doc[0]);
                    receiverMessageTextBody.put("from", senderId);
                    receiverMessageTextBody.put("date", date);
                    receiverMessageTextBody.put("link", uri.toString());

                    senderMessageKeyRef.updateChildren(senderMessageTextBody).addOnCompleteListener(task -> {
                        receiverMessageKeyRef.updateChildren(receiverMessageTextBody).addOnCompleteListener(task1 -> {
                            setContactState();
                            mProgressBar.setVisibility(GONE);
                            taskPercent.setVisibility(GONE);
                            listener.onBackPressedFragment();
                        });
                    });
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(GONE);
            taskPercent.setVisibility(GONE);
        }).addOnProgressListener(taskSnapshot -> {
            double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            mProgressBar.setProgress((int) p);
            taskPercent.setText((int) p + "%");
        });
    }

    private String[] getDocType() {
        String uriString = sendDocUri.toString();
        File myFile = new File(uriString);
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(sendDocUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst())
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                assert cursor != null;
                cursor.close();
            }
        } else if (uriString.startsWith("file://"))
            displayName = myFile.getName();

        assert displayName != null;
        String[] arr = displayName.split("\\.");

        return new String[] {getType(arr[arr.length-1]), displayName};
    }

    private String getType(String fileType) {

        if (fileType.toLowerCase().contains("docx"))
            return "DOCX";
        else if (fileType.toLowerCase().contains("pdf"))
            return "PDF";
        else if (fileType.toLowerCase().contains("pptx"))
            return "PPTX";
        else if (fileType.toLowerCase().contains("ppt"))
            return "PPT";
        else if (fileType.toLowerCase().contains("doc"))
            return "DOC";
        else if (fileType.toLowerCase().contains("xlsx"))
            return "XLSX";
        else if (fileType.toLowerCase().contains("xls"))
            return "XLS";
        else
            return "UNKNOWN";
    }

    @SuppressLint("SimpleDateFormat")
    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date today = Calendar.getInstance().getTime();
        String date = sdf.format(today);
        return date;
    }

    private void setContactState() {
        mContactsSharedPreferences.edit().putBoolean(mContact.getUserId() + "_state", true).apply();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.empty_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }
}
