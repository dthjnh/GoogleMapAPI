package com.example.googleapi;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.googleapi.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import kotlin.Suppress;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSION_REQUEST_CODE = 99;
    private static final int UPDATE_INTERVAL = 10*1000 ;
    private static final int FASTEST_INTERVAL = 2*1000;
    private GoogleMap mMap;
    private SearchView mapSearch;
    private ActivityMapsBinding binding;
    private Circle currentCircle;
    private Button removeCircleButton;
    private Marker currentMarker;

    protected FusedLocationProviderClient client;
    protected LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapSearch = findViewById(R.id.mapSearch);
        removeCircleButton = findViewById(R.id.removeCircleButton);

        removeCircleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCircle();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mapSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearch.getQuery().toString();
                List<Address> addressList = null;
                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);
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
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng rmit = new LatLng(10.73, 106.69);
        mMap.addMarker(new MarkerOptions().position(rmit).title("Marker in RMIT"));//add marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rmit));//move camera to the marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rmit, 15));//zoom level 1-20
        mMap.getUiSettings().setZoomControlsEnabled(true);//zoom in zoom out



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker()));
                drawCircle(latLng);
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapsActivity.this, marker.getPosition().latitude + " " + marker.getPosition().longitude, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        startLocationUpdate();

    }

    private void requestPermission() {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_CODE);
        }
    public void getPosition(View view) {
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
        client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String message = "Last Location: " + Double.toString(location.getLongitude()) + ", " + Double.toString(location.getLongitude());
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Last Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Suppress(names = "MissingPermission")
    private void startLocationUpdate() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        client.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                //onLocationChanged(locationResult.getLastLocation());
            }
        },null);
    }

    private void onLocationChanged(Location lastLocation) {
        String message = "Updated Location: " + Double.toString(lastLocation.getLongitude()) + ", " + Double.toString(lastLocation.getLongitude());
        LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void drawCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions().center(latLng).radius(1000).fillColor(Color.RED).strokeColor(Color.BLACK).strokeWidth(2);
        currentCircle = mMap.addCircle(circleOptions);
        Toast.makeText(this, "Circle drawn successfully", Toast.LENGTH_SHORT).show();

        fetchRestaurants(latLng, 1000);
    }

    private void removeCircle() {
        if (currentCircle != null) {
            currentCircle.remove();
            currentCircle = null;
            Toast.makeText(this, "Circle removed successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No circle to remove", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchRestaurants(LatLng center, double radius) {
        String apiKey = "AIzaSyAmYG0ewlmb4zaJAkC6pBsFjqi0NBQu-Po";
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                center.latitude + "," + center.longitude + "&radius=" + radius +
                "&type=restaurant&key=" + apiKey;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API Response", response.toString());
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject restaurant = results.getJSONObject(i);
                                String name = restaurant.getString("name");
                                double rating = restaurant.has("rating") ? restaurant.getDouble("rating") : 0.0;
                                double latitude = restaurant.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                double longitude = restaurant.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                                // Store the restaurant information in the database
                                storeRestaurant(name, rating, latitude, longitude);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void storeRestaurant(String name, double rating, double latitude, double longitude) {
        DatabaseHelper dbHelper = new DatabaseHelper(MapsActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_RATING, rating);
        values.put(DatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(DatabaseHelper.COLUMN_LONGITUDE, longitude);

        db.insert(DatabaseHelper.TABLE_RESTAURANTS, null, values);
        db.close();
    }


}