package com.example.smartpillow;


import java.util.ArrayList;
import java.util.List;
public class HeartRateCollector {
    private static HeartRateCollector instance;
    private List<Float> heartRates = new ArrayList<>();
    private boolean isTracking = false;
    private float lastHeartRate = 0; // New field for most recent valid reading

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
        lastHeartRate = 0;
    }

    public void addHeartRate(float value) {
        if (isTracking && value > 30 && value < 200) {
            heartRates.add(value);
        }
    }

    public void setLastHeartRate(float value) {
        if (isTracking && value > 30 && value < 200) {
            lastHeartRate = value;
        }
    }

    public float getLastHeartRate() {
        return lastHeartRate;
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
        lastHeartRate = 0;
    }
}