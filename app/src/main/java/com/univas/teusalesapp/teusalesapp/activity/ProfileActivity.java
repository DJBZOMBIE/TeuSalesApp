package com.univas.teusalesapp.teusalesapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.univas.teusalesapp.teusalesapp.R;

import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        userName = (TextView) findViewById(R.id.my_profile_username);
        userProfName = (TextView) findViewById(R.id.my_profile_full_name);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        userRelation = (TextView) findViewById(R.id.my_relationship_status);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //listar dados (codigo reaproveitado da classe SettingsActivity)
                    try {
                        String myUserName = dataSnapshot.child("username").getValue().toString();
                        userName.setText("@" + myUserName);
                        String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                        userProfName.setText(myProfileName);
                        String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                        userStatus.setText(myProfileStatus);
                        String myDOB = dataSnapshot.child("dob").getValue().toString();
                        userDOB.setText("DDN: " + myDOB);
                        String myCountry = dataSnapshot.child("country").getValue().toString();
                        userCountry.setText("País: " + myCountry);
                        String myGender = dataSnapshot.child("gender").getValue().toString();
                        userGender.setText("Gênero: " + myGender);
                        String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                        userRelation.setText("Relacionamento: " + myRelationStatus);
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);


                    }catch (Exception e ){
                        Log.i("ProfileActivity",e.getMessage().toString());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
