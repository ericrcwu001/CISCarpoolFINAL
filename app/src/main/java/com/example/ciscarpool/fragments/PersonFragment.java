package com.example.ciscarpool.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import jp.wasabeef.glide.transformations.BlurTransformation;
import com.example.ciscarpool.*;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * {@link PersonFragment} is a {@link Fragment} that controls the person page.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class PersonFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextView personRole, personName;
    private CircleImageView profile_image;
    private LinearLayout vehiclesView, reservationsView;
    private Activity activity; // To prevent multiple instantiations.
    private FragmentManager fragmentManager; // To prevent multiple instantiations.
    private FirebaseFirestore firestore;

    // Required empty public constructor
    public PersonFragment() {}

    /**
     * Instantiates respective views' variables.
     */
    private void elementsSetUp() {
        personName = getView().findViewById(R.id.personName);
        personRole = getView().findViewById(R.id.personRole);
        profile_image = getView().findViewById(R.id.profile_image);
        vehiclesView = getView().findViewById(R.id.vehiclesView);
        reservationsView = getView().findViewById(R.id.reservationsView);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Start of the fragment lifecycle.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        fragmentManager = getParentFragmentManager();
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
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    /**
     * Setups elements and populate them using data from Firebase Firestore.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Checks if fragment is still associated
        if (activity == null || !isAdded()) {
            return;
        }

        elementsSetUp();

        // Gets DocumentReference of the current user
        DocumentReference docRef = firestore.collection("users")
                .document(mAuth.getCurrentUser().getUid().toString());

        // Populates in the personName and personRole values
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Populating personName
                    String tName = document.getString("firstName") + " " + document.getString("lastName");
                    personName.setText(tName);
                    personRole.setText(document.getString("userType"));

                    // Populating "Your Vehicles"
                    ArrayList<String> vehicles = (ArrayList<String>) document.get("ownedVehicles");
                    if (vehicles.size() == 0) { // If current user has no vehicles
                        ConstraintLayout cl = addTextToCL(createDefaultConstraintLayout(),
                                "You have no vehicles at the moment.");
                        vehiclesView.addView(cl, 0);
                    } else {
                        for (String vehicleUID : vehicles) {
                            ConstraintLayout cl = vehicleCL(createDefaultConstraintLayout(), vehicleUID, true);
                            vehiclesView.addView(cl, 0);
                        }
                    }

                    // Populating "Your Reservations"
                    ArrayList<String> reservations = (ArrayList<String>) document.get("reservedRides");
                    if (reservations.size() == 0) { // If current user has no reservations
                        ConstraintLayout cl = addTextToCL(createDefaultConstraintLayout(),
                                "You have no reservations at the moment.");
                        reservationsView.addView(cl, 0);
                    } else {
                        for (String vehicleUID : reservations) {
                            ConstraintLayout cl = vehicleCL(createDefaultConstraintLayout(), vehicleUID, false);
                            reservationsView.addView(cl, 0);
                        }
                    }
                } else {
                    Log.d(PersonFragment.class.toString(), "No such document");
                    Toast.makeText(activity, "Internal error",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(PersonFragment.class.toString(), "1 get failed: " + task.getException());
                Toast.makeText(activity, "Internal error",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Getting profile picture and putting it in the circularImageView
        // Checking if current user is signed in through Google or not
        if (mAuth.getCurrentUser().getProviderId().equals("google.com")) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
            Uri personPhoto = account.getPhotoUrl();
            Glide.with(this).load(personPhoto)
                    .into((ImageView) getView().findViewById(R.id.profile_image));
        } else {
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && activity != null && isAdded()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Uri personPhoto = Uri.parse(document.getString("uriString"));
                        Glide.with(activity).load(personPhoto)
                                .into((ImageView) getView().findViewById(R.id.profile_image));

                    } else {
                        Log.d(PersonFragment.class.toString(), "No such document");
                        Toast.makeText(activity, "Internal error",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(PersonFragment.class.toString(), "2 get failed: " + task.getException());
                    Toast.makeText(activity, "Internal error",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     *
     * @param defaultConstraintLayout a pre-formatted {@link ConstraintLayout}. Use
     * {@link #createDefaultConstraintLayout()} for this.
     * @param text the text to be populating the {@link ConstraintLayout}
     * @return formatted {@link ConstraintLayout} with a TextView inside it according to
     * app design aesthetics.
     */
    private ConstraintLayout addTextToCL(ConstraintLayout defaultConstraintLayout, String text) {
        TextView tv = new TextView(activity);
        tv.setText(text);
        tv.setPadding(dpToPx(15),dpToPx(15),dpToPx(15),dpToPx(15));
        textViewFormat(tv);

        defaultConstraintLayout.addView(tv);

        // Adds layout constraints for the TextView within the ConstraintLayout
        ConstraintSet set = new ConstraintSet();
        set.clone(defaultConstraintLayout);
        set.connect(tv.getId(), ConstraintSet.TOP, defaultConstraintLayout.getId(), ConstraintSet.TOP);
        set.connect(tv.getId(), ConstraintSet.LEFT, defaultConstraintLayout.getId(), ConstraintSet.LEFT);
        set.connect(tv.getId(), ConstraintSet.BOTTOM, defaultConstraintLayout.getId(), ConstraintSet.BOTTOM);
        set.connect(tv.getId(), ConstraintSet.RIGHT, defaultConstraintLayout.getId(), ConstraintSet.RIGHT);
        set.applyTo(defaultConstraintLayout);

        return defaultConstraintLayout;
    }

    /**
     * Populates a {@link ConstraintLayout} with the data of a vehicle.
     *
     * @param cl {@link ConstraintLayout} to be populated with data.
     * @param vehicleUID UID of the vehicle to extract document from Firebase Firestore.
     * @param yourVehicles boolean value to check if {@link ConstraintLayout} is created in the
     *                     "Your Vehicles" or "Your Reservations" section.
     * @return a fully populated {@link ConstraintLayout}
     */
    private ConstraintLayout vehicleCL(ConstraintLayout cl, String vehicleUID, boolean yourVehicles) {
        DocumentReference docRef = firestore.collection("vehicles").document(vehicleUID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // Check if document exists
                if (document.exists()) {
                    cl.setClickable(true);

                    // Creates an onClickListener depending on if the ConstraintLayout
                    // is created in the "Your Vehicles" or "Your Reservations" sections.
                    if (yourVehicles)
                        cl.setOnClickListener(v -> {
                        FragmentManager fm = fragmentManager;
                        Fragment frag = new VehicleSchoolMapsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("vehicle", vehicleUID);
                        bundle.putString("user", mAuth.getCurrentUser().getUid().toString());
                        bundle.putBoolean("yourVehicles", true);
                        frag.setArguments(bundle);
                        fm.beginTransaction()
                                .replace(R.id.flFragment, frag)
                                .commit();
                    });
                    else
                        cl.setOnClickListener(v -> {
                        FragmentManager fm = fragmentManager;
                        Fragment frag = new VehicleSchoolMapsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("vehicle", vehicleUID);
                        bundle.putString("user", mAuth.getCurrentUser().getUid().toString());
                        bundle.putBoolean("yourReservations", true);
                        frag.setArguments(bundle);
                        fm.beginTransaction()
                                .replace(R.id.flFragment, frag)
                                .commit();
                    });


                    // Populates cl with the image of the car
                    Uri uri = Uri.parse(document.getString("image"));
                    ImageView imageView = new ImageView(activity);
                    imageView.setMaxWidth(dpToPx(200));
                    imageView.setMaxHeight(dpToPx(150));
                    imageView.setId(View.generateViewId());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(activity)
                            .load(uri)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(20, 2)))
                            .into(imageView);
                    cl.addView(imageView);
                    ConstraintSet set = new ConstraintSet();
                    set.clone(cl);
                    set.connect(imageView.getId(), ConstraintSet.TOP, cl.getId(), ConstraintSet.TOP);
                    set.connect(imageView.getId(), ConstraintSet.LEFT, cl.getId(), ConstraintSet.LEFT);
                    set.connect(imageView.getId(), ConstraintSet.BOTTOM, cl.getId(), ConstraintSet.BOTTOM);
                    set.connect(imageView.getId(), ConstraintSet.RIGHT, cl.getId(), ConstraintSet.RIGHT);
                    set.applyTo(cl);

                    // Creates an inner ConstraintLayout for the text
                    ConstraintLayout innerCL = new ConstraintLayout(activity);
                    innerCL.setId(View.generateViewId());
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                            dpToPx(124),
                            dpToPx(93)
                    );
                    innerCL.setLayoutParams(params);
                    cl.addView(innerCL);
                    set = new ConstraintSet();
                    set.clone(cl);
                    set.connect(innerCL.getId(), ConstraintSet.TOP, cl.getId(), ConstraintSet.TOP);
                    set.connect(innerCL.getId(), ConstraintSet.LEFT, cl.getId(), ConstraintSet.LEFT);
                    set.connect(innerCL.getId(), ConstraintSet.BOTTOM, cl.getId(), ConstraintSet.BOTTOM);
                    set.connect(innerCL.getId(), ConstraintSet.RIGHT, cl.getId(), ConstraintSet.RIGHT);
                    set.applyTo(cl);

                    // Populates partially transparent background in the innerCL
                    ImageView iv = new ImageView(activity);
                    iv.setMaxWidth(dpToPx(124));
                    iv.setMaxHeight(dpToPx(93));
                    iv.setId(View.generateViewId());
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(activity)
                            .load(R.drawable.low_opacity_bg)
                            .into(iv);
                    innerCL.addView(iv);
                    set = new ConstraintSet();
                    set.clone(innerCL);
                    set.connect(iv.getId(), ConstraintSet.TOP, innerCL.getId(), ConstraintSet.TOP);
                    set.connect(iv.getId(), ConstraintSet.LEFT, innerCL.getId(), ConstraintSet.LEFT);
                    set.connect(iv.getId(), ConstraintSet.BOTTOM, innerCL.getId(), ConstraintSet.BOTTOM);
                    set.connect(iv.getId(), ConstraintSet.RIGHT, innerCL.getId(), ConstraintSet.RIGHT);
                    set.applyTo(innerCL);

                    // Populates Vehicle Model Name
                    TextView carModel = new TextView(activity);
                    String carModelName = document.getString("model");
                    if (carModelName.length() > 20) {
                        carModelName = carModelName.substring(0, 18) + "...";
                    }
                    carModel.setText(carModelName);
                    textViewFormat(carModel);
                    innerCL.addView(carModel);

                    // Populates Vehicle Capacity
                    TextView capacity = new TextView(activity);
                    String capacityStr = String.valueOf(((Long)document.get("capacity")).intValue()
                            - ((ArrayList<String>) document.get("ridersUIDs")).size()) + " seats left";
                    capacity.setText(capacityStr);
                    textViewFormat(capacity);
                    innerCL.addView(capacity);

                    // Populates Vehicle Price
                    TextView price = new TextView(activity);
                    String priceStr = "$" + ((Double) document.get("basePrice")).toString() + " per ride";
                    price.setText(priceStr);
                    textViewFormat(price);
                    innerCL.addView(price);

                    // Creates layout constraints for:
                    set = new ConstraintSet();
                    set.clone(innerCL);
                    // model
                    set.connect(carModel.getId(), ConstraintSet.TOP, innerCL.getId(), ConstraintSet.TOP);
                    set.connect(carModel.getId(), ConstraintSet.LEFT, innerCL.getId(), ConstraintSet.LEFT);
                    set.connect(carModel.getId(), ConstraintSet.RIGHT, innerCL.getId(), ConstraintSet.RIGHT);
                    // capacity
                    set.connect(capacity.getId(), ConstraintSet.TOP, carModel.getId(), ConstraintSet.BOTTOM);
                    set.connect(capacity.getId(), ConstraintSet.LEFT, innerCL.getId(), ConstraintSet.LEFT);
                    set.connect(capacity.getId(), ConstraintSet.RIGHT, innerCL.getId(), ConstraintSet.RIGHT);
                    // price
                    set.connect(price.getId(), ConstraintSet.LEFT, innerCL.getId(), ConstraintSet.LEFT);
                    set.connect(price.getId(), ConstraintSet.RIGHT, innerCL.getId(), ConstraintSet.RIGHT);
                    set.connect(price.getId(), ConstraintSet.BOTTOM, innerCL.getId(), ConstraintSet.BOTTOM);

                    set.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                            new int[]{carModel.getId(), capacity.getId(), price.getId()}, null, ConstraintSet.CHAIN_PACKED);
                    set.applyTo(innerCL);

                } else {
                    Log.d(PersonFragment.class.toString(), "No such document");
                    Toast.makeText(activity, "Internal error",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(PersonFragment.class.toString(), "3 get failed: " + task.getException());
                Toast.makeText(activity, "Internal error",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return cl;
    }

    /**
     * Formats a {@link TextView} according to app design aesthetics.
     * @param tv {@link TextView} to be formatted
     */
    private void textViewFormat(TextView tv) {
        tv.setTextColor(activity.getResources().getColor(R.color.mainColour));
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(activity.getResources().getFont(R.font.spotify_font));
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setId(View.generateViewId());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
    }

    /**
     *
     * @return formatted ConstraintLayout according to app design aesthetics.
     */
    private ConstraintLayout createDefaultConstraintLayout() {
        ConstraintLayout cl = new ConstraintLayout(activity);
        cl.setBackgroundResource(R.drawable.border);
        cl.setId(View.generateViewId());

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                dpToPx(200),
                dpToPx(150)
        );
        params.setMargins(0, 0, 20, 0);
        cl.setLayoutParams(params);

        return cl;
    }

    /**
     *
     * @param dp a dp value.
     * @return corresponding px value.
     */
    private int dpToPx(int dp) {
        int x = (int) (dp * ((float) activity.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return x;
    }
}