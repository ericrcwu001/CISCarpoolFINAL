package com.example.ciscarpool.activities;

import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ciscarpool.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private EditText emailAddress;
    private EditText password;
    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_login);
        hideStatusBar();

        // pre-conditions
        elementsSetUp();
    }

    private void elementsSetUp() {
        emailAddress = (EditText) findViewById(R.id.emailAddress);
        password = (EditText) findViewById(R.id.password);

        // Setting up Firestore and Authentication
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void backBtnOnClick(View view) {
        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
        finish();
    }
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public void login(View view) {
        boolean validFields = checkAllFieldsValid();
        if (!validFields) return;

        String emailAddressString = emailAddress.getText().toString();
        String passwordString = password.getText().toString();

        mAuth.signInWithEmailAndPassword(emailAddressString, passwordString)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                } else {
                    emailAddress.setError("Wrong username or password.");
                    password.setError("Wrong username or password.");
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(this, MainViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
        finish();
    }

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
