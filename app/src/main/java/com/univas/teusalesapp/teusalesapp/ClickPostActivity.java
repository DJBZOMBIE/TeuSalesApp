package com.univas.teusalesapp.teusalesapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription, txtpostValue;
    private Button DeletePostButton, EditPostButton,negociarPostButton;

    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID, databaseUserID, description, image,userName,postagem,postValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        PostImage = (ImageView) findViewById(R.id.click_post_image);
        PostDescription = (TextView) findViewById(R.id.click_post_description);
        negociarPostButton= (Button) findViewById(R.id.negociarPost);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);
        txtpostValue = (TextView) findViewById(R.id.txtValuePost);

        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);
        negociarPostButton.setVisibility(View.INVISIBLE);

        //listar a foto e a descrição da postagem(que foi clicada) na tela de edição/exibição de posts
        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){
                   description = dataSnapshot.child("description").getValue().toString();

                   userName = dataSnapshot.child("fullname").getValue().toString();
                   image = dataSnapshot.child("postimage").getValue().toString();
                   databaseUserID = dataSnapshot.child("uid").getValue().toString(); //pega o id do usuario que fez a postagem
                   postagem = dataSnapshot.child("uid").getValue().toString();
                   postValue = dataSnapshot.child("value").getValue().toString();
//                   dataSnapshot.child("uid").getValue().toString();

                   PostDescription.setText(description);
                   txtpostValue.setText("R$ "+postValue);
                   Picasso.with(ClickPostActivity.this).load(image).into(PostImage);
                   //condição para verificar o id do usuario que fez a postagem, se for o id do autor do post, então os botões edit e delete aparecem
                   if(currentUserID.equals(databaseUserID)){
                       DeletePostButton.setVisibility(View.VISIBLE);
                       negociarPostButton.setVisibility(View.GONE);
                       EditPostButton.setVisibility(View.VISIBLE);
                   }
                   else
                   {
                       negociarPostButton.setVisibility(View.VISIBLE);
                   }

                   negociarPostButton.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent it = new Intent(getApplication(),ChatActivity.class);

                           it.putExtra("visit_user_id", databaseUserID);
                           it.putExtra("username", userName);
                           startActivity(it);
                       }
                   });

                   EditPostButton.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {

                                   AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);


                                   View v = getLayoutInflater().inflate(R.layout.editpost, null);
                                   final EditText editValue = (EditText) v.findViewById(R.id.editpost_post_value);
                                   final EditText editDescription = (EditText) v.findViewById(R.id.editpost_post_description);

                                   editValue.setText(postValue);
                                   editDescription.setText(description);
                                   builder.setPositiveButton("Concluir", new DialogInterface.OnClickListener() {
                                       @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           ClickPostRef.child("value").setValue(editValue.getText().toString());//atualiza post
                                           ClickPostRef.child("description").setValue(editDescription.getText().toString());//atualiza post
                                           Toast.makeText(ClickPostActivity.this, "Sua postagem foi atualizada com sucesso...", Toast.LENGTH_SHORT).show();
                                       }
                                   });



                                   builder.setNeutralButton("Voltar", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {



                                       }
                                   });



                                   builder.setView(v);
                                   builder.create();
                                   builder.show();





                               }
                           });


                       }

                        //   EditCurrentPost(description);
                       }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentPost();
            }
        });

    }

    //editar postagem
    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Editar Postagem: ");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);//exibir descrição da postagem antiga
        builder.setView(inputField);

        //se clicar em atualizar, atualiza o post.
        builder.setPositiveButton("Atualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClickPostRef.child("description").setValue(inputField.getText().toString());//atualiza post
                Toast.makeText(ClickPostActivity.this, "Sua postagem foi atualizada com sucesso...", Toast.LENGTH_SHORT).show();
            }
        });

        //se clicar em cancelar, cancela o evento.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_light);
    }

    //deletar post do database
    private void DeleteCurrentPost() {
        ClickPostRef.removeValue();
        Toast.makeText(this, "Sua postagem foi deletada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void SendUserToMainActivity() {
        Intent ClickPostIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        ClickPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(ClickPostIntent);
        finish();
    }
}
