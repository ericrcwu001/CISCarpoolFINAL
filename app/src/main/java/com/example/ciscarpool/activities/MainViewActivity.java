package com.example.ciscarpool.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ciscarpool.R;
import com.example.ciscarpool.fragments.CarpoolFragment;
import com.example.ciscarpool.fragments.PersonFragment;
import com.example.ciscarpool.fragments.LeaderboardFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * {@link MainViewActivity} is the main intractable activity to access the person, carpool, and
 * leaderboard pages. It implements a {@link BottomNavigationView}.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class MainViewActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    private static int page = 0;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    /**
     * Start of the activity lifecycle. Setup + assigns click listeners to buttons.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        hideStatusBar();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.person);

        PersonFragment personFragment = new PersonFragment();
        page = 1;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, personFragment)
                .commit();
    }

    /**
     * Hide's the status bar on Android.
     * **/
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * On navigation item selected, go to it's corresponding page's fragment.
     * @param item The selected item
     * @return whether or not navigation item is successfully selected.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (page != 1 && itemId == R.id.person) {
            PersonFragment personFragment = new PersonFragment();
            page = 1;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, personFragment)
                    .commit();
            return true;
        } else if (page != 2 && itemId == R.id.carpool) {
            CarpoolFragment carpoolFragment = new CarpoolFragment();
            page = 2;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, carpoolFragment)
                    .commit();
            return true;
        } else if (page != 3 && itemId == R.id.leaderboard) {
            LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
            page = 3;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, leaderboardFragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Signs out of the current user through Firebase Auth. Moves to {@link EntryActivity}.
     * @param view passed by the onClick property of the Button View.
     */
    public void signOut(View view) {
        if (mAuth.getCurrentUser().getProviderId().equals("google.com")) {
            mAuth.signOut();
            GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        } else {
            mAuth.signOut();
        }
        Intent intent = new Intent(MainViewActivity.this, EntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        finish();
    }

    /**
     * Moves to {@link AddVehicleActivity}.
     * @param view passed by the onClick property of the Button View.
     */
    public void goToAddVehicle(View view) {
        Intent intent = new Intent(MainViewActivity.this, AddVehicleActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Moves to {@link CarpoolFragment}.
     * @param view passed by the onClick property of the Button View.
     */
    public void goToCarpoolView(View view) {
        CarpoolFragment carpoolFragment = new CarpoolFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, carpoolFragment)
                .commit();
        bottomNavigationView.getMenu().getItem(1).setChecked(true);
    }


}
