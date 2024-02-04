package com.example.ciscarpool.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.ciscarpool.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Objects;

/**
 * {@link VehicleSchoolMapsFragment} is a {@link Fragment} that shows CIS and the vehicle position.
 * on a single Google Map.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class VehicleSchoolMapsFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private MaterialButton unreserve, backBtnVehicleSchoolsMaps, closeOpenVehicle;
    private GoogleMap map;
    private String vehicleUID, userUID;
    private boolean yourReservations, yourVehicles, viewMore;
    private FirebaseFirestore firestore;
    private LatLng vehicleLatLng;

    // Required empty public constructor
    public VehicleSchoolMapsFragment() {}

    /**
     * Start of the fragment lifecycle.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Instantiates elements + assigns click listeners to buttons.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vehicle_school_maps, container, false);

        // Initialize map fragment
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        backBtnVehicleSchoolsMaps = view.findViewById(R.id.backBtnVehicleSchoolsMaps);

        // Getting arguments
        vehicleUID = getArguments().getString("vehicle");
        userUID = getArguments().getString("user");
        yourReservations = getArguments().getBoolean("yourReservations");
        yourVehicles = getArguments().getBoolean("yourVehicles");
        viewMore = getArguments().getBoolean("viewMore");

        // Initialize necessary buttons
        unreserve = view.findViewById(R.id.unReserveVehicle);
        closeOpenVehicle = view.findViewById(R.id.closeOpenVehicle);

        if (Objects.nonNull(yourReservations) && yourReservations) {
            unreserve.setVisibility(View.VISIBLE);
            unreserve.setOnClickListener(v -> {
                DocumentReference docRef = firestore.collection("users")
                        .document(userUID);
                docRef.update("reservedRides", FieldValue.arrayRemove(vehicleUID));

                docRef = firestore.collection("vehicles")
                        .document(vehicleUID);
                docRef.update("ridersUIDs", FieldValue.arrayRemove(userUID));

                FragmentManager fm = getParentFragmentManager();
                Fragment frag = new PersonFragment();
                fm.beginTransaction()
                        .replace(R.id.flFragment, frag)
                        .commit();
            });

            backBtnVehicleSchoolsMaps.setOnClickListener(v -> {
                FragmentManager fm = getParentFragmentManager();
                Fragment frag = new PersonFragment();
                fm.beginTransaction()
                        .replace(R.id.flFragment, frag)
                        .commit();
            });
        } else if (Objects.nonNull(yourVehicles) && yourVehicles) {
            closeOpenVehicle.setVisibility(View.VISIBLE);
            closeOpenVehicle.setOnClickListener(v -> {
                DocumentReference docRef = firestore.collection("vehicles")
                        .document(vehicleUID);
                docRef.get().addOnSuccessListener(documentSnapshot ->
                        docRef.update("open",
                                Boolean.FALSE.equals(documentSnapshot.getBoolean("open"))));


                FragmentManager fm = getParentFragmentManager();
                Fragment frag = new PersonFragment();
                fm.beginTransaction()
                        .replace(R.id.flFragment, frag)
                        .commit();
            });

            backBtnVehicleSchoolsMaps.setOnClickListener(v -> {
                FragmentManager fm = getParentFragmentManager();
                Fragment frag = new PersonFragment();
                fm.beginTransaction()
                        .replace(R.id.flFragment, frag)
                        .commit();
            });
        } else if (Objects.nonNull(viewMore) && viewMore) {
            backBtnVehicleSchoolsMaps.setOnClickListener(v -> {
                FragmentManager fm = getParentFragmentManager();
                Fragment frag = new CarpoolFragment();
                fm.beginTransaction()
                        .replace(R.id.flFragment, frag)
                        .commit();
            });
        }

        mapView.getMapAsync(this);
        return view;

    }

    /**
     * Reformats MaterialButton based on it's closed/open status.
     * @param closeOpenVehicle affected MaterialButton
     */
    private void changeCloseOpenBtn(MaterialButton closeOpenVehicle) {
        DocumentReference docRef = firestore.collection("vehicles")
                .document(vehicleUID);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            boolean isOpen = Boolean.TRUE.equals(documentSnapshot.getBoolean("open"));
            if (isOpen) {
                closeOpenVehicle.setBackgroundColor(getResources().getColor(R.color.mainColour));
                closeOpenVehicle.setText((CharSequence) "Close Vehicle");
                closeOpenVehicle.setTextColor(getResources().getColor(R.color.white));
                closeOpenVehicle.setStrokeWidth(0);
            } else {
                closeOpenVehicle.setBackgroundColor(getResources().getColor(R.color.white));
                closeOpenVehicle.setText((CharSequence) "Open Vehicle");
                closeOpenVehicle.setTextColor(getResources().getColor(R.color.mainColour));
                closeOpenVehicle.setStrokeWidth(dpToPx(2));
            }
        });


    }

    /**
     * Positions the map around CIS and the vehicle.
     * @param googleMap GoogleMap object. Passed by interface.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        firestore = FirebaseFirestore.getInstance();
        changeCloseOpenBtn(closeOpenVehicle);

        firestore.collection("vehicles")
                .document(vehicleUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        ArrayList<Double> location = (ArrayList<Double>) document.get("location");
                        vehicleLatLng = new LatLng(location.get(0), location.get(1));

                        map = googleMap;
                        map.getUiSettings().setMyLocationButtonEnabled(false);

                        // Checks user permission
                        if (ActivityCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED) {
                            map.setMyLocationEnabled(true);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new String[] {
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION },
                                    1010);
                        }

                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng CISLatLng = new LatLng(22.28370058114513, 114.19787328287362);
                        markerOptions.position(CISLatLng);
                        markerOptions.title("CIS");
                        map.addMarker(markerOptions);

                        markerOptions.position(vehicleLatLng);
                        markerOptions.title(document.getString("model"));
                        map.addMarker(markerOptions);

                        // Creates boundaries for Google Map
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(CISLatLng);
                        builder.include(vehicleLatLng);
                        LatLngBounds bounds = builder.build();

                        int padding = dpToPx(75); // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        map.moveCamera(cu);
                    } else {
                        Log.d(VehicleSchoolMapsFragment.class.toString(), "No such document");
                        Toast.makeText(getActivity(), "Internal error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     *
     * @param dp a dp value.
     * @return corresponding px value.
     */
    private int dpToPx(int dp) {
        int x = (int) (dp * ((float) getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return x;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}


