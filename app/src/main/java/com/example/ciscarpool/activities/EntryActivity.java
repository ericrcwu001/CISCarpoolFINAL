package com.example.ciscarpool.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ciscarpool.R;
import com.example.ciscarpool.classes.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Objects;

/**
 * {@link EntryActivity} is for users to register/login through the email and password or Google-Sign-In
 * protocols provided through Firebase Authentication.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class EntryActivity extends AppCompatActivity {
    private Button signup, login;
    private TextView googleEmailError;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private final int RC_SIGN_IN = 17;
    private Bundle appBundle;

    /**
     * Start of the activity lifecycle. Setup + assigns click listeners to buttons.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        hideStatusBar();
        elementsSetUp();

        // Checks if already signed in. If so, launch the MainViewActivity.
        if (currentUser != null) {
            Intent intent = new Intent(EntryActivity.this, MainViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
            finish();
        }

        // Setup signup button listener
        signup.setOnClickListener(v -> {
            Intent intent = new Intent(EntryActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
        });

        // Setup login button listener
        login.setOnClickListener(v -> {
            Intent intent = new Intent(EntryActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
        });
    }

    /**
     * Instantiates respective views' variables.
     */
    private void elementsSetUp() {
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.login);
        googleEmailError = findViewById(R.id.googleEmailError);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        ApplicationInfo app;
        try {
            app = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            appBundle = app.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Variable instantiation failure.",
                    Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * Hide's the status bar on Android.
     * **/
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * Launches the Google Sign In intent.
     * @param view passed by the onClick property of the Button View.
     */
    public void continueWithGoogle(View view) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Objects.requireNonNull(appBundle.getString("default_web_client_id"))).requestEmail()
                .requestProfile().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut();

        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    /**
     * Gets Google Sign In account task.
     * **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Check if Google Sign In account is a valid account.
     * **/
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuth(account.getIdToken(), account);
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign In failed!",
                    Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * Signs in with Firebase Authentication using GoogleSignInAccount.
     * **/
    private void firebaseAuth(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);

        // Firebase Auth sign in
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Checking email validity
                        if (!account.getEmail().endsWith("cis.edu.hk")) {
                            googleEmailError.setText("Use a CIS email account.");
                            return;
                        }

                        // Check if Google account is already registered in Firebase
                        DocumentReference docRef = firestore.collection("users")
                                .document(mAuth.getCurrentUser().getUid().toString());
                        docRef.get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document = task1.getResult();
                                if (!document.exists()) {
                                    User user2 = new User(mAuth.getCurrentUser().getUid().toString(),
                                            account.getGivenName(), account.getFamilyName(),
                                            account.getEmail(), getRoleFromEmail(account.getEmail()),
                                            new ArrayList<>(), account.getPhotoUrl().toString(),
                                            new ArrayList<>(), 0);

                                    firestore.collection("users").document(user2.getuId()).set(user2);
                                }
                            } else {
                                Log.d(this.getLocalClassName(), "Failed with: ", task1.getException());
                            }
                        });

                        updateUI();
                    } else {
                        Log.w(this.getLocalClassName(), "signInWithCredential:failure", task.getException());
                    }
                });
    }

    /**
     * @param email A valid CIS email
     * @return The role of the user - student or faculty
     * **/
    private String getRoleFromEmail(String email) {
        if (email.endsWith("student.cis.edu.hk")) {
            return "Student";
        } else {
            return "Faculty";
        }
    }

    /**
     * Launches the MainViewActivity Intent.
     * **/
    private void updateUI() {
        Intent intent = new Intent(EntryActivity.this, MainViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
        finish();
    }

}