package com.univas.teusalesapp.teusalesapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
    private TextView PostDescription;
    private Button DeletePostButton, EditPostButton,negociarPostButton;

    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID, databaseUserID, description, image,userName,postagem;

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
//                   dataSnapshot.child("uid").getValue().toString();

                   PostDescription.setText(description);
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
                           EditCurrentPost(description);
                       }
                   });

               }
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
        SendUserToMainActivity();
        Toast.makeText(this, "Sua postagem foi deletada", Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity() {
        Intent ClickPostIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        ClickPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(ClickPostIntent);
        finish();
    }
}
