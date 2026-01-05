package com.example.prctc;

import com.google.firebase.database.Exclude;

public class PredictionRecord {

    public String productType;
    public String details;
    public String predictedPrice;
    public long timestamp;

    @Exclude
    public String key; // Unique key from Firebase

    // Default constructor for Firebase
    public PredictionRecord() { }

    // Constructor with values
    public PredictionRecord(String productType, String details,
                            String predictedPrice, long timestamp) {
        this.productType = productType;
        this.details = details;
        this.predictedPrice = predictedPrice;
        this.timestamp = timestamp;
    }
}
