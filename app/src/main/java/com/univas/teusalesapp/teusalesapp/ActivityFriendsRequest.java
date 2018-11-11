package com.univas.teusalesapp.teusalesapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityFriendsRequest extends AppCompatActivity {

    private RecyclerView myRequestFriendsList;
    private DatabaseReference reqFriendsRef, UsersRef,FriendsRef,allreqFriendsRef;
    private FirebaseAuth mAuth;
    private String online_user_id,saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_request);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        reqFriendsRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(online_user_id).orderByChild("request_type").equalTo("receive").getRef();
        allreqFriendsRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");



        myRequestFriendsList = (RecyclerView) findViewById(R.id.reqFriends_list);
        myRequestFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myRequestFriendsList.setLayoutManager(linearLayoutManager);


       DisplayAllReqFriends();




    }

    // listar todas as requisições de amizade
    private void DisplayAllReqFriends() {
        FirebaseRecyclerAdapter<Friends, fRequestViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, fRequestViewHolder>
                (
                        Friends.class,
                        R.layout.content_activity_friends_request,
                       fRequestViewHolder.class,
                        reqFriendsRef
                ) {
            @Override
            protected void populateViewHolder(final fRequestViewHolder viewHolder, Friends model, int position) {
                //viewHolder.setDate(model.);
                final String usersIDs = getRef(position).getKey();
                //pega o id de cada amigo
                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                            final String type;

                            //verificar se userSstate existe
                            if(dataSnapshot.hasChild("userState")){
                                type = dataSnapshot.child("userState").child("type").getValue().toString(); //recebe status(online/offline)
                                if(type.equals("online")){
                                    viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                }else{
                                    viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            viewHolder.btnAcept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    AcceptFriendRequest(usersIDs,online_user_id);



                                }
                            });


                            viewHolder.btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CancelFriendRequest(usersIDs,online_user_id);
                                }
                            });

                            viewHolder.setFullname(userName);
                            viewHolder.setProfileimage(getApplicationContext(), profileImage);
                            //alert dialog

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        myRequestFriendsList.setAdapter(firebaseRecyclerAdapter);
    }



    public static class fRequestViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView onlineStatusView;
        Button btnAcept;
        Button btnCancel;
        public fRequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.reqF_user_online_icon_req);
            btnAcept = (Button) itemView.findViewById(R.id.reqF__acept_friend_request);
            btnCancel = (Button) itemView.findViewById(R.id.reqF_decline_friend_request);
        }
        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.reqF_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);

        }
        public void setFullname(String fullname){
            TextView myName = (TextView) mView.findViewById(R.id.reqF_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setDate(String date){
            TextView friendsDate = (TextView) mView.findViewById(R.id.reqF_users_status);
            friendsDate.setText("Ultima Mensagem " + date);
        }
    }



    //aceitar pedido de amizade
    private void AcceptFriendRequest(final String receiverUserId, final String senderUserId) {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //data padrão
        saveCurrentDate = currentDate.format(calFordDate.getTime()); //pega data padrao e salva na var saveCurrentDate

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate) //enviar
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate) //recebido
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                //remover pedido de amizade do BD
                                                allreqFriendsRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    allreqFriendsRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(ActivityFriendsRequest.this,"Pedido de amizade aceito",Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });


    }




    //cancelar envio/pedido de solicitação de amizade
    private void CancelFriendRequest(final String receiverUserId, final String senderUserId) {
        //remover pedido de amizade do BD
        allreqFriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            allreqFriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                            }
                                        }
                                    });
                        }
                    }
                });
    }


}
