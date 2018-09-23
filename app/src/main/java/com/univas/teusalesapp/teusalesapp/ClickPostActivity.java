package com.univas.teusalesapp.teusalesapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription;
    private Button DeletePostButton, EditPostButton;

    private DatabaseReference ClickPostRef;

    private String PostKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        PostImage = (ImageView) findViewById(R.id.click_post_image);
        PostDescription = (TextView) findViewById(R.id.click_post_description);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);

        //listar a foto e a descrição da postagem(que foi clicada) na tela de edição de posts
        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String description = dataSnapshot.child("description").getValue().toString();
                String image = dataSnapshot.child("postimage").getValue().toString();

                PostDescription.setText(description);
                Picasso.with(ClickPostActivity.this).load(image).into(PostImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
