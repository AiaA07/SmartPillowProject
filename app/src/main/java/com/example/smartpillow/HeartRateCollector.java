package com.example.smartpillow;


import java.util.ArrayList;
import java.util.List;
public class HeartRateCollector {
    private static HeartRateCollector instance;
    private List<Float> heartRates = new ArrayList<>();
    private boolean isTracking = false;

    private HeartRateCollector() {}

    public static synchronized HeartRateCollector getInstance() {
        if (instance == null) {
            instance = new HeartRateCollector();
        }
        return instance;
    }

    public void startTracking() {
        heartRates.clear();
        isTracking = true;
    }

    public void addHeartRate(float value) {
        if (isTracking && value > 30 && value < 200) { // plausibility check
            heartRates.add(value);
        }
    }

    public float getAverageHeartRate() {
        if (heartRates.isEmpty()) return 0;
        float sum = 0;
        for (float hr : heartRates) sum += hr;
        return sum / heartRates.size();
    }

    public void stopTracking() {
        isTracking = false;
    }

    public void reset() {
        heartRates.clear();
        isTracking = false;
    }

}
