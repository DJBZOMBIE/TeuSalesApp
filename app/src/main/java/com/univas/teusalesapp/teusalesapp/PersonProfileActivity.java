package com.univas.teusalesapp.teusalesapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendRequestButton, DeclineFriendRequestButton;

    private DatabaseReference FriendRequestRef, usersRef;
    private FirebaseAuth mAuth;

    private String senderUserId, receiverUserId, CURRENT_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid(); //id do usuario online no app

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString(); //id do usuario que foi selecionado na tela de busca
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        InitializeFields();

        //mostrar informações do usuario
        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //listar dados (codigo reaproveitado da classe SettingsActivity)
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@" + myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DDN: " + myDOB);
                    userCountry.setText("País: " + myCountry);
                    userGender.setText("Gênero: " + myGender);
                    userRelation.setText("Relacionamento: " + myRelationStatus);

                    MaintananceofButtons();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        //validações dos botoes. Se o id do usuario online for diferente do id do usuario clicado, aparece o botao.
        if(!senderUserId.equals(receiverUserId)){
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendFriendRequestButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestToaPerson();
                    }
                }
            });

        }else{
            //se clicar no seu proprio perfil nao aparece os botoes
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    //alterar texto do botao (enviar pedido/cancelar solicitação de amizade)
    private void MaintananceofButtons() {
        FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId)){
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        SendFriendRequestButton.setText("Cancelar Solicitação");

                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                        DeclineFriendRequestButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //enviar solicitação de amizade
    private void SendFriendRequestToaPerson() {
        FriendRequestRef.child(senderUserId).child(receiverUserId) //envia pedido
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                         if(task.isSuccessful()){
                             FriendRequestRef.child(receiverUserId).child(senderUserId) //recebe pedido
                                     .child("request_type").setValue("received")
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                             if(task.isSuccessful()){
                                                 SendFriendRequestButton.setEnabled(true); //botao clicado
                                                 CURRENT_STATE = "request_sent"; //pedido enviado
                                                 SendFriendRequestButton.setText("Cancelar Solicitação");

                                                 DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                 DeclineFriendRequestButton.setEnabled(false);
                                             }
                                         }
                                     });
                         }
                    }
                });
    }

    private void InitializeFields() {
        userName = (TextView) findViewById(R.id.person_username);
        userProfName = (TextView) findViewById(R.id.person_full_name);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelation = (TextView) findViewById(R.id.person_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);

        SendFriendRequestButton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton= (Button) findViewById(R.id.person_decline_friend_request_btn);

        CURRENT_STATE = "not_friends";
    }
}
