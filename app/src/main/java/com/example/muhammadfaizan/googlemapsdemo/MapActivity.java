package com.example.muhammadfaizan.googlemapsdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private AutoCompleteTextView edtSearch;
    private ImageView imgSearch;
    private ImageView imgMyLocation;
    private ImageView imgHospital;
    private ImageView imgRestaurant;
    private ImageView imgLocalAtm;
    private ImageView imgClearMap;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private GeoDataClient geoDataClient;
    private LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    private LatLng myPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        initViews();
        permissionCheck();
        geoDataClient = Places.getGeoDataClient(MapActivity.this, null);
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(MapActivity.this, geoDataClient, latLngBounds, null);
        edtSearch.setAdapter(placeAutocompleteAdapter);
        setNearbyPlacesListeners();
        goToMyLocation();
        setClearButtonListener();
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
        goToLocation();
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
                    myPosition = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    moveCam(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), ZOOM);
//                    imgMyLocation.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            moveCam(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), ZOOM);
//                        }
//                    });
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

    private void goToLocation() {
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
        imgHospital = findViewById(R.id.img_hospital);
        imgRestaurant = findViewById(R.id.img_restaurant);
        imgLocalAtm = findViewById(R.id.img_local_atm);
        imgClearMap = findViewById(R.id.img_clear_map);
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

    private void setNearbyPlacesListeners() {
        imgRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getNearbyPlaces("restaurant");
            }
        });

        imgHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getNearbyPlaces("hospital");
            }
        });

        imgLocalAtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getNearbyPlaces("atm");
            }
        });
    }

    private void goToMyLocation() {
        imgMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });
    }

    private void setNearbyMarkers(LatLng latLng, String info) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12f));
        MarkerOptions markerOptions = new MarkerOptions().title(info).position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void getNearbyPlaces(String title) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        url.append("location=" + myPosition.latitude + "," + myPosition.longitude);
        url.append("&radius=" + 6000);
        url.append("&type=" + title);
        url.append("&key=AIzaSyBSex7To4yqpwbR0cqV1i4N0cmRlcMWAls");


        RequestQueue requestQueue = Volley.newRequestQueue(MapActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
                        String place_name = String.valueOf(jsonArray.getJSONObject(i).get("name"));
                        String place_vicinity = String.valueOf(jsonArray.getJSONObject(i).get("vicinity"));
                        setNearbyMarkers(new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng")), place_name + ", " + place_vicinity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("response", error.toString());
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void setClearButtonListener() {
        imgClearMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                moveCam(myPosition, ZOOM);
            }
        });
    }
}