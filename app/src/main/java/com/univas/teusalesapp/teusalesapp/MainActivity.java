package com.univas.teusalesapp.teusalesapp;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mTollbar;
    private boolean filter = false;
    private int countReqf = 0;
    private LinearLayoutManager linearLayoutManager;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private TextView txtNReqFriends;
    private ImageButton AddNewPostButton;
    private ImageButton aceptNewFriends;
    private ImageButton searchPost;
    private Integer indexEstado = 0;
    private Integer indexCidade = 0;
    private JSONObject obj;
    private String cidade;
    private AlertDialog alert;



    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef,requestFriendsRef;

    String currentUserID;
    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //Firebase
            mAuth = FirebaseAuth.getInstance();
            currentUserID = mAuth.getCurrentUser().getUid();
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            requestFriendsRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");



            //inicializar layouts: nav, drawer, toolbar, etc ...
            mTollbar = (Toolbar) findViewById(R.id.main_page_toolbar);
            setSupportActionBar(mTollbar ); //add tollbar na mainActivity
            getSupportActionBar().setTitle("Home"); //título da tollbar

            AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);
            aceptNewFriends = (ImageButton) findViewById(R.id.aceptReqFriends);
            txtNReqFriends = (TextView) findViewById(R.id.txtNumberReqF);
            searchPost = (ImageButton) findViewById(R.id.searchPost);

            drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
            actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open,R.string.drawer_close); //cria toggle
            drawerLayout.addDrawerListener(actionBarDrawerToggle); //add botao(toggle) no toolbar
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            navigationView = (NavigationView) findViewById(R.id.navigation_view);

            //recyclerview
            postList = (RecyclerView) findViewById(R.id.all_users_post_list);
            postList.setHasFixedSize(true);
            linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            postList.setLayoutManager(linearLayoutManager);

            View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
            NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
            NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);





            requestFriendsRef.child(currentUserID).addValueEventListener((new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    countReqf = 0;
                    if(dataSnapshot.exists()){
                        for (DataSnapshot rfSnapshot: dataSnapshot.getChildren()) {
                            String user = rfSnapshot.getKey();

                            if(rfSnapshot.child("request_type").getValue().toString().equals("received")){
                                countReqf++;
                            }


                        }

                      // int  countReqFriends = (int) dataSnapshot.getChildrenCount();

                        txtNReqFriends.setText(countReqf> 0 ?Integer.toString(countReqf) : "");

                    }
                    else {
                        txtNReqFriends.setText("");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            }));


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

            aceptNewFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendUserToFriendsRequestActtivity();
                }
            });


            searchPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {




                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


                    View v = getLayoutInflater().inflate(R.layout.filter_post, null);
                    final Spinner spCidades = (Spinner) v.findViewById(R.id.spinnerCidade);
                    final Spinner spEstados = (Spinner) v.findViewById(R.id.spinnerEstado);



                    String estados = loadJSONFromAsset(v.getContext());



                    try {
                        obj = new JSONObject(estados);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }




                    try {
                        JSONArray arr =     obj.getJSONArray("estados");

                        List<String> listEstados = new ArrayList<String>();
                        for (int i = 0; i <= arr.length() -1;i++){

                            listEstados.add(arr.optJSONObject(i).getString("nome").toString());

                        }

                        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_list_item_1, listEstados);

                        spEstados.setAdapter(adapterSpinner);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                    spEstados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            indexEstado = i;

                            try {
                                JSONArray arr = obj.getJSONArray("estados").getJSONObject(i).getJSONArray("cidades");

                                List<String> listCidades = new ArrayList<String>();
                                for (int j = 0; j < arr.length() -1;j++){

                                    listCidades.add(arr.getString(j));

                                }

                                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, listCidades);

                                spCidades.setAdapter(adapterSpinner);



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });



                    builder.setPositiveButton("Aplicar Filtro", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           cidade = spCidades.getSelectedItem().toString();
                           filter = true;



                            postList = (RecyclerView) findViewById(R.id.all_users_post_list);
                            postList.setHasFixedSize(true);
                            linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                            linearLayoutManager.setReverseLayout(true);
                            linearLayoutManager.setStackFromEnd(true);
                            postList.setLayoutManager(linearLayoutManager);


                            DisplayAllUsersPosts();


                          //  PostsRef = PostsRef.orderByChild("city").startAt(spCidades.getSelectedItem().toString()).endAt(spCidades.getSelectedItem().toString()+"\uf8ff").getRef();



    //                        deposito =  spFiltro.getSelectedItem().toString().split("-")[0].trim();
    //
    //                        adapter = new SeparacaoPedidosArrayAdapter(enderecos,deposito,depositos);
    //                        recyclerView.setLayoutManager(layoutManager);
    //                        adapter.setOnItemClickListener(newListenner);
    //
    //
    //                        recyclerView.setAdapter(adapter);



                        }
                    });



                    builder.setNeutralButton("Voltar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            filter = false;


                        }
                    });



                    builder.setView(v);
                    builder.create();
                    builder.show();





                }
            });

            DisplayAllUsersPosts();

    }



