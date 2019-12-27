package com.example.wetalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);



        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolBar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UserRef,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder > adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, int i, @NonNull Contacts contacts)
                    {
                        findFriendsViewHolder.userName.setText(contacts.getName());
                        findFriendsViewHolder.userStatus.setText(contacts.getStatus());
                        GlideImageImport(contacts, findFriendsViewHolder);
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();

    }


    private void GlideImageImport(Contacts contacts,FindFriendsViewHolder findFriendsViewHolder ){
        Glide.with(getApplicationContext()).asBitmap().load(contacts.getImage()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                findFriendsViewHolder.profileImage.setImageBitmap(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                Glide.with(getApplicationContext()).load(placeholder).into(findFriendsViewHolder.profileImage);
            }
        });
    }


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
