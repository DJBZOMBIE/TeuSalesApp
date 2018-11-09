package com.univas.teusalesapp.teusalesapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityFriendsRequest extends AppCompatActivity {

    private RecyclerView myChatList;
    private DatabaseReference reqFriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_request);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        reqFriendsRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");



        myChatList = (RecyclerView) findViewById(R.id.reqFriends_list);
        myChatList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myChatList.setLayoutManager(linearLayoutManager);
    }

}
