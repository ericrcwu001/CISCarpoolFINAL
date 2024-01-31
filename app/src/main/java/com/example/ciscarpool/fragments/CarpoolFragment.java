package com.example.ciscarpool.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.ciscarpool.*;
import com.example.ciscarpool.adapters.RidesAdapter;
import com.example.ciscarpool.classes.Vehicle;
import com.example.ciscarpool.interfaces.RidesListCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

/**
 * {@link CarpoolFragment} is a {@link Fragment} that controls the carpool page.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class CarpoolFragment extends Fragment {
    private FragmentManager fragmentManager; // To prevent multiple instantiations.
    private RecyclerView ridesView;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ArrayList<Vehicle> mVehicles = new ArrayList<>();

    // Required empty public constructor
    public CarpoolFragment() {}

    /**
     * Start of the fragment lifecycle.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentManager = getParentFragmentManager();
        super.onCreate(savedInstanceState);
    }

    /**
     *
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
        return inflater.inflate(R.layout.fragment_carpool, container, false);
    }

    /**
     * Setups elements and RecyclerView.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && isAdded()) {
            elementsSetUp();

            openVehiclesSortedByDistance(result -> {
                RidesAdapter adapter = new RidesAdapter(result, getActivity(), fragmentManager);
                ridesView.setAdapter(adapter);
                ridesView.setLayoutManager(new LinearLayoutManager(getActivity()));
            });
        }
    }

    /**
     * Callback to get a list of all Vehicles that are currently open and not owned by the current
     * user.
     * @param callback RidesListCallback
     */
    private void openVehiclesSortedByDistance(final RidesListCallback callback) {
        mVehicles = new ArrayList<>();
        CollectionReference collectionReference = firestore.collection("vehicles");
        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                Vehicle vehicle = queryDocumentSnapshot.toObject(Vehicle.class);

                // Checks if Vehicle is open and not owned by current user.
                if (Boolean.TRUE.equals(queryDocumentSnapshot.getBoolean("open")) &&
                        !queryDocumentSnapshot.getString("owner")
                                .equals(mAuth.getCurrentUser().getUid())
                ) {
                    mVehicles.add(vehicle);
                }

                callback.ridesListCallback(mVehicles);
            }
        });

    }


    /**
     * Instantiates respective views' variables.
     */
    private void elementsSetUp() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        ridesView = getActivity().findViewById(R.id.reservationsRecyclerView);

    }
}