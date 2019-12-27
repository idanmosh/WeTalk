package com.example.wetalk;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactRef, UsersRef;

    private FirebaseAuth mAuto;
    private String CurrectUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView =  inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList = ContactsView.findViewById(R.id.contacts_list);

        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuto = FirebaseAuth.getInstance();

        CurrectUserId = mAuto.getCurrentUser().getUid();

        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrectUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactRef, Contacts.class)
                .build();

        final FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {

                        final String userIds = getRef(i).getKey();
                        UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.hasChild("image"))
                                {
                                    contacts.setImage(dataSnapshot.child("image").getValue().toString());
                                    contacts.setName(dataSnapshot.child("status").getValue().toString());
                                    contacts.setStatus(dataSnapshot.child("name").getValue().toString());

                                    contactsViewHolder.userName.setText(contacts.getName());
                                    contactsViewHolder.userStatus.setText(contacts.getStatus());
                                    GlideImageImport(contacts.getImage(), contactsViewHolder);


                                }
                                else
                                {
                                    contacts.setName(dataSnapshot.child("status").getValue().toString());
                                    contacts.setStatus(dataSnapshot.child("name").getValue().toString());

                                    contactsViewHolder.userName.setText(contacts.getName());
                                    contactsViewHolder.userStatus.setText(contacts.getStatus());
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
                        return contactsViewHolder;
                    }
                };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void GlideImageImport(String image, ContactsFragment.ContactsViewHolder contactsViewHolder ){
        Glide.with(this).asBitmap().load(image).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                contactsViewHolder.profileImage.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                Glide.with(getContext()).load(placeholder).into(contactsViewHolder.profileImage);
            }
        });
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
