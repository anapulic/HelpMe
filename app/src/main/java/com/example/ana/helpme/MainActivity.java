package com.example.ana.helpme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance(); // instanca za firebase autentikaciju

        //dohvacanje korisnickog unosa i listeneri za gumbove
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        findViewById(R.id.textViewSignup).setOnClickListener(this);
        findViewById(R.id.buttonLogin).setOnClickListener(this);

    }


    // Metoda userLogin - prijava korisnika
    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if (email.isEmpty()) {
            editTextEmail.setError("Email je obavezan!");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Unesite ispravan email!");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Lozinka je obavezna!");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Minimalna duljina lozinke je 6 znakova!");
            editTextPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        // Firebase autentikacija:
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent); // preusmjeri na MapActivity
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {  // provjera prijavljenog korisnika
            finish();
            startActivity(new Intent(this, MapActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textViewSignup:   // u slucaju klika na signup otvara SignupActivity
                finish();
                startActivity(new Intent(this, SignUpActivity.class));
                break;
            case R.id.buttonLogin:    // u slucaju klika na login poziva metodu userLogin
                userLogin();
                break;
        }
    }
}

