package com.example.geofenceapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.geofenceapp.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private ActivityMapsBinding binding;
    private int FINE_LOCATION_REQUEST_ACCESS_CODE = 10001;
    private int BACKGROUND_LOCATION_REQUEST_ACCESS_CODE = 10005;
    private GeofenceHelper geofenceHelper;
    private float GEOFENCE_RADIUS = 200;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);

        geofenceHelper = new GeofenceHelper(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in pathankot and move the camera

        LatLng ptkFerozepur = new LatLng(32.33510915694546, 75.5803605484925);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ptkFerozepur));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ptkFerozepur, 16f));


        enableUserLocation();

        mMap.setOnMapLongClickListener(this);
    }

    private void enableUserLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        } else {
            //ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.
                    permission.ACCESS_FINE_LOCATION)) {
                //We need to show user why the permission is needed and then ask for the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_LOCATION_REQUEST_ACCESS_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_LOCATION_REQUEST_ACCESS_CODE);

            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_LOCATION_REQUEST_ACCESS_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // We have the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                // We do not have the permission
            }

        }

        if (requestCode == BACKGROUND_LOCATION_REQUEST_ACCESS_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();

            } else {
                // We do not have the permission
                Toast.makeText(this, "Background location access is necessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            
            }

        }


    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

        if ((Build.VERSION.SDK_INT >= 29)){
            //Application have to ask for background permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            handleMapLongClick(latLng);
        } else {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                //Dialog is shown to ask for permission


                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_REQUEST_ACCESS_CODE);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_REQUEST_ACCESS_CODE);

            }
        }

        }  else {
           handleMapLongClick(latLng);
        }
    }

    private void handleMapLongClick(LatLng latLng) {
        mMap.clear();
        addMarker(latLng);
        addCricle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);

    }
    private void addGeofence(LatLng latLng, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = (PendingIntent) geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG, "onSuccess: Geofence Added");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        String errorMessage = GeofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);

                    }
                });
    }

    private void addMarker(LatLng latLng){

        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);

    }

    private void addCricle(LatLng latLng, float radius) {

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255,255,0,0));
        circleOptions.fillColor(Color.argb(64,255,0,0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);


    }

}



