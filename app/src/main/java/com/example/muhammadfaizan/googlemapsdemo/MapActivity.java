package com.example.muhammadfaizan.googlemapsdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    boolean permission_granted = false;
    private static int PERMISSION_REQUEST_CODE = 06;
    private GoogleMap mMap;
    private String FINE_LOCATION_REQUEST = Manifest.permission.ACCESS_FINE_LOCATION;
    private String COURSE_LOCATION_REQUEST = Manifest.permission.ACCESS_COARSE_LOCATION;
    private String[] mPermission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private FusedLocationProviderClient mlProvider;
    private static float ZOOM = 15f;
    private EditText edtSearch;
    private ImageView imgSearch;
    private ImageView imgMyLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        initViews();
        permissionCheck();
    }

    private void permissionCheck() {
        if (ContextCompat.checkSelfPermission(MapActivity.this, FINE_LOCATION_REQUEST) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(MapActivity.this, COURSE_LOCATION_REQUEST) == PackageManager.PERMISSION_GRANTED) {
                permission_granted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(MapActivity.this, mPermission, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(MapActivity.this, mPermission, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 06: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            permission_granted = false;
                            Toast.makeText(this, "Permission not granted due to some reasons!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                permission_granted = true;

                //Implement method to initialize the Map
                initMap();
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
        mlProvider = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());
        getDeviceLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(this, "Map ready", Toast.LENGTH_LONG).show();
        goTOLocation();
    }

    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task task = mlProvider.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    final Location mLocation = task.getResult();
                    moveCam(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), ZOOM);
                    imgMyLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            moveCam(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), ZOOM);
                        }
                    });
                } else {
                    Toast.makeText(MapActivity.this, "Could not get device's location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void moveCam(LatLng latlng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void goTOLocation() {
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtSearch.getText().toString().trim().equals("") || edtSearch.getText().toString().trim().length() == 0) {
                    Toast.makeText(MapActivity.this, "Please enter a name to search", Toast.LENGTH_LONG).show();
                } else {
                    String location = edtSearch.getText().toString().trim();
                    Geocoder geocoder = new Geocoder(MapActivity.this);
                    List<Address> locationList = new ArrayList<>();
                    edtSearch.setText("");
                    edtSearch.setHint("Enter city, business or zip");

                    try {
                        locationList = geocoder.getFromLocationName(location, 1);
                        if (locationList.size() > 0) {
                            Address address = locationList.get(0);
                            Log.e("faizan", address.toString());
                            moveCam(new LatLng(address.getLatitude(), address.getLongitude()), ZOOM, address.getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtLoicationName);
        imgSearch = findViewById(R.id.imgSearch);
        imgMyLocation = findViewById(R.id.imgMyLocation);
    }

    private void moveCam(LatLng latlng, float zoom, String title) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        MarkerOptions markerOptions = new MarkerOptions().position(latlng).title(title);
        mMap.addMarker(markerOptions);
    }
}