package com.example.ciscarpool.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ciscarpool.R;
import com.example.ciscarpool.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private Spinner roleChoices;
    private MaterialButton backBtn;
    private MaterialButton uploadBtn;
    private EditText firstName, lastName, emailAddress, password;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private boolean checkIfImgPicked;
    private Uri selectedUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        hideStatusBar();

        // pre-conditions
        elementsSetUp();
        spinnerSetUp();
    }

    private void elementsSetUp() {
        roleChoices = (Spinner) findViewById(R.id.roleChoices);
        backBtn = (MaterialButton) findViewById(R.id.backBtn);
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        emailAddress = (EditText) findViewById(R.id.emailAddress);
        uploadBtn = (MaterialButton) findViewById(R.id.uploadBtn);
        password = (EditText) findViewById(R.id.password);

        // Setting up Firestore and Authentication
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        checkIfImgPicked = false;

        // Setting up image picker
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        checkIfImgPicked = true;
                        Log.d("PhotoPicker", "Selected URI: " + uri);

                        int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, flag);

                        selectedUri = uri;
                        uploadBtn.setCompoundDrawablesWithIntrinsicBounds(
                                getDrawable(R.drawable.share), null,
                                getDrawable(R.drawable.tick), null);
                    } else {
                        checkIfImgPicked = false;
                        Log.d("PhotoPicker", "No media selected");
                        uploadBtn.setCompoundDrawablesWithIntrinsicBounds(
                                getDrawable(R.drawable.share), null,
                                getDrawable(R.drawable.cross), null);
                    }
                });
    }

    private void spinnerSetUp() {
        List<String> mList = Arrays.asList("Student", "Faculty");
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_list, mList);
        mArrayAdapter.setDropDownViewResource(R.layout.spinner_list);

        roleChoices.setAdapter(mArrayAdapter);
    }

    public void uploadImg(View view) {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    public void backBtnOnClick(View view) {
        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
        finish();
    }

    public void register(View view) {
        boolean validFields = checkAllFieldsValid();
        if (!validFields) return;

        String firstNameString = firstName.getText().toString();
        String lastNameString = lastName.getText().toString();
        String roleString = roleChoices.getSelectedItem().toString();
        String emailAddressString = emailAddress.getText().toString();
        String passwordString = password.getText().toString();

//        FirebaseFirestore.setLoggingEnabled(true);

        mAuth.createUserWithEmailAndPassword(emailAddressString, passwordString)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User user = new User(mAuth.getCurrentUser().getUid().toString(),
                            firstNameString, lastNameString, emailAddressString, roleString,
                            new ArrayList<>(), selectedUri.toString(), new ArrayList<>());
                    firestore.collection("users").document(user.getuId()).set(user);
                    updateUI(mAuth.getCurrentUser());
                } else {
                    emailAddress.setError("You already have an account with this email. Please go to the login page.");
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(this, AddVehicleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
        finish();
    }

    private boolean checkAllFieldsValid() {
        boolean flag = true;
        if (firstName.getText().length() == 0) {
            firstName.setError("This field is required.");
            flag = false;
        } else {
            if (!Character.isUpperCase(firstName.getText().charAt(0))) {
                firstName.setError("Capitalize your first name.");
            }
        }
        if (lastName.getText().length() == 0) {
            lastName.setError("This field is required.");
            flag = false;
        } else {
            if (!Character.isUpperCase(lastName.getText().charAt(0))) {
                firstName.setError("Capitalize your last name.");
            }
        }
        if (emailAddress.getText().length() == 0) {
            emailAddress.setError("This field is required.");
            flag = false;
        } else {
            if (!emailAddress.getText().toString().contains("@")) {
                emailAddress.setError("Enter an email address.");
                flag = false;
            }
            if (emailAddress.getText().length() >= 10 && !emailAddress.getText().toString().substring(emailAddress.getText().length()-10).equals("cis.edu.hk")) {
                emailAddress.setError("Email must end with \"cis.edu.hk\".");
                flag = false;
            }
        }
        if (password.getText().length() == 0) {
            password.setError("This field is required.");
            flag = false;
        } else {
            if (password.getText().length() < 8) {
                password.setError("Password must be minimum 8 characters.");
                flag = false;
            }
        }
        return flag;
    }
}
