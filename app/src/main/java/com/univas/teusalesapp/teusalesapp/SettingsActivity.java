package com.univas.teusalesapp.teusalesapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfImage;
    private final String TAG = "SettingsActivity";
    private ProgressDialog loadingBar;


    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;

    private String currentUserId;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Configurações da conta");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.settings_username);
        userProfName = (EditText) findViewById(R.id.settings_profile_full_name);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userCountry = (EditText) findViewById(R.id.settings_country);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userRelation = (EditText) findViewById(R.id.settings_relationship_status);
        userDOB = (EditText) findViewById(R.id.settings_dob);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        UpdateAccountSettingsButton = (Button) findViewById(R.id.update_account_settings_buttons);
        loadingBar = new ProgressDialog(this);

        //exibir dados retirados do banco de dados
        SettingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    try {


                        String myUserName = dataSnapshot.child("username").getValue().toString();
                        userName.setText(myUserName);
                        String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                        userProfName.setText(myProfileName);
                        String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                        userStatus.setText(myProfileStatus);
                        String myDOB = dataSnapshot.child("dob").getValue().toString() == "nome" ? "" : dataSnapshot.child("dob").getValue().toString() ;
                        userDOB.setText(myDOB);
                        String myCountry = dataSnapshot.child("country").getValue().toString();
                        userCountry.setText(myCountry);
                        String myGender = dataSnapshot.child("gender").getValue().toString();
                        userGender.setText(myGender);
                        String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                        userRelation.setText(myRelationStatus);
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    } catch(Exception e){
                        Log.i(TAG,e.getMessage());
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();

            //cortar imagem by ArthurHub(github)
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){ //Crop image result
                loadingBar.setTitle("Foto de Perfil");
                loadingBar.setMessage("Por favor, aguarde enquanto estamos atualizando sua foto de perfil...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg"); //referencia a foto do usuario no fire storage
                filePath.putFile(resultUri); //salva a foto cortada dentro do fire storage

                //add a foto no firebase database (basicamente vai salvar o link da imagem dentro do firabase database)
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image stored sucessfully to firabase storage...", Toast.LENGTH_SHORT).show();
                            final String downloadUri = task.getResult().getDownloadUrl().toString(); //pega o link da imagem

                            SettingsUserRef.child("profileimage").setValue(downloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                //depois de salvar a imagem de perfil que foi cortada, mande o usuário de volta para o settingsActivity para poder finalizar os dados: username, nome e país.
                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingsActivity.this, "Profile Image stored sucessfully to firabase storage...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();

                                            }else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Erro: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                        }
                    }
                });
            }else{
                Toast.makeText(this, "Erro: Imagem não pode ser cortada. Tente de novo...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    //validar campos
    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username)){

            Toast.makeText(this, "Por favor, digite seu nome de usuário...", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(profilename)){

            Toast.makeText(this, "Por favor, digite seu nome...", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(status)){

            Toast.makeText(this, "Por favor, digite seu status...", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(dob)){

            Toast.makeText(this, "Por favor, digite sua data de nascimento...", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(country)){

            Toast.makeText(this, "Por favor, digite seu país...", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(gender)){

            Toast.makeText(this, "Por favor, digite seu gênero...", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(relation)){

            Toast.makeText(this, "Por favor, digite seu status de relacionamento...", Toast.LENGTH_SHORT).show();

        }else{

            loadingBar.setTitle("Dados do Perfil");
            loadingBar.setMessage("Por favor, aguarde enquanto estamos atualizando seu perfil...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username, profilename, status, dob, country, gender, relation);

        }
    }

    private void UpdateAccountInfo(String username, String profilename, String status, String dob, String country, String gender, String relation) {

        HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", profilename);
            userMap.put("status", status);
            userMap.put("dob", dob);
            userMap.put("country", country);
            userMap.put("gender", gender);
            userMap.put("relationshipstatus", relation);
            SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){

                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Conta atualizada com sucesso...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }else{
                        Toast.makeText(SettingsActivity.this, "Ocorreu um erro durante a atualização da conta...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });
    }

    //botão de fazer voltar para a mainActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }



    public void onBackPressed() {
        super.onBackPressed();
    }

}