//    public boolean onOptionsItemSelected(MenuItem item)  {
//        int id = item.getItemId();
//
//        if (id == R.id.action_filtros) {
//
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//            View view = this.getLayoutInflater().inflate(R.layout.filtro_separacao_pedidos, null);
//            final Spinner spFiltro = (Spinner) view.findViewById(R.id.spinner2);
//            sg.clear();
//
//            for(int i = 0; i < depositos.length(); i++ ){
//
//                try {
//                    sg.add(depositos.getJSONObject(i).getString("DEPCOD").toString()+" - "+depositos.getJSONObject(i).getString("DEPNOM").toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
//                    android.R.layout.simple_spinner_item, sg);
//            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spFiltro.setAdapter(arrayAdapter);
//
//
//
//            builder.setPositiveButton("Aplicar Filtro", new DialogInterface.OnClickListener() {
//                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                    Toast.makeText(getApplication(), "Filtro", Toast.LENGTH_SHORT).show();
//
//                    deposito =  spFiltro.getSelectedItem().toString().split("-")[0].trim();
//
//                    adapter = new SeparacaoPedidosArrayAdapter(enderecos,deposito,depositos);
//                    recyclerView.setLayoutManager(layoutManager);
//                    adapter.setOnItemClickListener(newListenner);
//
//
//                    recyclerView.setAdapter(adapter);
//
//
//
//                }
//            });
//
//
//
//            builder.setNeutralButton("Voltar", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                    Toast.makeText(getApplication(), "Voltar", Toast.LENGTH_SHORT).show();
//
//                }
//            });
//
//
//
//            builder.setView(view);
//            builder.create();
//
//
//
//            builder.show();
//
//
//
//
//        }
//
//        return true;
//    }
//


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
        UsersRef.child(currentUserID).child("userState").updateChildren(currentStateMap);
    }

    //exibir todos os posts dos usuários
    private void DisplayAllUsersPosts() {

        //organizar postagens na linha do tempo
        Query SortPostsInDecendingOrder = null;

        if(!filter){
            SortPostsInDecendingOrder = PostsRef.orderByChild("timestempValue");
        }
        else{
            SortPostsInDecendingOrder = PostsRef.orderByChild("city").equalTo(cidade);
        }



        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                            Posts.class, R.layout.all_posts_layout, PostsViewHolder.class, SortPostsInDecendingOrder
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, final Posts model, int position) {



                        //pegar os dados, exemplo: profilename, data, time, etc...
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDescription(model.getDescription());
                        //viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                        Picasso.with(getApplicationContext()).load(model.getProfileimage()).into(viewHolder.getImage());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());


                        viewHolder.getImage().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(currentUserID.equals(model.getUid())){
                                    Intent it = new Intent(MainActivity.this,ProfileActivity.class);
                                    startActivity(it);
                                }else{
                                    Intent it = new Intent(MainActivity.this,PersonProfileActivity.class);
                                    it.putExtra("visit_user_id",model.getUid());
                                    startActivity(it);
                                }
                            }
                        });



                        try{
                            viewHolder.setState(model.getState(),model.getCity());
//                            viewHolder.setCity();
                            viewHolder.setValue("R$"+ model.value.replace(".",","));
                            viewHolder.setDate(model.getDate());


                        }catch (Exception e){
                            Log.e("populateViewHolder",e.getMessage());
                        }

                        final String PostKey = getRef(position).getKey();//pegar a posição do post ao clicar




                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        //comments button
                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey", PostKey);
                                startActivity(commentsIntent);
                            }
                        });

                        //like button
                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LikeChecker = true;

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                      if(LikeChecker.equals(true)){
                                          if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                              //se like existe
                                              LikesRef.child(PostKey).child(currentUserID).removeValue(); //remove like
                                              LikeChecker = false;
                                          }else{
                                              LikesRef.child(PostKey).child(currentUserID).setValue(true);//add like
                                              LikeChecker = false;
                                          }
                                      }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online"); //quando as postagens aparecerem o user fica online
    }

    //classe suporte para o FirebaseRecyclerAdapter
    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        ImageButton LikePostButton, CommentPostButton;
        TextView value;
        TextView state;
      //  TextView city;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRefe;;
        CircleImageView image;

        public PostsViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) mView.findViewById(R.id.display_no_of_likes);

            LikesRefe = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            image = (CircleImageView) mView.findViewById(R.id.post_profile_image);


