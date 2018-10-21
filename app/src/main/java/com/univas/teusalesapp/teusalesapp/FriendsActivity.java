package com.univas.teusalesapp.teusalesapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    //atualizar o status(online/offline) do user
    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //data padrão
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a"); //data padrão
        saveCurrentTime = currentTime.format(calForTime.getTime());

        //salvar no database
        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        //cria um novo campo(userState) na tabela Users do BD
        UsersRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
    }

    //quando o user acessa a tela de amigos ele fica online
    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    //quando user minimiza o app, ele fica offline.
    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    //se der erro no app
    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    // listar todos os amigos
    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        FriendsRef
                ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String usersIDs = getRef(position).getKey();
                //pega o id de cada amigo
                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                           final String userName = dataSnapshot.child("fullname").getValue().toString();
                           final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                           viewHolder.setFullname(userName);
                           viewHolder.setProfileimage(getApplicationContext(), profileImage);
                           //alert dialog
                           viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View view) {
                                   CharSequence options[] = new CharSequence[]{
                                     userName + "'s Perfil", "Enviar menssagem"
                                   };
                                   AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                   builder.setTitle("Selecione uma opção");

                                   builder.setItems(options, new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialogInterface, int i) {
                                           //i = é a posição do ponto da tela/alertDialog que foi clicadp
                                           if(i == 0){
                                               Intent profileintent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                               profileintent.putExtra("visit_user_id", usersIDs);
                                               startActivity(profileintent);
                                           }
                                           if(i == 1){
                                               Intent chatintent = new Intent(FriendsActivity.this, ChatActivity.class);
                                               chatintent.putExtra("visit_user_id", usersIDs);
                                               chatintent.putExtra("username", userName);
                                               startActivity(chatintent);
                                           }
                                       }
                                   });
                                   builder.show();
                               }
                           });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    //static class para o recyclerView
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);

        }
        public void setFullname(String fullname){
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setDate(String date){
            TextView friendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Amigos desde: " + date);
        }
    }
}
