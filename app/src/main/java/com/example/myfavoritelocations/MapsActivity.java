package com.example.myfavoritelocations;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    TextView currentPositionTextView;
    Button addButton;
    Button backButton;

    Location favLocation;
    Marker favMarker;

    boolean favPlaceSelected;

    public void addFavToList(View view) {
        MainActivity.getLocations().add(favLocation);
        finish();
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);
    }

    public void goBack(View view) {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
        }

        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentPositionTextView = (TextView) findViewById(R.id.currentPositionTextView);
        addButton = (Button) findViewById(R.id.addButton);
        backButton = (Button) findViewById(R.id.backButton);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        favPlaceSelected = false;

        // getting the locationManager from system
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!favPlaceSelected) {
                    mMap.clear();
                    updateMarkerOnMap(location, "Me!", 17);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // asking for permission to get ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // if access granted getting GPS and setting it locationListener
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        // getting new fav pin for fav location
        Drawable favPin = getResources().getDrawable(R.drawable.fav_pin);
        final Bitmap favPinBitmap = ((BitmapDrawable) favPin).getBitmap();

        int passedLocationId = getIntent().getIntExtra("id", -1);
        if (passedLocationId != -1) {
            Location passedLoc = MainActivity.getLocations().get(passedLocationId);
            LatLng latLng = new LatLng(passedLoc.getLatitude(), passedLoc.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Favorite").icon(BitmapDescriptorFactory.fromBitmap(favPinBitmap)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            ConvertLocationToAddress address = new ConvertLocationToAddress(MapsActivity.this, passedLoc);
            address.run();
            currentPositionTextView.setText(address.getAddress());
            favPlaceSelected = true;
        } else {
            // adding the last know location to map
            Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            assert lastKnowLocation != null;
            updateMarkerOnMap(lastKnowLocation, "Me!", 17);
        }

        // if on long press - setting new favLocation
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                addButton.setVisibility(View.VISIBLE);
                if (favMarker != null) {
                    favMarker.remove();
                }

                favLocation = new Location("");
                favLocation.setLatitude(latLng.latitude);
                favLocation.setLongitude(latLng.longitude);

                // show text on map
                ConvertLocationToAddress address = new ConvertLocationToAddress(MapsActivity.this, favLocation);
                address.run();
                currentPositionTextView.setText(address.getAddress());
                favMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng).title("Favorite")
                        .icon(BitmapDescriptorFactory.fromBitmap(favPinBitmap)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                favPlaceSelected = true;
            }
        });
    }

    private void updateMarkerOnMap(Location location, String title, int zoom) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        ConvertLocationToAddress address = new ConvertLocationToAddress(MapsActivity.this, location);
        address.run();
        currentPositionTextView.setText(address.getAddress());
    }


}
