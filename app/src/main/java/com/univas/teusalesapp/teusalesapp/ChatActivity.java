package com.univas.teusalesapp.teusalesapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar ChattoolBar;
    private ImageButton SendMessageButton, SendImagefileButton;
    private EditText userMessageInput;

    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private String messageReceiverID, messageReceiverName, messageSenderID, saveCurrentDate, saveCurrentTime;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef, UserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName= getIntent().getExtras().get("username").toString();

        IntializeFields();
        DisplayReceiverInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        FetchMessages();
    }

    //BUSCAR MENSAGENS
    private void FetchMessages() {
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //enviar mensagem
    private void SendMessage() {
        updateUserStatus("online");//quando user manda mensagem ele fica online
        String messageText = userMessageInput.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Por favor digite uma mensagem primeiro...", Toast.LENGTH_SHORT).show();
        }else{
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID; //referencia o id (do usuario) que enviou mensagem (montagem da arvore de mensagens enviadas no database)
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID; //refencia o id (do usuario) que recebeu a mensagem
            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();//push cria um id unico para as mensagens
            String message_push_id = user_message_key.getKey();

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //data padrão
            saveCurrentDate = currentDate.format(calFordDate.getTime()); //pega data padrao e salva na var saveCurrentDate
            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss aa"); //hora/tempo padrão
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            //salvar mensagem no bd
            Map messageTextBody = new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", messageSenderID);

            //salvar os id's dos users que enviaram/receberam mensagens
           Map messageBodyDetails = new HashMap();
               messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
               messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

           //salva a arvore de mensagens no BD
           RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if(task.isSuccessful()){
                       Toast.makeText(ChatActivity.this, "Mensagem enviada com sucesso.", Toast.LENGTH_SHORT).show();
                       userMessageInput.setText(""); //limpar campo de texto
                   }else{
                       String message = task.getException().getMessage();
                       Toast.makeText(ChatActivity.this, "Erro: " + message, Toast.LENGTH_SHORT).show();
                   }
               }
           });
        }
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
        UserRef.child(messageSenderID).child("userState").updateChildren(currentStateMap);
    }

    //mostra o nome, foto e status(online ou visto pela ultima vez(off)) do usuario na barra de titulo do chat
    private void DisplayReceiverInfo() {
       receiverName.setText(messageReceiverName); //seta o nome

       RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                   final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                   final String LastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                   final String LastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                   if(type.equals("online")){
                       //se user ta online
                       userLastSeen.setText("online");
                   }else{
                       userLastSeen.setText("visto pela última vez: " + LastTime + " " + LastDate);
                   }

                   Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage); //seta a foto
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
    }

    private void IntializeFields() {
        ChattoolBar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChattoolBar);

        //conectar ChatActivity com o chat_custom_bar.xml
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        receiverName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SendImagefileButton = (ImageButton) findViewById(R.id.send_image_file_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);
    }
}
