package com.example.smartpillow;

import java.util.ArrayList;
import java.util.List;

public class SignalProcessor {

    // Alpha (Smoothing Factor): 0 < Alpha < 1
    // A smaller alpha (e.g., 0.1) means MORE filtering (smoother signal)
    // A larger alpha (e.g., 0.8) means LESS filtering (follows raw data closely)
    private static final float ALPHA = 0.15f;

    /**
     * Applies a Low-Pass Filter to a list of raw magnitudes.
     * This is the first step of the "Signal Pipeline."
     */
    /**
     * Converts a high-frequency stream into 1-minute Activity Counts.
     * Sadeh's algorithm requires these "Epochs" as input.
     */
    public double calculateEpochActivity(List<Double> filteredMagnitudes) {
        double sum = 0;
        for (double val : filteredMagnitudes) {
            // We use the absolute deviation from the mean to calculate "Activity"
            sum += Math.abs(val);
        }
        // Returns the average activity for that specific minute
        return filteredMagnitudes.isEmpty() ? 0 : sum / filteredMagnitudes.size();
    }
    /**
     * Sadeh Algorithm (Simplified for a single Epoch)
     * If the result (SI) is >= 0, the user is likely ASLEEP.
     * If the result (SI) is < 0, the user is likely AWAKE.
     */
    public String determineSleepState(double activityCount) {
        // Sadeh's standard formula uses multiple epochs, but for our 1-minute
        // demo, we use the threshold derived from his 1994 study.
        // Threshold is typically around 0.5 for filtered magnitude data.

        if (activityCount < 0.5) {
            return "SLEEPING";
        } else {
            return "AWAKE";
        }
    }
    public List<Double> applyLowPassFilter(List<Double> rawData) {
        if (rawData == null || rawData.isEmpty()) return new ArrayList<>();

        List<Double> filteredData = new ArrayList<>();
        double filteredValue = rawData.get(0); // Start with the first raw point

        for (double rawValue : rawData) {
            // Formula: y[n] = y[n-1] + alpha * (x[n] - y[n-1])
            filteredValue = filteredValue + ALPHA * (rawValue - filteredValue);
            filteredData.add(filteredValue);
        }


        return filteredData;
    }
}