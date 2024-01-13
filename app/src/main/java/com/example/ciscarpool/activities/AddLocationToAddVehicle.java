package com.example.ciscarpool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ciscarpool.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class AddLocationToAddVehicle extends AppCompatActivity implements OnMapReadyCallback {
    final private int padding = dpToPx(75); // default padding to offset from edges of the map in pixels
    private MapView mapView;
    private SearchView searchView;
    private GoogleMap map;
    private final LatLng HKLowerLeftLatLng = new LatLng(22.181904, 113.823104);
    private final LatLng HKUpperRightLatLng = new LatLng(22.581798, 114.405529);
    private final LatLng CISLatLng = new LatLng(22.28370058114513, 114.19787328287362);
    private LatLng selectedLatLng;
    private Bundle appBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_to_add_vehicle);
        hideStatusBar();
        ApplicationInfo app = null;
        try {
            app = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            appBundle = app.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        elementsSetUp();
        mapView.onCreate(savedInstanceState);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Address> addressList = null;
                if (query != null || !query.equals("")) {
                    String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                            + Uri.encode(query) + "&sensor=true&key=" +
                            appBundle.getString("com.google.android.geo.WEB_API_KEY");
                    RequestQueue queue = Volley.newRequestQueue(AddLocationToAddVehicle.this);
                    JsonObjectRequest stateReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            JSONObject location;
                            try {
                                // Get JSON Array called "results" and then get the 0th
                                // complete object as JSON
                                location = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                                // Get the value of the attribute whose name is
                                // "formatted_string"
                                if (location.getDouble("lat") != 0 && location.getDouble("lng") != 0) {
                                    selectedLatLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(selectedLatLng);
                                    map.clear();
                                    map.addMarker(new MarkerOptions().position(CISLatLng));
                                    map.addMarker(markerOptions);

                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    builder.include(selectedLatLng);
                                    builder.include(CISLatLng);
                                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
                                    map.moveCamera(cu);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();

                            }
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
                        }
                    });

                    queue.add(stateReq);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapView.getMapAsync(AddLocationToAddVehicle.this);
    }

    private void elementsSetUp() {
        mapView = (MapView) findViewById(R.id.mapView);
        searchView = (SearchView) findViewById(R.id.searchView);
    }


    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    public void confirmLocation(View view) {
        Intent data = new Intent();
        data.putExtra("lat", selectedLatLng.latitude);
        data.putExtra("lng", selectedLatLng.longitude);
        setResult(17, data);
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(CISLatLng);
        markerOptions.title("CIS");
        map.addMarker(markerOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(CISLatLng);
        builder.include(HKLowerLeftLatLng);
        builder.include(HKUpperRightLatLng);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 0);
        map.moveCamera(cu);
    }

    private int dpToPx(int dp) {
        int x = (int) (dp * ((float) Resources.getSystem().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return x;
    }
}