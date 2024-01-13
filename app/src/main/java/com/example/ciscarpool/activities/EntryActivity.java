package com.example.ciscarpool.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.ciscarpool.R;
import com.example.ciscarpool.classes.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class EntryActivity extends AppCompatActivity {
    private Button signup, login;
    private TextView googleEmailError;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        hideStatusBar();
        elementsSetUp();

//        if (mAuth.getCurrentUser().getProviderId().equals("google.com")) {
//            mAuth.signOut();
//            GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
//        } else {
//            mAuth.signOut();
//        }


        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Intent intent = new Intent(EntryActivity.this, MainViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
            finish();
        }

        // signup button listener
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntryActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntryActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
            }
        });
    }

    private void elementsSetUp() {
        signup = (Button) findViewById(R.id.signup);
        login = (Button) findViewById(R.id.login);
        googleEmailError = (TextView) findViewById(R.id.googleEmailError);
        mAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();
    }

    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public void continueWithGoogle(View view) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().requestProfile().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut();

        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuth(account.getIdToken(), account);
        } catch (ApiException e) {
            Log.w("EntryActivity", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuth(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (!account.getEmail().endsWith("cis.edu.hk")) {
                                googleEmailError.setText("Use a CIS email account.");
                                return;
                            }

                            DocumentReference docRef = firestore.collection("users")
                                    .document(mAuth.getCurrentUser().getUid().toString());
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d("check", "Document exists!");
                                        } else {
                                            User user2 = new User(mAuth.getCurrentUser().getUid().toString(),
                                                    account.getGivenName(), account.getFamilyName(),
                                                    account.getEmail(), getRoleFromEmail(account.getEmail()),
                                                    new ArrayList<>(), account.getPhotoUrl().toString(), new ArrayList<>(), 0);

                                            firestore.collection("users").document(user2.getuId()).set(user2);
                                        }
                                    } else {
                                        Log.d("check", "Failed with: ", task.getException());
                                    }
                                }
                            });

                            updateUI(user);
                        } else {
                            Log.w("GOOGLE SIGN IN", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private String getRoleFromEmail(String email) {
        if (email.endsWith("student.cis.edu.hk")) {
            return "Student";
        } else {
            return "Faculty";
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        Intent intent = new Intent(EntryActivity.this, MainViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
        finish();
    }

}