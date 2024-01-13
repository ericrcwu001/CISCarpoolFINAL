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
import com.example.ciscarpool.adapters.LeaderboardAdapter;
import com.example.ciscarpool.classes.User;
import com.example.ciscarpool.interfaces.LeaderboardCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class LeaderboardFragment extends Fragment {
    private FragmentManager fragmentManager;
    private RecyclerView rankingsView;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ArrayList<User> mUsers = new ArrayList<>();

    public LeaderboardFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getParentFragmentManager();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && isAdded()) {
            elementsSetUp();
            openUsersSortedByCarbonSaved(new LeaderboardCallback() {
                @Override
                public void leaderboardCallback(ArrayList<User> result) {
                    LeaderboardAdapter adapter = new LeaderboardAdapter(result, getActivity(), fragmentManager);
                    rankingsView.setAdapter(adapter);
                    rankingsView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
            });
        }
    }

    private void openUsersSortedByCarbonSaved(final LeaderboardCallback callback) {
        mUsers = new ArrayList<>();
        CollectionReference collectionReference = firestore.collection("users");
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    User user = queryDocumentSnapshot.toObject(User.class);
                    mUsers.add(user);

                    mUsers.sort((a, b) -> (-1)*Double.compare(a.getKiloCarbonSaved(), b.getKiloCarbonSaved()));

                    callback.leaderboardCallback(mUsers);
                }
            }
        });

    }

    private void elementsSetUp() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        rankingsView = (RecyclerView) getActivity().findViewById(R.id.leaderboardRecyclerView);
    }
}