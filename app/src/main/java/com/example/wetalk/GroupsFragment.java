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
public class GroupsFragment extends Fragment {

    private View view;
    private FloatingActionButton mCreateGroup;

    public GroupsFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_groups, container, false);

        mCreateGroup = view.findViewById(R.id.create_group_btn);

        mCreateGroup.setOnClickListener(v -> sendUserToGroupActivity());



        return view;
    }

    private void sendUserToGroupActivity() {
        Intent groupIntent = new Intent( getActivity(), GroupActivity.class);
        groupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(groupIntent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        getActivity().finish();
    }

}
