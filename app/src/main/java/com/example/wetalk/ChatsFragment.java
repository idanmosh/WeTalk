package com.example.wetalk;


import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetalk.Adapters.ContactsRecyclerViewAdapter;
import com.example.wetalk.Classes.Contact;
import com.example.wetalk.Classes.Message;
import com.example.wetalk.Permissions.Permissions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements ContactsRecyclerViewAdapter.ListItemClickListener  {

    private static final String ContactPREFERENCES = "ContactsPrefs";
    private SharedPreferences mContactsSharedPreferences;
    private View chatView;
    private ContentResolver mResolver;
    private FloatingActionButton mFindContacts;
    private RecyclerView contactsRecyclerView;
    private final List<Contact> contactsList = new ArrayList<>();
    private final Map<String, Contact> contactsMap = new HashMap<>();
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private ContactsRecyclerViewAdapter contactsRecyclerViewAdapter;
    private ContactsThread contactsThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatView = inflater.inflate(R.layout.fragment_chats, container, false);

        mContactsSharedPreferences = getContext().getSharedPreferences(ContactPREFERENCES, getContext().MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        mFindContacts = chatView.findViewById(R.id.find_contacts_btn);
        mResolver = getContext().getContentResolver();

        contactsRecyclerView = chatView.findViewById(R.id.chat_list);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mFindContacts.setOnClickListener(v -> checkContactsPermissions());

        if (!Permissions.checkPermissions(Objects.requireNonNull(getContext()), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS)) {
            contactsThread = new ContactsThread();
        }

        contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(
                contactsList,2, this);

        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);

        return chatView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Permissions.checkPermissions(Objects.requireNonNull(getContext()), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS)) {
            if (contactsThread != null) {
                if (contactsThread.isStop())
                    contactsThread.setStop(false);
            }
            else
                contactsThread = new ContactsThread();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (contactsThread != null) {
            if (!contactsThread.isStop())
                contactsThread.setStop(true);
        }
    }

    private void checkContactsPermissions() {
        if (!Permissions.checkPermissions(Objects.requireNonNull(getContext()), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS))
            sendUserToFindContactsActivity();
        else
            Permissions.ContactsPermissionDialog(getContext(), ChatsFragment.this);
    }

    private void sendUserToFindContactsActivity() {
        Intent findContactIntent = new Intent(getActivity(), FindContactActivity.class);
        findContactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findContactIntent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        getActivity().finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Permissions.EXTERNAL_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                sendUserToFindContactsActivity();
            }
            else {
                Toast.makeText(getContext(), "You can't get access to your phone book contacts" +
                        ", you must confirm the permissions.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        chatIntent.putExtra("CONTACT", contactsList.get(position));
        startActivity(chatIntent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        getActivity().finish();
    }

    private class ContactsThread implements Runnable {

        private boolean stop;
        private Thread thread;

        public ContactsThread() {
            stop = false;
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
            while (!stop) {
                getActivity().runOnUiThread(() -> {
                    Cursor c = mResolver.query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.MIMETYPE + " =?",
                            new String[] {"vnd.android.cursor.item/com.example.wetalk.profile"},
                            null);

                    assert c != null;
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            String id = c.getString(c.getColumnIndex(ContactsContract.Data.DATA7));
                            String phone = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                            String name = c.getString(c.getColumnIndex(ContactsContract.Data.DATA2));
                            String userId = c.getString(c.getColumnIndex(ContactsContract.Data.DATA4));
                            String image = c.getString(c.getColumnIndex(ContactsContract.Data.DATA5));
                            String status = c.getString(c.getColumnIndex(ContactsContract.Data.DATA6));

                            if (!mAuth.getCurrentUser().getUid().equals(userId)) {
                                boolean contactState = mContactsSharedPreferences.getBoolean(userId + "_state", false);
                                if (contactState) {
                                    int unreadMessages = mContactsSharedPreferences.getInt(userId + "_unreadMessages", 0);
                                    DatabaseReference lastMessageRef = ref.child(getString(R.string.USERS))
                                            .child(userId).child("Messages")
                                            .child(mAuth.getCurrentUser().getUid());
                                    Query query = lastMessageRef.orderByKey().limitToLast(1);
                                    query.keepSynced(true);
                                    query.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                            if (dataSnapshot.exists()) {
                                                Message message = dataSnapshot.getValue(Message.class);
                                                message.setMessageId(dataSnapshot.getKey());
                                                Contact contact1 = new Contact(userId,id,name,phone,status,image,message,unreadMessages);
                                                if (contactsMap.containsKey(contact1.getUserId())) {
                                                    Contact contact2 = contactsMap.get(contact1.getUserId());
                                                    if (!contact1.equals(contact2)) {
                                                        contactsMap.remove(contact2.getUserId());
                                                        contactsMap.put(contact1.getUserId(),contact1);
                                                        contactsList.clear();
                                                        contactsList.addAll(contactsMap.values());
                                                        contactsRecyclerViewAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                                else {
                                                    contactsMap.put(contact1.getUserId(),contact1);
                                                    contactsList.clear();
                                                    contactsList.addAll(contactsMap.values());
                                                    contactsRecyclerViewAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });
                                }
                            }

                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        public boolean isStop() {
            return stop;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }
    }
}
