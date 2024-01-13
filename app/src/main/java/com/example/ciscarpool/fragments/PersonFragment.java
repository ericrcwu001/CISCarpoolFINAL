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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import jp.wasabeef.glide.transformations.BlurTransformation;
import com.example.ciscarpool.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextView personRole, personName;
    private CircleImageView profile_image;
    private LinearLayout vehiclesView, reservationsView;
    private Activity activity;
    private FragmentManager fragmentManager;

    private FirebaseFirestore firestore;
    private DocumentSnapshot documentSnapshot;
    public PersonFragment() {
        // Required empty public constructor
    }
    private void elementsSetUp() {
        personName = (TextView) getView().findViewById(R.id.personName);
        personRole = (TextView) getView().findViewById(R.id.personRole);
        profile_image = (CircleImageView) getView().findViewById(R.id.profile_image);
        vehiclesView = (LinearLayout) getView().findViewById(R.id.vehiclesView);
        reservationsView = (LinearLayout) getView().findViewById(R.id.reservationsView);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        fragmentManager = getParentFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity != null && isAdded()) {
            elementsSetUp();

            DocumentReference docRef = firestore.collection("users")
                    .document(mAuth.getCurrentUser().getUid().toString());

            // fills in the personName and personRole
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            String tName = document.getString("firstName") + " " + document.getString("lastName");
                            personName.setText(tName);
                            personRole.setText(document.getString("userType"));

                            ArrayList<String> vehicles = (ArrayList<String>) document.get("ownedVehicles");
                            if (vehicles.size() == 0) {
                                ConstraintLayout cl = addTextToCL(createDefaultConstraintLayout(),
                                        "You have no vehicles at the moment.");
                                vehiclesView.addView(cl, 0);
                            } else {
                                for (String vehicleUID : vehicles) {
                                    ConstraintLayout cl = vehicleCL(createDefaultConstraintLayout(), vehicleUID, true);
                                    vehiclesView.addView(cl, 0);
                                }
                            }

                            ArrayList<String> reservations = (ArrayList<String>) document.get("reservedRides");
                            if (reservations.size() == 0) {
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
                            Log.d("PERSON FRAGMENT", "No such document");
                        }
                    } else {
                        Log.d("PERSON FRAGMENT", "get failed with ", task.getException());
                    }
                }
            });


            // getting profile picture and putting it in the circularImageView
            if (mAuth.getCurrentUser().getProviderId().equals("google.com")) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
                Uri personPhoto = account.getPhotoUrl();
                Glide.with(this).load(personPhoto)
                        .into((ImageView) getView().findViewById(R.id.profile_image));
            } else {
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && getActivity() != null && isAdded()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                Uri personPhoto = Uri.parse(document.getString("uriString"));
                                Glide.with(activity).load(personPhoto)
                                        .into((ImageView) getView().findViewById(R.id.profile_image));

                            } else {
                                Log.d("PROFILE PICTURE", "No such document");
                            }
                        } else {
                            Log.d("PROFILE PICTURE", "get failed with ", task.getException());
                        }
                    }
                });
            }
        }
    }

    private ConstraintLayout addTextToCL(ConstraintLayout defaultConstraintLayout, String text) {
        TextView tv = new TextView(activity);
        tv.setText(text);
        tv.setPadding(dpToPx(15),dpToPx(15),dpToPx(15),dpToPx(15));
        textViewFormat(tv);

        defaultConstraintLayout.addView(tv);
        ConstraintSet set = new ConstraintSet();
        set.clone(defaultConstraintLayout);
        set.connect(tv.getId(), ConstraintSet.TOP, defaultConstraintLayout.getId(), ConstraintSet.TOP);
        set.connect(tv.getId(), ConstraintSet.LEFT, defaultConstraintLayout.getId(), ConstraintSet.LEFT);
        set.connect(tv.getId(), ConstraintSet.BOTTOM, defaultConstraintLayout.getId(), ConstraintSet.BOTTOM);
        set.connect(tv.getId(), ConstraintSet.RIGHT, defaultConstraintLayout.getId(), ConstraintSet.RIGHT);

        set.applyTo(defaultConstraintLayout);

        return defaultConstraintLayout;
    }

    private ConstraintLayout vehicleCL(ConstraintLayout cl, String vehicleUID, boolean yourVehicles) {
        DocumentReference docRef = firestore.collection("vehicles")
                .document(vehicleUID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        cl.setClickable(true);
                        if (yourVehicles) cl.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
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
                            }
                        });
                        else cl.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
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
                            }
                        });


                        // Image of Car
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

                        // Car Model Name
                        TextView carModel = new TextView(activity);
                        String carModelName = document.getString("model");
                        if (carModelName.length() > 20) {
                            carModelName = carModelName.substring(0, 18) + "...";
                        }
                        carModel.setText(carModelName);
//                        carModel.setPadding(0, dpToPx(15),0,0);
                        textViewFormat(carModel);
                        innerCL.addView(carModel);


                        // Car Model Capacity
                        TextView capacity = new TextView(activity);
                        String capacityStr = String.valueOf(((Long)document.get("capacity")).intValue()
                                - ((ArrayList<String>) document.get("ridersUIDs")).size()) + " seats left";
                        capacity.setText(capacityStr);
                        textViewFormat(capacity);
                        innerCL.addView(capacity);


                        // Price
                        TextView price = new TextView(activity);
                        String priceStr = "$" + ((Double) document.get("basePrice")).toString() + " per ride";
                        price.setText(priceStr);
                        textViewFormat(price);
                        innerCL.addView(price);
//                        set.connect(price.getId(), ConstraintSet.TOP, capacity.getId(), ConstraintSet.BOTTOM);


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
                        Log.d("VEHICLE CL", "No such document");
                    }
                } else {
                    Log.d("VEHICLE CL", "get failed with ", task.getException());
                }
            }
        });

        return cl;
    }



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

    private int dpToPx(int dp) {
        int x = (int) (dp * ((float) activity.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return x;
    }

}