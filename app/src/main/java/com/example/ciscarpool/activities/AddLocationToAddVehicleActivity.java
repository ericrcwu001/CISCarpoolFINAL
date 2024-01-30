package com.example.ciscarpool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link AddLocationToAddVehicleActivity} is for users to confirm a location to associate with their vehicle.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class AddLocationToAddVehicleActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int padding = dpToPx(75); // default padding to offset from edges of the map in pixels
    private MapView mapView;
    private SearchView searchView;
    private GoogleMap map;
    private final LatLng HKLowerLeftLatLng = new LatLng(22.181904, 113.823104);
    private final LatLng HKUpperRightLatLng = new LatLng(22.581798, 114.405529);
    private final LatLng CISLatLng = new LatLng(22.28370058114513, 114.19787328287362);
    private LatLng selectedLatLng;
    private Bundle appBundle;

    /**
     * Start of the activity lifecycle. Setup + assigns click listeners to buttons
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_to_add_vehicle);

        hideStatusBar();
        elementsSetUp();

        mapView.onCreate(savedInstanceState);

        // What happens when text is entered to the SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // When query is submitted
                if (query != null || !query.equals("")) { // Preventing useless checking
                    // Establishing API Key for API Call
                    String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                            + Uri.encode(query) + "&sensor=true&key=" +
                            appBundle.getString("com.google.android.geo.WEB_API_KEY");

                    // Calling API using Volley
                    RequestQueue queue = Volley.newRequestQueue(AddLocationToAddVehicleActivity.this);
                    JsonObjectRequest stateReq = new JsonObjectRequest(Request.Method.GET, url,
                            null, response -> {
                                JSONObject location;
                                try {
                                    // Get JSON Array called "results" and then get the 0th
                                    // complete object as JSON
                                    location = response.getJSONArray("results")
                                            .getJSONObject(0).getJSONObject("geometry")
                                            .getJSONObject("location");

                                    // Get the value of the attribute whose name is "formatted_string"
                                    if (location.getDouble("lat") != 0 &&
                                            location.getDouble("lng") != 0) {
                                        selectedLatLng = new LatLng(location.getDouble("lat"),
                                                location.getDouble("lng"));
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(selectedLatLng);
                                        map.clear();
                                        map.addMarker(new MarkerOptions().position(CISLatLng));
                                        map.addMarker(markerOptions);

                                        // Position the map around the two markers.
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(selectedLatLng);
                                        builder.include(CISLatLng);
                                        CameraUpdate cu = CameraUpdateFactory
                                                .newLatLngBounds(builder.build(), padding);
                                        map.moveCamera(cu);
                                    }
                                } catch (JSONException e1) {
                                    Toast.makeText(AddLocationToAddVehicleActivity.this,
                                            "Something went wrong.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }, error -> {
                                Log.d("VolleyError.Response", error.toString());
                                Toast.makeText(AddLocationToAddVehicleActivity.this,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT).show();
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

        mapView.getMapAsync(AddLocationToAddVehicleActivity.this);
    }

    /**
     * Instantiates respective views' variables.
     */
    private void elementsSetUp() {
        mapView = (MapView) findViewById(R.id.mapView);
        searchView = (SearchView) findViewById(R.id.searchView);

        ApplicationInfo app = null;
        try {
            app = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            appBundle = app.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hide's the status bar on Android.
     * **/
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * When the user confirms their location selection. Finishes current activity and attaches
     * {@link LatLng} to activity result.
     * @param view passed by the onClick property of the Button View.
     */
    public void confirmLocation(View view) {
        Intent data = new Intent();
        data.putExtra("lat", selectedLatLng.latitude);
        data.putExtra("lng", selectedLatLng.longitude);
        setResult(17, data);
        finish();
    }

    /**
     * Initially positions the Map to Hong Kong. Adds a marker on CIS.
     * @param googleMap GoogleMap object. Passed by interface.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Creates CIS marker.
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(CISLatLng);
        markerOptions.title("CIS");
        map.addMarker(markerOptions);

        // Creates boundaries for Google Map
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(CISLatLng);
        builder.include(HKLowerLeftLatLng);
        builder.include(HKUpperRightLatLng);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 0);
        map.moveCamera(cu);
    }

    /**
     *
     * @param dp a dp value.
     * @return corresponding px value.
     */
    private int dpToPx(int dp) {
        int x = (int) (dp * ((float) Resources.getSystem().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return x;
    }
}