package com.example.ciscarpool.interfaces;

/**
 * {@link VehicleOwnerNameCallback} is an interface to wait for Firebase Firestore response for the
 * {@link com.example.ciscarpool.adapters.RidesAdapter}
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public interface VehicleOwnerNameCallback {
    void vehicleOwnerNameCallback(String fullName);
}
