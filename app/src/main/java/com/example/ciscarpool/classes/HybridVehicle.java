package com.example.ciscarpool.classes;

import java.util.ArrayList;

public class HybridVehicle extends Vehicle{
    private int carbonSavedPerCarpool;

    public HybridVehicle() {
    }

    public HybridVehicle(String owner, String model, int capacity, String vehicleID, ArrayList<String> ridersUIDs, boolean open, double basePrice, String image) {
        super(owner, model, capacity, vehicleID, ridersUIDs, open, basePrice, image);
        this.carbonSavedPerCarpool = 4;
    }

    public int getCarbonSavedPerCarpool() {
        return carbonSavedPerCarpool;
    }

    public void setCarbonSavedPerCarpool(int carbonSavedPerCarpool) {
        this.carbonSavedPerCarpool = carbonSavedPerCarpool;
    }
}
