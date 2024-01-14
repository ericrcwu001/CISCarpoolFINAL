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

/**
 * {@link RidesAdapter} extends {@link RecyclerView.Adapter} to format data from Firebase
 * Firestore into a {@link RecyclerView} for {@link com.example.ciscarpool.fragments.CarpoolFragment}
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class RidesAdapter extends
        RecyclerView.Adapter<RidesAdapter.ViewHolder> {
    private List<Vehicle> mVehicles;
    private FirebaseFirestore firestore;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private Activity activity;

    /**
     * {@link RidesAdapter.ViewHolder} extends {@link RecyclerView.ViewHolder} to instantiate
     * each individual element in the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView carModel, typeOfVehicle, ownedBy, distanceFromUser, basePrice, capacity;
        public MaterialButton reserveRideBtn, viewMoreBtn;
        public ConstraintLayout reservationsTemplate;

        // Constructor to instantiate each element.
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            carModel = itemView.findViewById(R.id.carModel);
            typeOfVehicle = itemView.findViewById(R.id.typeOfVehicle);
            ownedBy = itemView.findViewById(R.id.ownedBy);
            distanceFromUser = itemView.findViewById(R.id.distanceFromUser);
            basePrice = itemView.findViewById(R.id.basePrice);
            capacity = itemView.findViewById(R.id.capacity);
            reserveRideBtn = itemView.findViewById(R.id.reserveRideBtn);
            viewMoreBtn = itemView.findViewById(R.id.viewMoreBtn);
            reservationsTemplate = itemView.findViewById(R.id.reservationsTemplate);
        }
    }

    // Pass in the Vehicle array into the constructor
    public RidesAdapter(List<Vehicle> contacts, Activity activity, FragmentManager parentFragmentManager) {
        mVehicles = contacts;
        mAuth = FirebaseAuth.getInstance();
        fragmentManager = parentFragmentManager;
        this.activity = activity;
    }

    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return viewHolder instance.
     */
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

    /**
     * Populates data into the item through the ViewHolder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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
        getVehicleOwnerName(fullName -> {
            CharSequence cs = (CharSequence) ("Owned by " + fullName);
            finalTv.setText(cs);
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
        mb.setOnClickListener(v -> {
            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getUid());
            docRef.update("reservedRides", FieldValue.arrayUnion(vehicle.getVehicleID()));

            docRef = firestore.collection("vehicles")
                    .document(vehicle.getVehicleID());
            docRef.update("ridersUIDs", FieldValue.arrayUnion(mAuth.getUid()));
            holder.reservationsTemplate.setVisibility(View.GONE);
        });

        mb = holder.viewMoreBtn;
        mb.setOnClickListener(v -> {
            FragmentManager fm = fragmentManager;
            Fragment frag = new VehicleSchoolMapsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("vehicle", vehicle.getVehicleID());
            bundle.putBoolean("viewMore", true);
            frag.setArguments(bundle);
            fm.beginTransaction()
                    .replace(R.id.flFragment, frag)
                    .commit();
        });
    }

    /**
     * Callback to get Vehicle owner's full name and using it once connected to Firebase Firestore.
     * @param callback VehicleOwnerNameCallback
     * @param owner Vehicle owner's UID
     */
    private void getVehicleOwnerName(final VehicleOwnerNameCallback callback, String owner) {
        firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("users")
                .document(owner);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String fullName = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                callback.vehicleOwnerNameCallback(fullName);
            }
        });
    }

    /**
     * @param latLng1 LatLng aat point 1
     * @param latLng2 LatLng at point 2
     * @return the distance in km between latLng1 and latLng 2
     */
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



    /**
     * @return Returns the total count of items in the list
     */
    @Override
    public int getItemCount() {
        return mVehicles.size();
    }
}