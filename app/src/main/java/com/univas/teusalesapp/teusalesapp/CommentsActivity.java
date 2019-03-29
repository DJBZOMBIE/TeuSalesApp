package com.univas.teusalesapp.teusalesapp;

<<<<<<< HEAD
import android.content.Intent;
=======
>>>>>>> master
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
<<<<<<< HEAD
import android.util.Log;
=======
>>>>>>> master
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
<<<<<<< HEAD
import com.google.firebase.database.Query;
=======
>>>>>>> master
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;
<<<<<<< HEAD
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
=======

>>>>>>> master
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private String Post_key, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_key).child("Comments");

        CommentsList = (RecyclerView) findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = (EditText) findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton) findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       if(dataSnapshot.exists()){
                           String userName = dataSnapshot.child("username").getValue().toString();//pegar username

                           ValidateComment(userName);

                           CommentInputText.setText("");
                       }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

<<<<<<< HEAD
        Query SortPostsComments = PostsRef.orderByChild("timestempValue");

        final FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
=======
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
>>>>>>> master
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
<<<<<<< HEAD
                        SortPostsComments
                )



        {
            @Override
            protected void populateViewHolder(final CommentsViewHolder viewHolder, final Comments model, final int position) {

                viewHolder.getMyUserName().setText(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());



                viewHolder.getMyUserName().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String idUserComment = model.getUserId();
                        if(current_user_id.equals(idUserComment)){
                            Intent it = new Intent(CommentsActivity.this,ProfileActivity.class);
                            startActivity(it);
                        }
                        else{
                            Intent profileIntent = new Intent(CommentsActivity.this, PersonProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", idUserComment); //envia id do user
                            startActivity(profileIntent);
                        }


                    }
                });


            }


        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int comentsCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (comentsCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    CommentsList.scrollToPosition(positionStart);
                }
            }
        });

=======
                        PostsRef
                )
        {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position) {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
            }
        };

>>>>>>> master
        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    //static class suporte para o RecyclerView
    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
<<<<<<< HEAD
        TextView myUserName;

        public TextView getMyUserName() {
            return myUserName;
        }
=======
>>>>>>> master

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
<<<<<<< HEAD
            myUserName = (TextView) mView.findViewById(R.id.comment_username);
        }

//        public void setUsername(String username){
//
//            myUserName.setText("@"+username+" ");
//        }
=======
        }

        public void setUsername(String username){
            TextView myUserName = (TextView) mView.findViewById(R.id.comment_username);
            myUserName.setText("@"+username+" ");
        }
>>>>>>> master

        public void setComment(String comment){
            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date){
            TextView myDate = (TextView) mView.findViewById(R.id.comment_date);
            myDate.setText("  Data: "+date);
        }

        public void setTime(String time){
            TextView myTime = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText("  Hora: "+time);
        }

    }

    private void ValidateComment(String userName) {
        String commentText = CommentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Por favor, digite seu comentário...", Toast.LENGTH_SHORT).show();
        }else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //data padrão
            final String saveCurrentDate = currentDate.format(calFordDate.getTime()); //pega data padrao e salva na var saveCurrentDate

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss"); //hora/tempo padrão
            final String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime; //chave aleatória

<<<<<<< HEAD
            String nowt = String.valueOf(System.currentTimeMillis());

=======
>>>>>>> master
            //salvar comentário no banco
            HashMap commentsMap = new HashMap();
                commentsMap.put("uid", current_user_id);
                commentsMap.put("comment", commentText);
                commentsMap.put("date", saveCurrentDate);
                commentsMap.put("time", saveCurrentTime);
                commentsMap.put("username", userName);
<<<<<<< HEAD
                commentsMap.put("timestempValue",nowt);
=======
>>>>>>> master
                
            PostsRef.child(RandomKey).updateChildren(commentsMap)  //no banco de dados a var PostsRef(subTabela "Comments"), é filha da tabela "Posts"
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "Seu comentário foi realizado com sucesso...", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(CommentsActivity.this, "Ocorreu um erro, tente novamente...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
