package com.example.myfavoritelocations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@SuppressLint("StaticFieldLeak")
public class ConvertLocationToAddress implements Runnable {

    private Context context;
    private Location location;
    private String address;

    public ConvertLocationToAddress(Context context, Location location) {
        this.context = context;
        this.location = location;
    }

    @Override
    public void run() {
        if (location != null) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> theAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (theAddress != null && theAddress.size() > 0) {
                    if (theAddress.get(0).getAdminArea() != null) {
                        address = theAddress.get(0).getAddressLine(0);
                    } else {
                        address = theAddress.get(0).getLatitude() + " / " + theAddress.get(0).getLongitude();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAddress() {
        return address;
    }
}
