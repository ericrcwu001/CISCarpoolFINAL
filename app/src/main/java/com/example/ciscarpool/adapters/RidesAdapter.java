package com.example.ciscarpool.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciscarpool.R;
import com.example.ciscarpool.classes.Vehicle;
import com.example.ciscarpool.fragments.VehicleSchoolMapsFragment;
import com.example.ciscarpool.helpers.GPSHelper;
import com.example.ciscarpool.interfaces.VehicleOwnerNameCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class RidesAdapter extends
        RecyclerView.Adapter<RidesAdapter.ViewHolder> {
    private FirebaseFirestore firestore;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private Activity activity;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        public TextView carModel, typeOfVehicle, ownedBy, distanceFromUser, basePrice, capacity;
        public MaterialButton reserveRideBtn, viewMoreBtn;
        public ConstraintLayout reservationsTemplate;
        public ImageView carImg;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            carModel = (TextView) itemView.findViewById(R.id.carModel);
            typeOfVehicle = (TextView) itemView.findViewById(R.id.typeOfVehicle);
            ownedBy = (TextView) itemView.findViewById(R.id.ownedBy);
            distanceFromUser = (TextView) itemView.findViewById(R.id.distanceFromUser);
            basePrice = (TextView) itemView.findViewById(R.id.basePrice);
            capacity = (TextView) itemView.findViewById(R.id.capacity);
            reserveRideBtn = (MaterialButton) itemView.findViewById(R.id.reserveRideBtn);
            viewMoreBtn = (MaterialButton) itemView.findViewById(R.id.viewMoreBtn);
            reservationsTemplate = (ConstraintLayout) itemView.findViewById(R.id.reservationsTemplate);
        }
    }

    // Store a member variable for the contacts
    private List<Vehicle> mVehicles;

    // Pass in the contact array into the constructor
    public RidesAdapter(List<Vehicle> contacts, Activity activity, FragmentManager parentFragmentManager) {
        mVehicles = contacts;
        mAuth = FirebaseAuth.getInstance();
        fragmentManager = parentFragmentManager;
        this.activity = activity;
    }

    @Override
    public RidesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.reservation_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RidesAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Vehicle vehicle = mVehicles.get(position);

        // Set item views based on your views and data model
        TextView tv = holder.carModel;
        tv.setText(vehicle.getModel());

        tv = holder.basePrice;
        tv.setText(String.format(Locale.ENGLISH, "%,.2f", vehicle.getBasePrice()));

        tv = holder.capacity;
        tv.setText((CharSequence) (String.valueOf(vehicle.getCapacity()) + " left"));

        tv = holder.typeOfVehicle;
//        System.out.println(vehicle.getKiloCarbonSavedPerRidePerKm());
        double carbonSaved = vehicle.getKiloCarbonSavedPerRidePerKm();
        if (carbonSaved == 0.192) { // Fuel Vehicle
            tv.setText((CharSequence) "Fuel Vehicle");
        } else if (carbonSaved == 0.262) { // Hybrid Vehicle
            tv.setText((CharSequence) "Hybrid Vehicle");
        } else { // Electric Vehicle
            tv.setText((CharSequence) "Electric Vehicle");
        }

        tv = holder.ownedBy;
        TextView finalTv = tv;
        getVehicleOwnerName(new VehicleOwnerNameCallback() {
            @Override
            public void vehicleOwnerNameCallback(String fullName) {
                CharSequence cs = (CharSequence) ("Owned by " + fullName);
                finalTv.setText(cs);
            }
        }, vehicle.getOwner());

        tv = holder.distanceFromUser;
        LatLng carLoc = new LatLng(vehicle.getLocation().get(0), vehicle.getLocation().get(1));
        GPSHelper gpsHelper = new GPSHelper(activity);
        gpsHelper.getMyLocation();
        LatLng myLoc = gpsHelper.getLatLng();
        double dist = distance(carLoc, myLoc);
        String displayStr = String.format(Locale.ENGLISH, "%,.2f", dist) + "km";
        tv.setText(displayStr);

        MaterialButton mb = holder.reserveRideBtn;
        mb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = firestore.collection("users")
                        .document(mAuth.getUid());
                docRef.update("reservedRides", FieldValue.arrayUnion(vehicle.getVehicleID()));

                docRef = firestore.collection("vehicles")
                        .document(vehicle.getVehicleID());
                docRef.update("ridersUIDs", FieldValue.arrayUnion(mAuth.getUid()));
                holder.reservationsTemplate.setVisibility(View.GONE);
            }
        });

        mb = holder.viewMoreBtn;
        mb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = fragmentManager;
                Fragment frag = new VehicleSchoolMapsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("vehicle", vehicle.getVehicleID());
                bundle.putBoolean("viewMore", true);
//                bundle.putString("user", mAuth.getCurrentUser().getUid().toString());
//                bundle.putBoolean("yourVehicles", true);
                frag.setArguments(bundle);
                fm.beginTransaction()
                        .replace(R.id.flFragment, frag)
                        .commit();
            }
        });
    }

    private void getVehicleOwnerName(final VehicleOwnerNameCallback callback, String owner) {
        firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("users")
                .document(owner);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                    callback.vehicleOwnerNameCallback(fullName);
                }
            }
        });
    }

    private double distance(LatLng latLng1, LatLng latLng2) {

        double lat1 = latLng1.latitude;
        double lat2 = latLng2.latitude;
        double lon1 = latLng1.longitude;
        double lon2 = latLng2.longitude;

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c ; // final in km

        return distance;
    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mVehicles.size();
    }
}