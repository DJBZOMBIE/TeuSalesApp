package com.univas.teusalesapp.teusalesapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef, FriendsRef, PostsRef;
    private FirebaseAuth mAuth;
    private Button MyPosts, MyFriends;

    private String currentUserId;
    private int countFriends = 0, countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        userName = (TextView) findViewById(R.id.my_profile_username);
        userProfName = (TextView) findViewById(R.id.my_profile_full_name);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        userRelation = (TextView) findViewById(R.id.my_relationship_status);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        MyFriends = (Button) findViewById(R.id.my_friends_button);
        MyPosts = (Button) findViewById(R.id.my_post_button);

        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToFriendsActivity();
            }
        });

        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToMyPostsActivity();
            }
        });

        //alterar o texto do botao(postagens) com a quantidade de postagens realizadas
        PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //se tem postagens
                    countPosts = (int) dataSnapshot.getChildrenCount();
                    MyPosts.setText(Integer.toString(countPosts) + "  Postagens");
                }else{
                   //se nao tem postagens
                    MyPosts.setText("0 Postagens");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //alterar o texto do botao(amigos) com a quantidade de amigos
        FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //se users tem amigos
                    countFriends = (int) dataSnapshot.getChildrenCount();//conta o numero de amigos(childs) no database
                    MyFriends.setText(Integer.toString(countFriends) + "  Amigos");
                }else{
                    //se users não ten amigos
                    MyFriends.setText("0 Amigos");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    try {

                        //listar dados (codigo reaproveitado da classe SettingsActivity)
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        String myUserName = dataSnapshot.child("username").getValue().toString();
                        String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                        String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                        String myDOB = dataSnapshot.child("dob").getValue().toString();
                        String myCountry = dataSnapshot.child("country").getValue().toString();
                        String myGender = dataSnapshot.child("gender").getValue().toString();
                        String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                        Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                        userName.setText("@" + myUserName);
                        userProfName.setText(myProfileName);
                        userStatus.setText(myProfileStatus);
                        userDOB.setText("DDN: " + myDOB);
                        userCountry.setText("País: " + myCountry);
                        userGender.setText("Gênero: " + myGender);
                        userRelation.setText("Relacionamento: " + myRelationStatus);

                    }catch (Exception e){
                        Log.e("Profile",e.getMessage());
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToFriendsActivity(){
        Intent FriendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(FriendsIntent);
    }

    private void SendUserToMyPostsActivity(){
        Intent myPostsIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(myPostsIntent);
    }
}
