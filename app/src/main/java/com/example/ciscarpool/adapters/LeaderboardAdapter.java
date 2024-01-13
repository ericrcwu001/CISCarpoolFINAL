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
import com.example.ciscarpool.classes.User;
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

public class LeaderboardAdapter extends
        RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private FirebaseFirestore firestore;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private Activity activity;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        public TextView totalRidesDone, personName, personRanking, totalCarbonSaved;
        public ImageView carImg;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            totalRidesDone = (TextView) itemView.findViewById(R.id.totalRidesDone);
            personName = (TextView) itemView.findViewById(R.id.personName);
            personRanking = (TextView) itemView.findViewById(R.id.personRanking);
            totalCarbonSaved = (TextView) itemView.findViewById(R.id.totalCarbonSaved);
        }
    }

    // Store a member variable for the contacts
    private List<User> mUsers;

    // Pass in the contact array into the constructor
    public LeaderboardAdapter(List<User> users, Activity activity, FragmentManager parentFragmentManager) {
        mUsers = users;
        mAuth = FirebaseAuth.getInstance();
        fragmentManager = parentFragmentManager;
        this.activity = activity;
    }

    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.leaderboard_ranking_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(LeaderboardAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        User user = mUsers.get(position);

        // Set item views based on your views and data model
        TextView tv = holder.totalRidesDone;


        tv = holder.personName;
        tv.setText((CharSequence) (user.getFirstName() + " " + user.getLastName()));

        tv = holder.personRanking;
        tv.setText((CharSequence) ("#" + String.valueOf(position+1)));

        tv = holder.totalCarbonSaved;
        String numericKiloCarbonSavedStr = String.valueOf(user.getKiloCarbonSaved());
        tv.setText((CharSequence) ("Saved " + numericKiloCarbonSavedStr + " kg of carbon"));

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}