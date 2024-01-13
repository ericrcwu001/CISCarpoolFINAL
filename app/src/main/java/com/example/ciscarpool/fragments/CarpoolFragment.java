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

import com.example.ciscarpool.R;
import com.example.ciscarpool.adapters.RidesAdapter;
import com.example.ciscarpool.classes.Vehicle;
import com.example.ciscarpool.interfaces.RidesListCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CarpoolFragment extends Fragment {
    private FragmentManager fragmentManager;
    private RecyclerView ridesView;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ArrayList<Vehicle> mVehicles = new ArrayList<>();

    public CarpoolFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentManager = getParentFragmentManager();
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_carpool, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && isAdded()) {
            elementsSetUp();

            openVehiclesSortedByDistance(new RidesListCallback() {
                @Override
                public void ridesListCallback(ArrayList<Vehicle> result) {
                    RidesAdapter adapter = new RidesAdapter(result, getActivity(), fragmentManager);
                    ridesView.setAdapter(adapter);
                    ridesView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
            });
        }
    }

    private void openVehiclesSortedByDistance(final RidesListCallback callback) {
        mVehicles = new ArrayList<>();
        CollectionReference collectionReference = firestore.collection("vehicles");
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    Vehicle vehicle = queryDocumentSnapshot.toObject(Vehicle.class);
                    if (Boolean.TRUE.equals(queryDocumentSnapshot.getBoolean("open")) &&
                            !queryDocumentSnapshot.getString("owner")
                                    .equals(mAuth.getCurrentUser().getUid())
                    ) {
                        mVehicles.add(vehicle);
                    }

                    callback.ridesListCallback(mVehicles);
                }
            }
        });

    }

    private void elementsSetUp() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        ridesView = (RecyclerView) getActivity().findViewById(R.id.reservationsRecyclerView);

    }
}