//            public void setProfileimage(Context ctx, String profileimage){
//                CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
//                Picasso.with(ctx).load(profileimage).into(image);
//            }

        }

        //status do botao/coração like(cor)
        public void setLikeButtonStatus(final String PostKey){
            LikesRefe.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                   if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                       countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();//quantidade de likes dado na postagem
                       LikePostButton.setImageResource(R.drawable.like);
                       DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                   }else{
                       countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();//quantidade de likes dado na postagem
                       LikePostButton.setImageResource(R.drawable.dislike);
                       DisplayNoOfLikes.setText(Integer.toString(countLikes)+(" Likes"));
                   }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }



        public void setValue(String value) {
            TextView txtvalue = (TextView) mView.findViewById(R.id.post_value_main);
            txtvalue.setText(value);
        }


        public void setState(String state,String cidade) {
            TextView txtstate = (TextView) mView.findViewById(R.id.post_state);
            txtstate.setText(state+" - "+cidade);
        }


//        public void setCity(String city) {
//            TextView txtcity = (TextView) mView.findViewById(R.id.post_city);
//            txtcity.setText(city);
//
//        }


        public CircleImageView getImage() {
            return image;
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
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

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //se o registro do usuário não existe no "firebase real-time database". OBS: Validação mais importante do APP

                    if(!dataSnapshot.hasChild("username")){
                        SendUserToSetupActivity();
                    }
                     //envia o usuário para setup activity
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
                SendUserToMainActivity();
                break;

            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;

            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;

            case R.id.nav_messages:
                SendUserToChatActivity();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;

            case R.id.nav_Logout:
                updateUserStatus("offline"); //quando o user clica em sair ele fica offline
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToMainActivity(){
        Intent HomeIntent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(HomeIntent);

    }

    private void SendUserToFriendsActivity(){
        Intent FriendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(FriendsIntent);

    }

    private void SendUserToSettingsActivity(){
        Intent SettingIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingIntent);

    }

    private void SendUserToFindFriendsActivity(){
        Intent FindFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(FindFriendsIntent);

    }

    private void SendUserToChatActivity(){
        Intent chatIntent = new Intent(MainActivity.this, ChatsActivity.class);
        startActivity(chatIntent);

    }

    private void SendUserToProfileActivity(){
        Intent ProfileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(ProfileIntent);

    }

    private void SendUserToFriendsRequestActtivity(){
        if(countReqf > 0){
            Intent friendsRequestIntent = new Intent(this,ActivityFriendsRequest.class);
            startActivity(friendsRequestIntent);
        }
        else{
            Toast.makeText(this,"Não há solicitação de amizade!",Toast.LENGTH_SHORT).show();
        }

    }






    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = this.getAssets().open("CidadesEstados.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (Exception e) {
            Log.e("LoadJsonFile",e.getMessage());
            return null;
        }
        return json;

    }




}
