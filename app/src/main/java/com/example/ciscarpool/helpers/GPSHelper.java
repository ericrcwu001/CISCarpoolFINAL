package com.example.ciscarpool.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class GPSHelper {

    private Activity activity;
    // flag for GPS Status
    private boolean isGPSEnabled = false;
    // flag for network status
    private boolean isNetworkEnabled = false;
    private LocationManager locationManager;
    private Location location;
    private double latitude;
    private double longitude;

    public GPSHelper(Activity activity) {
        this.activity = activity;

        locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);

    }
    public void getMyLocation() {
        List<String> providers = locationManager.getProviders(true);

        Location l = null;
        for (int i = 0; i < providers.size(); i++) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                l = locationManager.getLastKnownLocation(providers.get(i));
            } else {
                ActivityCompat.requestPermissions((Activity) activity, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        1010);
            }

            if (l != null)
                break;

        }
        if (l != null) {
            latitude = l.getLatitude();
            longitude = l.getLongitude();
        }
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
