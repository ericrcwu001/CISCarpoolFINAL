package com.example.ciscarpool.activities;

import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ciscarpool.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * {@link LoginActivity} is for users to login through the email and password protocols provided
 * through Firebase Authentication.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private EditText emailAddress;
    private EditText password;

    /**
     * Start of the activity lifecycle. Setup + assigns click listeners to buttons.
     */
    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_login);

        hideStatusBar();
        elementsSetUp();
    }

    /**
     * Instantiates respective views' variables.
     */
    private void elementsSetUp() {
        emailAddress = findViewById(R.id.emailAddress);
        password = findViewById(R.id.password);
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Back button onClick method to go back to {@link EntryActivity}.
     * @param view passed by the onClick property of the Button View.
     */
    public void backBtnOnClick(View view) {
        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
        finish();
    }

    /**
     * Hide's the status bar on Android.
     * **/
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * Attempts to login through Firebase Authentication
     * @param view passed by the onClick property of the Button View.
     */
    public void login(View view) {
        // Make sure all entered fields meet the requirements set for them. If not, login fails.
        boolean validFields = checkAllFieldsValid();
        if (!validFields) {
            Toast.makeText(this, "Check all your fields!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Get values of emailAddress and password fields
        String emailAddressString = emailAddress.getText().toString();
        String passwordString = password.getText().toString();

        // Firebase Auth sign in
        mAuth.signInWithEmailAndPassword(emailAddressString, passwordString)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUI();
                    } else {
                        emailAddress.setError("Wrong username or password.");
                        password.setError("Wrong username or password.");
                    }
                });
    }

    /**
     * Launches the MainViewActivity Intent.
     * **/
    private void updateUI() {
        Intent intent = new Intent(this, MainViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
        finish();
    }

    /**
     *
     * @return whether or not all entered fields are valid
     */
    private boolean checkAllFieldsValid() {
        boolean flag = true;
        if (emailAddress.getText().length() == 0) {
            emailAddress.setError("This field is required.");
            flag = false;
        } else {
            if (!emailAddress.getText().toString().contains("@")) {
                emailAddress.setError("Enter an email address.");
                flag = false;
            }
        }
        if (password.getText().length() == 0) {
            password.setError("This field is required.");
            flag = false;
        }
        return flag;
    }
}
