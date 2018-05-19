package com.example.ana.helpme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfileActivity extends AppCompatActivity {

    EditText userName, userSurname;
    TextView email;
    Button save;
    FirebaseAuth auth;

    DatabaseReference database;

    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Dohvacanje korisnickog unosa:
        userName = (EditText) findViewById(R.id.editTextName);
        userSurname = (EditText) findViewById(R.id.editTextSurname);
        save = (Button) findViewById(R.id.buttonSave);
        email = (TextView) findViewById(R.id.TextViewEmail);


        //Autentikacija - provjera korisnika
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference().child("Korisnici").child(auth.getCurrentUser().getUid());

        email.setText(auth.getCurrentUser().getEmail());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();
            }
        });
    }

    private void saveUser(){
        final String name, surname, email;

        name = userName.getText().toString().trim();
        surname = userSurname.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(surname)){
            database.child("Ime_korisnika").setValue(name);
            database.child("Prezime_korisnika").setValue(surname);

            Intent moveToMap = new Intent(UserProfileActivity.this, MapActivity.class);
            moveToMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(moveToMap); // preusmjeravanje na MapActivity
        }else{
            Toast.makeText(UserProfileActivity.this, "Molim unesite svoje ime prezime i email", Toast.LENGTH_LONG).show();
        }
    }
}
