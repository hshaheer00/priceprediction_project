package com.example.prctc;

import com.google.firebase.database.Exclude;

public class PredictionRecord {
    public String productType;
    public String details;
    public String predictedPrice;
    public long timestamp;
    
    @Exclude
    public String key; // Unique key from Firebase for deletion

    public PredictionRecord() {
        // Default constructor for Firebase
    }

    public PredictionRecord(String productType, String details, String predictedPrice, long timestamp) {
        this.productType = productType;
        this.details = details;
        this.predictedPrice = predictedPrice;
        this.timestamp = timestamp;
    }
}
