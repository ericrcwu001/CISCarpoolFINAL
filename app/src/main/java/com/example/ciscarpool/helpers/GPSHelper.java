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

/**
 * {@link GPSHelper} is a helper class to get the current location of the user.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class GPSHelper {
    private Activity activity;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;

    // Constructor
    public GPSHelper(Activity activity) {
        this.activity = activity;

        locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);

    }

    /**
     * Checks location permissions + gets the current location of the user.
     */
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

    /**
     * @return {@link LatLng} current location of user
     */
    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
