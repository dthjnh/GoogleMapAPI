package com.example.googleapi;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.googleapi.databinding.ActivityMapsBinding;

import kotlin.Suppress;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSION_REQUEST_CODE = 99;
    private static final int UPDATE_INTERVAL = 10*1000 ;
    private static final int FASTEST_INTERVAL = 2*1000;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    protected FusedLocationProviderClient client;
    protected LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
        mMap.addMarker(new MarkerOptions().position(rmit).title("Marker in RMIT"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rmit));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rmit, 15));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker()));
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
    public void getPosition(View view){
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
                onLocationChanged(locationResult.getLastLocation());
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
}