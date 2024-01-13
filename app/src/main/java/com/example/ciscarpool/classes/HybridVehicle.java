package com.example.ciscarpool.classes;

import java.util.ArrayList;

public class HybridVehicle extends Vehicle{
    public HybridVehicle() {
    }

    public HybridVehicle(String owner, String model, int capacity, String vehicleID, ArrayList<String> ridersUIDs, boolean open, double basePrice, String image, ArrayList<Double> location) {
        super(owner, model, capacity, vehicleID, ridersUIDs, open, basePrice, image, location);
        this.kiloCarbonSavedPerRidePerKm = 0.262;
    }

}
