package com.example.wetalk;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

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

        mFindContacts.setOnClickListener(v -> sendUserToFindContactsActivity());


        return chatView;
    }

    private void sendUserToFindContactsActivity() {
        Intent findContactIntent = new Intent( getActivity(), FindContactActivity.class);
        findContactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findContactIntent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        getActivity().finish();
    }

}
