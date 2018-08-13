package com.univas.teusalesapp.teusalesapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationbutton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);


        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationbutton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });

    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Por Favor digite seu nome de usuário...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Por Favor digite seu nome de completo...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Por Favor digite o seu país...", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Salvar informação");
            loadingBar.setMessage("Por favor, aguarde enquanto criamos a sua conta nova conta...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            //guardar informação dentro do firebase database
            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "testing status");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipstatus", "none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUsertToMainActivity();
                        Toast.makeText(SetupActivity.this, "Conta criada con sucesso!", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Erro: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUsertToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
