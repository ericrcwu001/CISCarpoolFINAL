package com.example.ciscarpool.interfaces;

import com.example.ciscarpool.classes.Vehicle;
import java.util.ArrayList;

/**
 * {@link RidesListCallback} is an interface to wait for Firebase Firestore response for the
 * {@link com.example.ciscarpool.fragments.CarpoolFragment}
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public interface RidesListCallback {
    void ridesListCallback(ArrayList<Vehicle> result);
}
