package com.univas.teusalesapp.teusalesapp;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mTollbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        //inicializar layouts: nav, drawer, toolbar, etc ...
        mTollbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mTollbar ); //add tollbar na mainActivity
        getSupportActionBar().setTitle("Home"); //título da tollbar

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open,R.string.drawer_close); //cria toggle
        drawerLayout.addDrawerListener(actionBarDrawerToggle); //add botao(toggle) no toolbar
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //recyclerview
        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);


        //atualizar foto de perfil e nome de usuário na navbar(puxa do firebase)
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Nome do perfil não existe...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPosts();

    }

    //exibir todos os posts dos usuários
    private void DisplayAllUsersPosts() {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                            Posts.class,
                            R.layout.all_posts_layout,
                            PostsViewHolder.class,
                            PostsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {

                        final String PostKey = getRef(position).getKey();//pegar a posição do post ao clicar

                        //pegar os dados, exemplo: profilename, data, time, etc...
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    //classe suporte para o FirebaseRecyclerAdapter
    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public PostsViewHolder(View itemView){
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

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent (MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    /*-------- FIREBASE Validations -------- */

    //confere se a autenticação do login do usuário foi um sucesso ou não
    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            SendUserToLoginActivity();
        }else{
            CheckUserExistence();
        }
    }

    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){ //se o registro do usuário não existe no "firebase real-time database". OBS: Validação mais importante do APP
                    SendUserToSetupActivity(); //envia o usuário para setup activity
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity(){
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    /*-------- FIM FIREBASE --------*/


    //faz funcionar o botao(Toggle) que tem a funnção de fazer aparecer o menu(navigationView) automaticamente
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //acesso as opções do navigationView
    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()){

            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;

            case R.id.nav_home:
                Toast.makeText(this,"Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this,"Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;

            case R.id.nav_messages:
                Toast.makeText(this,"Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                Toast.makeText(this,"Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_Logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToSettingsActivity(){
        Intent SettingIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingIntent);

    }

    private void SendUserToFindFriendsActivity(){
        Intent FindFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(FindFriendsIntent);
        finish();
    }

    private void SendUserToProfileActivity(){
        Intent ProfileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(ProfileIntent);
    }
}
