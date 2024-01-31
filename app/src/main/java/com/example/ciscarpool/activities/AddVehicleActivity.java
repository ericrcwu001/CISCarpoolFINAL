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
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ciscarpool.*;
import com.example.ciscarpool.classes.ElectricVehicle;
import com.example.ciscarpool.classes.FuelVehicle;
import com.example.ciscarpool.classes.HybridVehicle;
import com.example.ciscarpool.classes.Vehicle;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * {@link AddVehicleActivity} is for users to register their vehicles to the Firebase Firestore.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class AddVehicleActivity extends AppCompatActivity {
    private Uri selectedUri;
    private EditText carModel;
    private EditText seatingCapacity;
    private EditText pricePerRide;
    private MaterialButton skipBtn;
    private MaterialButton uploadBtn;
    private MaterialButton addVehicleBtn;
    private MaterialButton locationBtn;
    private Spinner vehicleChoices;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private boolean checkIfImgPicked;
    private boolean checkIfLocationPicked;
    private ArrayList<Double> latLng;
    private ActivityResultLauncher<Intent> addLocationToAddVehicleLauncher;

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
        setContentView(R.layout.activity_add_vehicle);
        hideStatusBar();

        elementsSetUp();
        spinnerSetUp();
    }

    /**
     * Instantiates respective views' variables.
     */
    private void elementsSetUp() {
        skipBtn = findViewById(R.id.skipBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        addVehicleBtn = findViewById(R.id.addVehicleBtn);
        locationBtn = findViewById(R.id.locationBtn);
        carModel = findViewById(R.id.carModel);
        seatingCapacity = findViewById(R.id.seatingCapacity);
        pricePerRide = findViewById(R.id.pricePerRide);
        vehicleChoices = findViewById(R.id.vehicleChoices);
        checkIfImgPicked = false;
        checkIfLocationPicked = false;
        latLng = new ArrayList<>();

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

        // Setting up location picker
        addLocationToAddVehicleLauncher = registerForActivityResult(new
                        ActivityResultContracts.StartActivityForResult(),
                o -> {
                    Intent intent = o.getData();
                    if (intent != null) {
                        Bundle data = intent.getExtras();
                        checkIfLocationPicked = true;
                        latLng.add(data.getDouble("lat"));
                        latLng.add(data.getDouble("lng"));
                        Log.d("latlng", latLng.toString());
                        locationBtn.setCompoundDrawablesWithIntrinsicBounds(
                                getDrawable(R.drawable.location), null,
                                getDrawable(R.drawable.tick), null);
                    } else {
                        checkIfLocationPicked = false;
                        Log.d("PhotoPicker", "No media selected");
                        locationBtn.setCompoundDrawablesWithIntrinsicBounds(
                                getDrawable(R.drawable.location), null,
                                getDrawable(R.drawable.cross), null);
                    }
                }
        );
    }

    /**
     * Instantiates spinnerView's choices and layout.
     */
    private void spinnerSetUp() {
        List<String> mList = Arrays.asList("Fuel Vehicle",
                "Hybrid Vehicle", "Electric Vehicle");
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_list, mList);
        mArrayAdapter.setDropDownViewResource(R.layout.spinner_list);

        vehicleChoices.setAdapter(mArrayAdapter);
    }

    /**
     * End the {@link AddVehicleActivity} lifecycle.
     * @param view passed by the onClick property of the Button View.
     */
    public void skip(View view) {
        finish();
    }

    /**
     * Launches media picker.
     * @param view passed by the onClick property of the Button View.
     */
    public void uploadImg(View view) {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    /**
     * Hide's the status bar on Android.
     * **/
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    /**
     * Attempts to register vehicle to Firebase Firestore.
     * @param view passed by the onClick property of the Button View.
     */
    public void addVehicle(View view) {
        // Make sure all entered fields meet the requirements set for them. If not, register fails.
        boolean validFields = checkAllFieldsValid();
        if (!validFields) {
            Toast.makeText(this, "Check all your fields!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Get values of relevant fields
        String carModelString = carModel.getText().toString();
        String vehicleString = vehicleChoices.getSelectedItem().toString();
        String uriString = selectedUri.toString();
        String vehicleUniqueId = UUID.randomUUID().toString();
        int seatingCapacityString = Integer.parseInt(seatingCapacity.getText().toString());
        double pricePerRideString = Double.parseDouble(pricePerRide.getText().toString());

        // Checks what type of vehicle user is registering
        if (vehicleString.equals("Fuel Vehicle")) {
            FuelVehicle vehicle = new FuelVehicle(mAuth.getCurrentUser().getUid().toString(),
                    carModelString, seatingCapacityString, vehicleUniqueId, new ArrayList<>(),
                    true, pricePerRideString, uriString, latLng);
            firestore.collection("vehicles").document(vehicle.getVehicleID())
                    .set((Vehicle) vehicle);

            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getCurrentUser().getUid().toString());
            docRef.update("ownedVehicles", FieldValue.arrayUnion(vehicle.getVehicleID()));

        } else if (vehicleString.equals("Hybrid Vehicle")) {
            HybridVehicle vehicle = new HybridVehicle(mAuth.getCurrentUser().getUid().toString(),
                    carModelString, seatingCapacityString, vehicleUniqueId, new ArrayList<>(),
                    true, pricePerRideString, uriString, latLng);
            firestore.collection("vehicles").document(vehicle.getVehicleID())
                    .set((Vehicle) vehicle);

            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getCurrentUser().getUid().toString());
            docRef.update("ownedVehicles", FieldValue.arrayUnion(vehicle.getVehicleID()));
        } else {
            ElectricVehicle vehicle = new ElectricVehicle(mAuth.getCurrentUser().getUid().toString(),
                    carModelString, seatingCapacityString, vehicleUniqueId, new ArrayList<>(),
                    true, pricePerRideString, uriString, latLng);
            firestore.collection("vehicles").document(vehicle.getVehicleID())
                    .set((Vehicle) vehicle);

            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getCurrentUser().getUid().toString());
            docRef.update("ownedVehicles", FieldValue.arrayUnion(vehicle.getVehicleID()));
        }

        finish();
    }


    /**
     *
     * @return whether or not all entered fields are valid
     */
    private boolean checkAllFieldsValid() {
        boolean flag = checkIfImgPicked && checkIfLocationPicked;
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

    /**
     * Launches activity to add a location associated to the vehicle.
     * @param view passed by the onClick property of the Button View.
     */
    public void insertLocation(View view) {
        Intent intent = new Intent(this, AddLocationToAddVehicleActivity.class);
        addLocationToAddVehicleLauncher.launch(intent);
    }
}
