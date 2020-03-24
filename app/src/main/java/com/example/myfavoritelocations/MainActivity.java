package com.example.myfavoritelocations;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    private static List<Location> locations = new ArrayList<>();
    List<String> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        addresses = new ArrayList<>();
        addresses.add(0, "Add new location...");
        getAddresses();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addresses);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.i("clicked ---->" , String.valueOf(position));
                // passing locationId to map
                Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                if (position == 0) {
                    startActivity(mapIntent);
                } else {
                    mapIntent.putExtra("id", position-1);
                    startActivity(mapIntent);
                }
            }
        });
    }

    public List<String> getAddresses() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        for (Location location : locations) {
            try {
                List<Address> theAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (theAddress != null && theAddress.size() > 0) {
                    if (theAddress.get(0).getAdminArea() != null) {
                        addresses.add(theAddress.get(0).getAddressLine(0));
                    } else {
                        String noAddress =  "No Address: " + theAddress.get(0).getLatitude() +
                                " " +
                                theAddress.get(0).getLongitude();
                        addresses.add(noAddress);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<Location> getLocations() {
        return locations;
    }

    public static void setLocations(List<Location> locations) {
        MainActivity.locations = locations;
    }



}
