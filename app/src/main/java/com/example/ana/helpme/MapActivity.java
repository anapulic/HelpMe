package com.example.ana.helpme;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private Boolean locationPermissionsGranted = false;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    Button btnHelp;
    Button btnLogOut;
    public static TextView usersInDanger;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnHelp = (Button) findViewById(R.id.btnHelp);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);
        usersInDanger = (TextView) findViewById(R.id.usersInDanger);
        usersInDanger.bringToFront();

        getLocationPermission();



        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClickBtnHelp: Gumb za pomoć dodirnut!");
                getDeviceLocation();
            }
        });

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClickBtnLogOut: Gumb za logout dodirnut!");
                logout();
            }
        });


    }



    ///////////////////////// METODA ZA DOHVAĆANJE LOKACIJE UREĐAJA: //////////////////////////////

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: dohvaćam trenutnu lokaciju uređaja");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            Log.d(TAG, "onComplete: pronašao sam lokaciju. " +
                                    "Lat:" + currentLocation.getLatitude() + "    " +
                                    "Lng: " + currentLocation.getLongitude());
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM);

                        } else {
                            Log.d(TAG, "onComplete: ne mogu pronaći trenutnu lokaciju");
                            Toast.makeText(MapActivity.this,
                                    "Dohvaćanje trenutne lokacije nije moguće",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }

    }




    /////////////////////////// METODA ZA ZUMIRANJE ///////////////////////////////////////////////

    private void moveCamera(LatLng latlng, float zoom) {
        Log.d(TAG, "moveCamera: zumiram na latituda: "
                + latlng.latitude + ", longituda: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }





    /////////////////////////// INICIJALIZACIJA MAPE //////////////////////////////////////////////

    private void initMap() {
        Log.d(TAG, "initMap: Inicijaliziram mapu");
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }







    ///////////////////////// DOPUŠTENJA LOKACIJE /////////////////////////////////////////////////

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: dohvaćam dozvole lokacije");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }




    /////////////////////// PROVJERA DOPUŠTENJA ///////////////////////////////////////////////////

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: metoda je pozvana");
        locationPermissionsGranted = false;
        int i;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: " +
                                    "Nemam dozvolu korištenja lokacije uređaja!");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: dopuštenja su odobrena");
                    locationPermissionsGranted = true;
                    // inicijaliziraj mapu
                    initMap();
                }
            }
        }
    }



    ////////////////////////////////////////// PRIKAZ KARTE //////////////////////////////////////

   @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Mapa je spremna", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Mapa je spremna");
        mMap = googleMap;

        if (locationPermissionsGranted) {
            //getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            getUsersInDanger();
        }
    }




    /////////////////////////////////// ODJAVA ////////////////////////////////////////////////////

    private void logout(){
        auth = FirebaseAuth.getInstance();
        auth.signOut();
        startActivity(new Intent(this, MainActivity.class));
    }





    ///////////////////////////// DOHVATI LOKACIJE S REST API-ja //////////////////////////////////

    private void getUsersInDanger(){
        fetchData process = new fetchData();
        process.execute();
    }

}
