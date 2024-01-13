package com.example.ciscarpool.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.ciscarpool.R;
import com.example.ciscarpool.fragments.CarpoolFragment;
import com.example.ciscarpool.fragments.PersonFragment;
import com.example.ciscarpool.fragments.LeaderboardFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainViewActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

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
    }
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Bundle bundle = new Bundle();
        if (itemId == R.id.person) {
            PersonFragment personFragment = new PersonFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, personFragment)
                    .commit();
            return true;
        } else if (itemId == R.id.carpool) {
            CarpoolFragment carpoolFragment = new CarpoolFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, carpoolFragment)
                    .commit();
            return true;
        } else if (itemId == R.id.leaderboard) {
            LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, leaderboardFragment)
                    .commit();
            return true;
        }
        return false;
    }

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

    public void goToAddVehicle(View view) {
        Intent intent = new Intent(MainViewActivity.this, AddVehicleActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void goToCarpoolView(View view) {
        CarpoolFragment carpoolFragment = new CarpoolFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, carpoolFragment)
                .commit();
        bottomNavigationView.getMenu().getItem(1).setChecked(true);
    }


}
