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
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ciscarpool.R;
import com.example.ciscarpool.classes.ElectricVehicle;
import com.example.ciscarpool.classes.FuelVehicle;
import com.example.ciscarpool.classes.HybridVehicle;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddVehicleActivity extends AppCompatActivity {
    private Uri selectedUri;
    private EditText carModel;
    private EditText seatingCapacity;

    private EditText pricePerRide;
    private MaterialButton skipBtn;
    private MaterialButton uploadBtn;
    private MaterialButton addVehicleBtn;
    private Spinner vehicleChoices;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private boolean checkIfImgPicked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);
        hideStatusBar();

        elementsSetUp();
        spinnerSetUp();
    }

    private void elementsSetUp() {
        skipBtn = (MaterialButton) findViewById(R.id.skipBtn);
        uploadBtn = (MaterialButton) findViewById(R.id.uploadBtn);
        addVehicleBtn = (MaterialButton) findViewById(R.id.addVehicleBtn);
        carModel = (EditText) findViewById(R.id.carModel);
        seatingCapacity = (EditText) findViewById(R.id.seatingCapacity);
        pricePerRide = (EditText) findViewById(R.id.pricePerRide);
        vehicleChoices = (Spinner) findViewById(R.id.vehicleChoices);
        checkIfImgPicked = false;

        // Setting up Firestore
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setting up image picker
        pickMedia =
                registerForActivityResult(new PickVisualMedia(), uri -> {
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
        List<String> mList = Arrays.asList("Fuel Vehicle",
                "Hybrid Vehicle", "Electric Vehicle");
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_list, mList);
        mArrayAdapter.setDropDownViewResource(R.layout.spinner_list);

        vehicleChoices.setAdapter(mArrayAdapter);
    }

    public void skip(View view) {
        Intent intent = new Intent(this, MainViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
        finish();
    }

    public void uploadImg(View view) {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    public void addVehicle(View view) {
        Log.d("check", "check");
        boolean validFields = checkAllFieldsValid();
        if (!validFields) return;

        String carModelString = carModel.getText().toString();
        int seatingCapacityString = Integer.parseInt(seatingCapacity.getText().toString());
        String vehicleString = vehicleChoices.getSelectedItem().toString();
        double pricePerRideString = Double.parseDouble(pricePerRide.getText().toString());
        String uriString = selectedUri.toString();

        String vehicleUniqueId = UUID.randomUUID().toString();


        if (vehicleString.equals("Fuel Vehicle")) {
            FuelVehicle vehicle = new FuelVehicle(mAuth.getCurrentUser().getUid().toString(), carModelString,
                    seatingCapacityString, vehicleUniqueId, new ArrayList<>(), true,
                    pricePerRideString, uriString);
            firestore.collection("vehicles").document(vehicle.getVehicleID()).set(vehicle);
            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getCurrentUser().getUid().toString());
            docRef.update("ownedVehicles", FieldValue.arrayUnion(vehicle.getVehicleID()));

        } else if (vehicleString.equals("Hybrid Vehicle")) {
            HybridVehicle vehicle = new HybridVehicle(mAuth.getCurrentUser().getUid().toString(), carModelString,
                    seatingCapacityString, vehicleUniqueId, new ArrayList<>(), true,
                    pricePerRideString, uriString);
            firestore.collection("vehicles").document(vehicle.getVehicleID()).set(vehicle);
            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getCurrentUser().getUid().toString());
            docRef.update("ownedVehicles", FieldValue.arrayUnion(vehicle.getVehicleID()));
        } else {
            ElectricVehicle vehicle = new ElectricVehicle(mAuth.getCurrentUser().getUid().toString(), carModelString,
                    seatingCapacityString, vehicleUniqueId, new ArrayList<>(), true,
                    pricePerRideString, uriString);
            firestore.collection("vehicles").document(vehicle.getVehicleID()).set(vehicle);
            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getCurrentUser().getUid().toString());
            docRef.update("ownedVehicles", FieldValue.arrayUnion(vehicle.getVehicleID()));
        }

        Intent intent = new Intent(this, MainViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
        finish();
    }

    private boolean checkAllFieldsValid() {
        boolean flag = checkIfImgPicked;
        if (carModel.getText().length() == 0) {
            carModel.setError("This field is required.");
            flag = false;
        } else {
            if (!Character.isUpperCase(carModel.getText().charAt(0))) {
                carModel.setError("Use correct capitalization.");
            }
        }
        if (seatingCapacity.getText().length() == 0) {
            seatingCapacity.setError("This field is required.");
            flag = false;
        } else {
            int seatingCapacityInt;
            try {
                seatingCapacityInt = Integer.parseInt(seatingCapacity.getText().toString());
                if (seatingCapacityInt > 10 || seatingCapacityInt < 1) {
                    seatingCapacity.setError("Enter a number between 1 and 10.");
                    flag = false;
                }
            } catch (NumberFormatException e) {
                seatingCapacity.setError("Enter a number.");
                flag = false;
            }
        }
        if (pricePerRide.getText().length() == 0) {
            pricePerRide.setError("This field is required.");
            flag = false;
        } else {
            double pricePerRideDouble;
            try {
                pricePerRideDouble = Double.parseDouble(pricePerRide.getText().toString());
                if (pricePerRideDouble > 10 || pricePerRideDouble < 0) {
                    pricePerRide.setError("Enter a number between 0 and 10.");
                    flag = false;
                }
            } catch (NumberFormatException e) {
                pricePerRide.setError("Enter a number.");
                flag = false;
            }

        }
        return flag;
    }
}
