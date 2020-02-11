package com.example.wetalk;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wetalk.Permissions.Permissions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View chatView;
    private FloatingActionButton mFindContacts;

    public ChatsFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatView = inflater.inflate(R.layout.fragment_chats, container, false);

        mFindContacts = chatView.findViewById(R.id.find_contacts_btn);

        mFindContacts.setOnClickListener(v -> checkContactsPermissions());

        return chatView;
    }

    private void checkContactsPermissions() {
        if (!Permissions.checkPermissions(Objects.requireNonNull(getContext()), Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS))
            sendUserToFindContactsActivity();
        else
            Permissions.ContactsPermissionDialog(getContext(), ChatsFragment.this);
    }

    private void sendUserToFindContactsActivity() {
        Intent findContactIntent = new Intent( getActivity(), FindContactActivity.class);
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
}
