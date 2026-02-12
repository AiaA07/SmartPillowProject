package com.example.smartpillow;

import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class ProcessingService {

    private final DatabaseManager dbManager;
    private final SignalProcessor signalProcessor;

    public ProcessingService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.signalProcessor = new SignalProcessor();
    }

    /**
     * The Pipeline: Fetch -> Filter -> Epoch -> Sadeh
     */
    public void processNightlyData(long sessionId) {
        // 1. Fetching data from SQLite
        List<Double> rawData = fetchRawMagnitudes(sessionId);

        if (rawData.isEmpty()) {
            Log.w("Pipeline", "No data found for session: " + sessionId);
            return;
        }

        // 2. Apply the Butterworth/Low-Pass Filter (Remove "Chaff")
        // This smooths the mattress damping/noise
        List<Double> cleanData = signalProcessor.applyLowPassFilter(rawData);

        // 3. Roll up into Epoch Activity
        // This calculates the average movement intensity for the window
        double finalActivityCount = signalProcessor.calculateEpochActivity(cleanData);

        // 4. Sadeh Determination
        // Turns the math into a human-readable state (Sleep vs Wake)
        String sleepState = signalProcessor.determineSleepState(finalActivityCount);

        // 5. Output results
        Log.i("Pipeline", "SUCCESS: Processed " + rawData.size() + " data points.");
        Log.i("Pipeline", "Final Activity Count: " + finalActivityCount);
        Log.i("Pipeline", "SADEH DETERMINATION: User is " + sleepState);

        dbManager.deleteRawDataForSession(sessionId);
        Log.i("Pipeline", "Raw data purged to save device storage. ");
    }

    private List<Double> fetchRawMagnitudes(long sessionId) {
        List<Double> magnitudes = new ArrayList<>();
        // Note: Ensure your dbManager has the fetchRawDataForSession method we discussed!
        Cursor cursor = dbManager.fetchRawDataForSession(sessionId);

        if (cursor != null) {
            int magIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_RAW_MAGNITUDE);
            if (magIndex != -1) {
                while (cursor.moveToNext()) {
                    magnitudes.add(cursor.getDouble(magIndex));
                }
            }
            cursor.close();
        }
        return magnitudes;
    }
}