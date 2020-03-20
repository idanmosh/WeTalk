package com.example.wetalk.Calling;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wetalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sinch.android.rtc.SinchClient;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView userProfileIImage;
    private ImageView btnCancel,btnAccept;
    private SinchClient sinchClient;

    private String reciverUserId , reciverUserimage , reciverUsername ;
    private String senderUserId  , senderUserimage , senderUsername;
    private DatabaseReference userRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);


        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reciverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        nameContact = findViewById(R.id.name_calling);
        btnCancel = findViewById(R.id.btnRejectCall);
        btnAccept = findViewById(R.id.btnAcceptCall);
        userProfileIImage = findViewById(R.id.profileImageUser) ;


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  cancelCall();

            }
        });

       // getAndsetProfileInfo();

    }
/*
    @Override
    protected void onStart() {
        super.onStart();


        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling"))
                {
                    btnAccept.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(senderUserId).child("Ringing").hasChild("CallEnding"))
                        {
                            Toast.makeText(CallingActivity.this, "call ending", Toast.LENGTH_SHORT).show();
                            finish();
                            removeCallEnding();
                        }

                        else if(dataSnapshot.child(senderUserId).child("Calling").hasChild("CallEnding"))
                        {
                            Toast.makeText(CallingActivity.this, "call ending", Toast.LENGTH_SHORT).show();
                            finish();
                            removeCallEnding();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void removeCallEnding() {///need to add multy string to make the code short

        userRef.child(senderUserId)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("CallEnding"))
                        {
                            userRef.child(reciverUserId)
                                    .child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                userRef.child(senderUserId)
                                                        .child("Calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {

                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                        else
                            finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        userRef.child(senderUserId)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("CallEnding"))
                        {
                            userRef.child(reciverUserId)
                                    .child("Calling")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                userRef.child(senderUserId)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                        else
                            finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




    }

    private void cancelCall() {


        userRef.child(senderUserId)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                        {
                            final HashMap<String,Object> endingInfoSender = new HashMap<>();
                            endingInfoSender.put("CallEnding",senderUserId);

                            userRef.child(reciverUserId)
                                    .child("Ringing")
                                    .setValue(endingInfoSender)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                final HashMap<String,Object> endingInfoReceiver = new HashMap<>();
                                                endingInfoReceiver.put("CallEnding",reciverUserId);
                                                userRef.child(senderUserId)
                                                        .child("Calling")
                                                        .setValue(endingInfoReceiver)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {

                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                        else
                            finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        userRef.child(senderUserId)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                        {

                            final HashMap<String,Object> endInfoSender = new HashMap<>();
                            endInfoSender.put("CallEnding",senderUserId);

                            userRef.child(reciverUserId)
                                    .child("Calling")
                                    .setValue(endInfoSender)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                final HashMap<String,Object> endInfoReceiver = new HashMap<>();
                                                endInfoReceiver.put("CallEnding",reciverUserId);

                                                userRef.child(senderUserId)
                                                        .child("Ringing")
                                                        .setValue(endInfoReceiver)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                        else
                            finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void getAndsetProfileInfo()
    {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(reciverUserId).exists())
                {
                    reciverUserimage = dataSnapshot.child(reciverUserId).child("image").getValue().toString();
                    reciverUsername = dataSnapshot.child(reciverUserId).child("name").getValue().toString();

                    nameContact.setText(reciverUsername);
                    Glide.with(getApplicationContext()).load(reciverUserimage)
                            .into(userProfileIImage);

                }
                if(dataSnapshot.child(senderUserId).exists())
                {
                    senderUserimage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUsername = dataSnapshot.child(senderUserId).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    */
}
