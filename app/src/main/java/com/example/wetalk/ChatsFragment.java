package com.example.wetalk;


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
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
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
import com.sinch.gson.Gson;
import com.sinch.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ContactsRecyclerViewAdapter.ListItemClickListener  {

    private static final String MyPREFERENCES = "ContactsPrefs";

    private SharedPreferences mSharedPreferences;
    private View chatView;
    private FloatingActionButton mFindContacts;
    private RecyclerView contactsRecyclerView;
    private List<Contact> contactsList = new ArrayList<>();
    private final Map<String, Contact> contactsMap = new HashMap<>();
    private List<Message> messageList = new ArrayList<>();
    private HashMap<String, Message> messageMap = new HashMap<>();
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private ContactsRecyclerViewAdapter contactsRecyclerViewAdapter;

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        mFindContacts = chatView.findViewById(R.id.find_contacts_btn);

        mSharedPreferences = getActivity().getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        contactsRecyclerView = chatView.findViewById(R.id.chat_list);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (!Permissions.checkPermissions(Objects.requireNonNull(getContext()), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS))
            getLoaderManager().initLoader(0, null, this);

        mFindContacts.setOnClickListener(v -> checkContactsPermissions());

        contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(
                contactsList,2, this);

        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);

        return chatView;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart() {
        super.onStart();
        if (!Permissions.checkPermissions(Objects.requireNonNull(getContext()), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS))
            getLoaderManager().initLoader(0, null, this);
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(Objects.requireNonNull(getContext()),
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.MIMETYPE + " =?",
                new String[] {"vnd.android.cursor.item/com.example.wetalk.profile"},
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor c) {
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndex(ContactsContract.Data.DATA7));
                String phone = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                String name = c.getString(c.getColumnIndex(ContactsContract.Data.DATA2));
                String userId = c.getString(c.getColumnIndex(ContactsContract.Data.DATA4));
                String image = c.getString(c.getColumnIndex(ContactsContract.Data.DATA5));
                String status = c.getString(c.getColumnIndex(ContactsContract.Data.DATA6));

                if (!mAuth.getCurrentUser().getUid().equals(userId)) {
                    Contact contact = new Contact(userId,id,name,phone,status,image);

                    if (mSharedPreferences.contains(userId + "_state") && mSharedPreferences.contains(userId + "_messageList")) {
                        Gson gson = new Gson();
                        Type typeList = new TypeToken<List<Message>>() {}.getType();
                        String jsonMessageList = mSharedPreferences.getString(userId + "_messageList", "");
                        List<Message> messageList = gson.fromJson(jsonMessageList, typeList);
                        contact.setLastMessage(messageList.get(messageList.size()-1));
                        contact.setUnreadMessages(getUnreadMessages(messageList));

                        contactsMap.put(contact.getUserId(), contact);
                    }

                    ref.child(getString(R.string.USERS))
                            .child(contact.getUserId()).child("Messages")
                            .child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Message message = dataSnapshot.getValue(Message.class);
                            Objects.requireNonNull(message).setMessageId(dataSnapshot.getKey());
                            setContactState(contact);
                            if (!messageMap.containsKey(message.getMessageId())) {
                                messageMap.put(message.getMessageId(), message);
                                messageList.add(message);
                                setContactShredPreferences(contact);
                            }
                            contactsRecyclerViewAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Message message = dataSnapshot.getValue(Message.class);
                            Objects.requireNonNull(message).setMessageId(dataSnapshot.getKey());
                            messageList.remove(messageMap.get(message.getMessageId()));
                            messageList.add(message);
                            messageMap.remove(dataSnapshot.getKey());
                            messageMap.put(message.getMessageId(), message);
                            setContactShredPreferences(contact);
                            contactsRecyclerViewAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        }

        contactsList = new ArrayList<>(contactsMap.values());

        contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(
                contactsList,2, this);

        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);
    }

    private void setContactState(Contact contact) {
        mSharedPreferences.edit().putBoolean(contact.getUserId() + "_state", true).apply();
    }

    private void getContactShredPreferences(Contact contact) {
        Gson gson = new Gson();
        String jsonMessageList = mSharedPreferences.getString(contact.getUserId() + "_messageList", "");
        String jsonMessageMap = mSharedPreferences.getString(contact.getUserId() + "_messageMap", "");

        Type typeList = new TypeToken<List<Message>>() {}.getType();
        Type typeMap = new TypeToken<HashMap<String, Message>>() {}.getType();
        if ((!jsonMessageList.equals("")) && (!jsonMessageMap.equals(""))) {
            messageList = gson.fromJson(jsonMessageList, typeList);
            messageMap = gson.fromJson(jsonMessageMap, typeMap);
        }
    }

    private void setContactShredPreferences(Contact contact) {
        Gson gson = new Gson();
        String jsonMessageList = gson.toJson(messageList);
        String jsonMessageMap = gson.toJson(messageMap);

        mSharedPreferences.edit().putString(contact.getUserId() + "_messageList", jsonMessageList).apply();
        mSharedPreferences.edit().putString(contact.getUserId() + "_messageMap", jsonMessageMap).apply();
    }

    private int getUnreadMessages(List<Message> messageList) {
        int unread = 0;
        for (int i = messageList.size()-1; i <= 0; i++) {
            if (messageList.get(i).getState().equals("unread"))
                unread++;
            else
                break;
        }
        return unread;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
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
}
