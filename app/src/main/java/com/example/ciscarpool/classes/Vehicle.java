package com.example.ciscarpool.classes;

import java.util.ArrayList;

public class Vehicle {
    protected String owner; // Value is User's uID
    protected String model;
    protected int capacity;
    protected String vehicleID; // randomly assigned --> same as the document title in firebase
    protected ArrayList<String> ridersUIDs;
    protected boolean open;
    protected double basePrice;
    protected String image;

    protected ArrayList<Double> location;
    protected double kiloCarbonSavedPerRidePerKm;

    public Vehicle() {
    }

    public Vehicle(String owner, String model, int capacity, String vehicleID, ArrayList<String> ridersUIDs, boolean open, double basePrice, String image, ArrayList<Double> location) {
        this.owner = owner;
        this.model = model;
        this.capacity = capacity;
        this.vehicleID = vehicleID;
        this.ridersUIDs = ridersUIDs;
        this.open = open;
        this.basePrice = basePrice;
        this.image = image;
        this.location = location;
    }

    public Vehicle(String owner, String model, int capacity, String vehicleID, ArrayList<String> ridersUIDs, boolean open, double basePrice, String image, ArrayList<Double> location, double kiloCarbonSavedPerRidePerKm) {
        this.owner = owner;
        this.model = model;
        this.capacity = capacity;
        this.vehicleID = vehicleID;
        this.ridersUIDs = ridersUIDs;
        this.open = open;
        this.basePrice = basePrice;
        this.image = image;
        this.location = location;
        this.kiloCarbonSavedPerRidePerKm = kiloCarbonSavedPerRidePerKm;
    }

    public ArrayList<Double> getLocation() {
        return location;
    }

    public void setLocation(ArrayList<Double> location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public ArrayList<String> getRidersUIDs() {
        return ridersUIDs;
    }

    public void setRidersUIDs(ArrayList<String> ridersUIDs) {
        this.ridersUIDs = ridersUIDs;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
    public double getKiloCarbonSavedPerRidePerKm() {
        return kiloCarbonSavedPerRidePerKm;
    }
    public void setKiloCarbonSavedPerRidePerKm(double kiloCarbonSavedPerRidePerKm) {
        this.kiloCarbonSavedPerRidePerKm = kiloCarbonSavedPerRidePerKm;
    }
}
