package com.univas.teusalesapp.teusalesapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Minhas Postagens");

        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
    }

    //mostrar todas as postagens do usuario
    private void DisplayMyAllPosts() {
        Query myPostsQuery = PostsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>
                (
                     Posts.class,
                     R.layout.all_posts_layout,
                     MyPostsViewHolder.class,
                     myPostsQuery
                )
        {
            @Override
            protected void populateViewHolder(MyPostsViewHolder viewHolder, Posts model, int position) {
                //pegar os dados, exemplo: profilename, data, time, etc...
                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(), model.getPostimage());
            }
        };

        myPostsList.setAdapter(firebaseRecyclerAdapter);
    }

    //suporte pro recyclerView
    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public MyPostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time){
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("   " + time);
        }

        public void setDate(String date){
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("   " + date);
        }

        public void setDescription(String description){
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx, String postimage){
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postimage).into(PostImage);
        }
    }
}
