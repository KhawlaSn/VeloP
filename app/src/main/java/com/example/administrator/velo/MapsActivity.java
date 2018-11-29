package com.example.administrator.velo;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Double latitude;
    Double longitude;
    String address;
    String name;
    Integer number;
    /*  LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); */
    private static final String TAG = "MyLocation";
//    Button button = (Button)findViewById(R.id.button);

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private DatabaseReference mDatabase;
    Marker marker;
    Marker m;
    //vars
    private Boolean mLocationPermissionsGranted = false;
    ChildEventListener mChildEventListener;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //  HashMap<String, Marker> markers = new HashMap<>();
    List<UserLocation> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //setUpMap();
        //  new MarkerTask().execute();


        getLocationPermission();
        // setUpMap();
        mDatabase = FirebaseDatabase.getInstance().getReference("velo-223422").child("location");
        mDatabase.push().setValue(m);
        getMarkers();

    }
    public void button (View view) {
        Intent i = new Intent(this,EspaceVelo.class);
        startActivity(i);

    }

      /*  private void setUpMap(){
            // Retrieve the city data from the web service
            // In a worker thread since it's a network operation.
            new Thread(new Runnable() {
                public void run() {
                    try {
                        retrieveAndAddCities();
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot retrive cities", e);
                        return;
                    }
                }
            }).start();
        } */


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        //  DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        // Add a marker in Sydney and move the camera
        LatLng lyon = new LatLng(45.74846, 4.84671);
        mMap.addMarker(new MarkerOptions().position(lyon).title("Lyon").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lyon));
        LatLng amiens = new LatLng(49.894066, 2.295753);
        mMap.addMarker(new MarkerOptions().position(amiens).title("Amiens").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(amiens));
        LatLng cergypontoise = new LatLng(49.03894, 2.07805);
        mMap.addMarker(new MarkerOptions().position(cergypontoise).title("Cergy-Pontoise").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cergypontoise));
        LatLng creteil = new LatLng(48.7833, 2.4667);
        mMap.addMarker(new MarkerOptions().position(creteil).title("Creteil").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(creteil));
        LatLng marseille = new LatLng(43.296482, 5.369780);
        mMap.addMarker(new MarkerOptions().position(marseille).title("Marseille").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marseille));
        LatLng mulhouse = new LatLng(47.749531, 7.339750);
        mMap.addMarker(new MarkerOptions().position(mulhouse).title("Mulhouse").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mulhouse));
        LatLng nancy = new LatLng(36.871441, -94.863281);
        mMap.addMarker(new MarkerOptions().position(nancy).title("Nancy").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nancy));
        LatLng nantes = new LatLng(47.218372, -1.553621);
        mMap.addMarker(new MarkerOptions().position(nantes).title("Nantes").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nantes));
        LatLng rouen = new LatLng(49.443233,1.099971);
        mMap.addMarker(new MarkerOptions().position(rouen).title("Rouen").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rouen));
        LatLng toulouse = new LatLng(43.604652,1.444209);
        mMap.addMarker(new MarkerOptions().position(toulouse).title("Toulouse").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(toulouse));

        // googleMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        //  getMarkers();

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);


            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                }

            }
        }
    }

    @Override
    public void onStop() {
        if (mChildEventListener != null)
            mDatabase.removeEventListener(mChildEventListener);
        super.onStop();
    }

    private void getMarkers() {

        mChildEventListener = mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserLocation marker = dataSnapshot.getValue(UserLocation.class);
                String name = marker.getName();
                String address = marker.getAddress();
                Double latitude = marker.getLatitude();
                Double longitude = marker.getLongitude();
                Integer number = marker.getNumber();
                markers.add(marker);
                for (int i = 0; i < markers.size(); i++) {
                    LatLng location = new LatLng(latitude, longitude);

                    if (mMap != null) {
                        m = mMap.addMarker(new MarkerOptions().position(location).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        mMap.setMaxZoomPreference(20);
                    }
                }
                // markers.put(dataSnapshot.getKey(), m);

                // map.add(m);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //   markers.get(dataSnapshot.getKey()).remove();
                // markers.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
      /*  mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                                 @Override
                                                 public void onDataChange(DataSnapshot dataSnapshot) {
                                                     for (DataSnapshot child : dataSnapshot.child("location").getChildren()){
                                                         latitude = child.child("latitude").getValue(Double.class);
                                                         longitude = child.child("longitude").getValue(Double.class);
                                                         address = child.child("address").getValue(String.class);
                                                         name = child.child("name").getValue(String.class);
                                                         number = child.child("number").getValue(Integer.class);


                                                         LatLng latLng = new LatLng(latitude, longitude);
                                                         mMap.addMarker(new MarkerOptions()
                                                                 .position(latLng)
                                                                 .title(name)
                                                                 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                                         mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                                         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13f));
                                                     }



                                                 }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    } */
          /*  mDatabase.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    LatLng newLocation = new LatLng(
                            dataSnapshot.child("location").child("latitude").getValue(Long.class),
                            dataSnapshot.child("location").child("longitude").getValue(Long.class)
                    );
                    mMap.addMarker(new MarkerOptions()
                            .position(newLocation)
                            .title(dataSnapshot.getKey()));
                } */

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionsGranted = true;
            initMap();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}




