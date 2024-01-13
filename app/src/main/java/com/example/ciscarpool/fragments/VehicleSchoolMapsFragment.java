package com.example.ciscarpool.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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

import com.example.ciscarpool.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Objects;

public class VehicleSchoolMapsFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private MaterialButton unreserve, backBtnVehicleSchoolsMaps, closeOpenVehicle;
    private GoogleMap map;
//    private FusedLocationProviderClient fusedLocationClient;
    private String vehicleUID, userUID;
    private boolean yourReservations, yourVehicles, viewMore;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private LatLng vehicleLatLng;


    public VehicleSchoolMapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vehicle_school_maps, container, false);

        // Initialize map fragment
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        backBtnVehicleSchoolsMaps = (MaterialButton) view.findViewById(R.id.backBtnVehicleSchoolsMaps);
        mAuth = FirebaseAuth.getInstance();

        // Getting arguments
        vehicleUID = getArguments().getString("vehicle");
        userUID = getArguments().getString("user");
        yourReservations = getArguments().getBoolean("yourReservations");
        yourVehicles = getArguments().getBoolean("yourVehicles");
        viewMore = getArguments().getBoolean("viewMore");

        // Initialize necessary buttons
        unreserve = (MaterialButton) view.findViewById(R.id.unReserveVehicle);
        closeOpenVehicle = (MaterialButton) view.findViewById(R.id.closeOpenVehicle);

        if (Objects.nonNull(yourReservations) && yourReservations) {
            unreserve.setVisibility(View.VISIBLE);
            unreserve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });

            backBtnVehicleSchoolsMaps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getParentFragmentManager();
                    Fragment frag = new PersonFragment();
                    fm.beginTransaction()
                            .replace(R.id.flFragment, frag)
                            .commit();
                }
            });
        } else if (Objects.nonNull(yourVehicles) && yourVehicles) {
            closeOpenVehicle.setVisibility(View.VISIBLE);
            closeOpenVehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DocumentReference docRef = firestore.collection("vehicles")
                            .document(vehicleUID);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            docRef.update("open", Boolean.FALSE.equals(documentSnapshot.getBoolean("open")));
                        }
                    });

//                    changeCloseOpenBtn(closeOpenVehicle);

                    FragmentManager fm = getParentFragmentManager();
                    Fragment frag = new PersonFragment();
                    fm.beginTransaction()
                            .replace(R.id.flFragment, frag)
                            .commit();
                }
            });

            backBtnVehicleSchoolsMaps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getParentFragmentManager();
                    Fragment frag = new PersonFragment();
                    fm.beginTransaction()
                            .replace(R.id.flFragment, frag)
                            .commit();
                }
            });
        } else if (Objects.nonNull(viewMore) && viewMore) {
            backBtnVehicleSchoolsMaps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getParentFragmentManager();
                    Fragment frag = new CarpoolFragment();
                    fm.beginTransaction()
                            .replace(R.id.flFragment, frag)
                            .commit();
                }
            });
        }

        mapView.getMapAsync(this);
        return view;

    }

    private void changeCloseOpenBtn(MaterialButton closeOpenVehicle) {
        DocumentReference docRef = firestore.collection("vehicles")
                .document(vehicleUID);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
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
            }
        });


    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        firestore = FirebaseFirestore.getInstance();
        changeCloseOpenBtn(closeOpenVehicle);

        firestore.collection("vehicles")
                .document(vehicleUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            ArrayList<Double> location = (ArrayList<Double>) document.get("location");
                            vehicleLatLng = new LatLng(location.get(0), location.get(1));

                            map = googleMap;
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                            PackageManager.PERMISSION_GRANTED) {
                                map.setMyLocationEnabled(true);
                            } else {
                                ActivityCompat.requestPermissions(getActivity(), new String[] {
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION },
                                        1010);
                            }

//                            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng CISLatLng = new LatLng(22.28370058114513, 114.19787328287362);
                            markerOptions.position(CISLatLng);
                            markerOptions.title("CIS");
                            map.addMarker(markerOptions);


                            markerOptions.position(vehicleLatLng);
                            markerOptions.title(document.getString("model"));
                            map.addMarker(markerOptions);

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(CISLatLng);
                            builder.include(vehicleLatLng);
                            LatLngBounds bounds = builder.build();

                            int padding = dpToPx(75); // offset from edges of the map in pixels
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                            map.moveCamera(cu);

                        } else {
                            Log.d("VEHICLE SCHOOLS MAP FRAGMENT", "Error getting documents: ", task.getException());
                        }
                    }

                });
    }

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


