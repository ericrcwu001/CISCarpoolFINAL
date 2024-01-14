package com.example.ciscarpool.classes;
import java.util.ArrayList;

/**
 * {@link User} is a class to represent a user's relevant information.
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class User {
    private static String currentUser = null;
    private String uId;
    private String firstName;
    private String lastName;
    private String email;
    private String userType;
    private ArrayList<String> ownedVehicles;
    private String uriString;
    private ArrayList<String> reservedRides;
    private double kiloCarbonSaved;

    public User() {}

    public User(String uId, String firstName, String lastName, String email, String userType, ArrayList<String> ownedVehicles, String uriString, ArrayList<String> reservedRides, double kiloCarbonSaved) {
        this.uId = uId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userType = userType;
        this.ownedVehicles = ownedVehicles;
        this.uriString = uriString;
        this.reservedRides = reservedRides;
        this.kiloCarbonSaved = kiloCarbonSaved;
    }

    // Getters and Setters
    public static String getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(String currentUser) {
        User.currentUser = currentUser;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public ArrayList<String> getOwnedVehicles() {
        return ownedVehicles;
    }

    public void setOwnedVehicles(ArrayList<String> ownedVehicles) {
        this.ownedVehicles = ownedVehicles;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public ArrayList<String> getReservedRides() {
        return reservedRides;
    }

    public void setReservedRides(ArrayList<String> reservedRides) {
        this.reservedRides = reservedRides;
    }

    public double getKiloCarbonSaved() {
        return kiloCarbonSaved;
    }

    public void setKiloCarbonSaved(double kiloCarbonSaved) {
        this.kiloCarbonSaved = kiloCarbonSaved;
    }
}
