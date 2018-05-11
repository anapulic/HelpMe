package com.example.ana.helpme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;

    EditText userName, userSurname, userEmail;
    ImageView userImage;
    LinearLayout save;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authlistener;

    DatabaseReference userDatabase;
    StorageReference storageRef;

    Uri imageHoldUri = null;

    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = (EditText) findViewById(R.id.userName);
        userSurname = (EditText) findViewById(R.id.userSurname);
        userEmail = (EditText) findViewById(R.id.userEmail);
        userImage = (ImageView) findViewById(R.id.userImage);
        save = (LinearLayout) findViewById(R.id.saveProfile);

        auth = FirebaseAuth.getInstance();
        authlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){
                    finish();
                    Intent moveToMap = new Intent(ProfileActivity.this, MapActivity.class);
                    moveToMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(moveToMap);
                }
            }
        };

        progress = new ProgressDialog(this);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
        storageRef = FirebaseStorage.getInstance().getReference();

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                saveUserProfile();
            }
        });

        userImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                profilePicSelection();
            }
        });
    }

    private void saveUserProfile(){
        final String username, usersurname, useremail;

        username = userName.getText().toString().trim();
        usersurname = userSurname.getText().toString().trim();
        useremail = userEmail.getText().toString().trim();

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(usersurname) && !TextUtils.isEmpty(useremail)){
            if (imageHoldUri != null){
                progress.setTitle("Spremam profil");
                progress.setMessage("Molim pričekajte");
                progress.show();

                StorageReference childStorage = storageRef.child("Korisnicki_profil").child(imageHoldUri.getLastPathSegment());
                final String profilePicUrl = imageHoldUri.getLastPathSegment();

                childStorage.putFile(imageHoldUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri imageUrl = taskSnapshot.getDownloadUrl();

                        userDatabase.child("userName").setValue(username);
                        userDatabase.child("userSurname").setValue(usersurname);
                        userDatabase.child("userEmail").setValue(userEmail);
                        userDatabase.child("userID").setValue(auth.getCurrentUser().getUid());
                        userDatabase.child("imageURL").setValue(imageUrl.toString());

                        progress.dismiss();

                        finish();

                        Intent moveToMap = new Intent(ProfileActivity.this, MapActivity.class);
                        moveToMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(moveToMap);
                    }
                });
            }else{
                Toast.makeText(ProfileActivity.this, "Molim izaberite sliku profila.", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(ProfileActivity.this, "Molim unesite svoje ime prezime i email", Toast.LENGTH_LONG).show();
        }
    }

    private void profilePicSelection(){
        final CharSequence[] items = {"Snimite fotografiju", "Uvezite s računala", "Poništi"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Dodaj fotografiju!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Snimite fotografiju")){
                    cameraIntent();
                }else if (items[item].equals("Uvezite s računala")){
                    galleryIntent();
                }else if (items[item].equals("Poništi")){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent(){
        Log.d("gola", "entered here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent(){
        Log.d("gola", "entered here");
        Intent intent = new Intent (Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_FILE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }else if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                imageHoldUri = result.getUri();

                userImage.setImageURI(imageHoldUri);
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
            }
        }
    }
}