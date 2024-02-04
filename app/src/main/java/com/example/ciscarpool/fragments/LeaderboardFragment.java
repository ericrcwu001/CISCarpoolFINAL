package com.example.ciscarpool.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.ciscarpool.*;
import com.example.ciscarpool.adapters.LeaderboardAdapter;
import com.example.ciscarpool.classes.User;
import com.example.ciscarpool.interfaces.LeaderboardCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

/**
 * {@link LeaderboardFragment} is a {@link Fragment} that controls the leaderboard page.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class LeaderboardFragment extends Fragment {
    private RecyclerView rankingsView;
    private FirebaseFirestore firestore;
    private ArrayList<User> mUsers = new ArrayList<>();

    // Required empty public constructor
    public LeaderboardFragment() {}

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
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
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

            openUsersSortedByCarbonSaved(result -> {
                LeaderboardAdapter adapter = new LeaderboardAdapter(result);
                rankingsView.setAdapter(adapter);
                rankingsView.setLayoutManager(new LinearLayoutManager(getActivity()));
            });
        }
    }

    /**
     * Callback to get a sorted list of all users by their total carbon saved.
     * @param callback
     */
    private void openUsersSortedByCarbonSaved(final LeaderboardCallback callback) {
        mUsers = new ArrayList<>();
        CollectionReference collectionReference = firestore.collection("users");
        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                User user = queryDocumentSnapshot.toObject(User.class);
                mUsers.add(user);
            }
            // Sorting based on total carbon saved
            mUsers.sort((a, b) -> (-1)*Double.compare(a.getKiloCarbonSaved(), b.getKiloCarbonSaved()));

            callback.leaderboardCallback(mUsers);
        });

    }

    /**
     * Instantiates respective views' variables.
     */
    private void elementsSetUp() {
        firestore = FirebaseFirestore.getInstance();
        rankingsView = getActivity().findViewById(R.id.leaderboardRecyclerView);
    }
}