package com.example.smartpillow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class StatsPage extends AppCompatActivity implements SensorEventListener {

    // UI elements
    private ImageView homeBtn2, trackingBtn2, profileBtn2;
    private TextView trackingStatusText, sessionTimerText;
    private Button startTrackingBtn, stopTrackingBtn;
    private TextView sleepDurationText, sleepQualityText, heartRateText, recentDataText;

    // Tracking state
    private boolean isTracking = false;
    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private Handler hrHandler = new Handler(); // For live heart rate updates

    // Database
    private DatabaseManager dbManager;

    // TODO: Replace with actual logged-in user ID
    private long currentUserId = 1;

    // Sensor related
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private static final int PERMISSION_REQUEST_BODY_SENSORS = 100;

    // Timer runnable (HH:MM:SS format)
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = SystemClock.uptimeMillis() - startTime;
            int totalSeconds = (int) (millis / 1000);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            sessionTimerText.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    // Live heart rate updater (now just updates from collector)
    private Runnable hrRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTracking) {
                float hr = HeartRateCollector.getInstance().getLastHeartRate();
                if (hr > 0) {
                    heartRateText.setText(String.format("%.0f bpm", hr));
                } else {
                    heartRateText.setText("-- bpm");
                }
                hrHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_page);

        // Initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        }

        // Initialize database
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (SQLException e) {
            Log.e("StatsPage", "Error opening database", e);
            Toast.makeText(this, "Error opening database", Toast.LENGTH_SHORT).show();
        }

        // Find views
        homeBtn2 = findViewById(R.id.home_Btn);
        trackingBtn2 = findViewById(R.id.tracking_Btn);
        profileBtn2 = findViewById(R.id.profile_Btn);
        trackingStatusText = findViewById(R.id.tracking_status_text);
        sessionTimerText = findViewById(R.id.session_timer_text);
        startTrackingBtn = findViewById(R.id.start_tracking_btn);
        stopTrackingBtn = findViewById(R.id.stop_tracking_btn);
        sleepDurationText = findViewById(R.id.sleep_duration_text);
        sleepQualityText = findViewById(R.id.sleep_quality_text);
        heartRateText = findViewById(R.id.heart_rate_text);
        recentDataText = findViewById(R.id.recent_data_text);

        // Set click listeners
        homeBtn2.setOnClickListener(v -> GotoHome2());
        trackingBtn2.setOnClickListener(v -> GoToTracking2());
        profileBtn2.setOnClickListener(v -> GoToProfile2());
        startTrackingBtn.setOnClickListener(v -> startTracking());
        stopTrackingBtn.setOnClickListener(v -> stopTracking());

        // Load the most recent session (if any)
        loadLatestSessionData();

        // Check for heart rate sensor
        if (heartRateSensor == null) {
            Toast.makeText(this, "Heart rate sensor not available", Toast.LENGTH_LONG).show();
        }
    }

    private void startTracking() {
        // Request permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    PERMISSION_REQUEST_BODY_SENSORS);
            return; // Wait for permission result
        }

        // Permission already granted, start tracking
        doStartTracking();
    }

    private void doStartTracking() {
        isTracking = true;
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        // Reset and start heart rate collector
        HeartRateCollector.getInstance().reset();
        HeartRateCollector.getInstance().startTracking();

        // Register heart rate sensor listener
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Start live heart rate updates
        hrHandler.post(hrRunnable);

        // Update UI
        trackingStatusText.setText("TRACKING");
        sessionTimerText.setVisibility(View.VISIBLE);
        startTrackingBtn.setVisibility(View.GONE);
        stopTrackingBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStartTracking();
            } else {
                Toast.makeText(this, "Heart rate permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopTracking() {
        isTracking = false;
        timerHandler.removeCallbacks(timerRunnable);
        hrHandler.removeCallbacks(hrRunnable);

        // Unregister heart rate sensor
        sensorManager.unregisterListener(this);

        // Calculate duration
        long millis = SystemClock.uptimeMillis() - startTime;
        int minutes = (int) (millis / 1000) / 60;

        // Stop collector and get average heart rate
        HeartRateCollector.getInstance().stopTracking();
        float avgHeartRate = HeartRateCollector.getInstance().getAverageHeartRate();

        int simulatedQuality = 7; // Replace with real calculation later

        // Save session to database
        long sessionId = dbManager.insertSleepSession(currentUserId, minutes, simulatedQuality, avgHeartRate);

        if (sessionId != -1) {
            Toast.makeText(this, "Session Saved! Duration: " + minutes + " mins, Avg HR: " + avgHeartRate, Toast.LENGTH_LONG).show();

            // Sync to Firebase if user is logged in
            String firebaseUid = getFirebaseUid();
            Log.d("StatsPage", "Firebase UID: " + firebaseUid);
            if (firebaseUid != null) {
                int score = dbManager.calculateScoreLogic(minutes, simulatedQuality);
                dbManager.syncSingleSessionToFirebase(sessionId, currentUserId, minutes,
                        simulatedQuality, score, avgHeartRate, firebaseUid);
            } else {
                Log.e("StatsPage", "User not logged in – cannot sync to Firebase");
            }


            // Refresh the display with the new session data
            loadLatestSessionData();
        }

        // Reset UI
        trackingStatusText.setText("READY TO TRACK");
        sessionTimerText.setVisibility(View.GONE);
        startTrackingBtn.setVisibility(View.VISIBLE);
        stopTrackingBtn.setVisibility(View.GONE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float heartRate = event.values[0];
            int accuracy = event.accuracy;

            Log.d(TAG, "Heart rate raw: " + heartRate + ", accuracy=" + accuracy);

            // Add to collector if plausible (ignore accuracy for now)
            if (heartRate > 30 && heartRate < 200) {
                HeartRateCollector.getInstance().addHeartRate(heartRate);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private void loadLatestSessionData() {
        Cursor cursor = dbManager.fetchLatestSession(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            int durationIndex = cursor.getColumnIndex("duration_minutes");
            int qualityIndex = cursor.getColumnIndex("sleep_quality");
            int hrIndex = cursor.getColumnIndex("avg_heart_rate");
            int timestampIndex = cursor.getColumnIndex("timestamp");

            if (durationIndex != -1) {
                int minutes = cursor.getInt(durationIndex);
                int hours = minutes / 60;
                int mins = minutes % 60;
                sleepDurationText.setText(String.format("%dh %dm", hours, mins));
            }
            if (qualityIndex != -1) {
                int quality = cursor.getInt(qualityIndex);
                sleepQualityText.setText(quality + "/10");
            }
            if (hrIndex != -1) {
                float hr = cursor.getFloat(hrIndex);
                heartRateText.setText(String.format("%.0f bpm", hr));
            }
            if (timestampIndex != -1) {
                String timestamp = cursor.getString(timestampIndex);
                recentDataText.setText("Last session: " + timestamp);
            }
            cursor.close();
        } else {
            sleepDurationText.setText("--");
            sleepQualityText.setText("--");
            heartRateText.setText("-- bpm");
            recentDataText.setText("No sleep data yet");
        }
    }

    private String getFirebaseUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        hrHandler.removeCallbacks(hrRunnable);
        sensorManager.unregisterListener(this);
        if (dbManager != null) {
            dbManager.close();
        }
    }

    private void GotoHome2() {
        Intent home2 = new Intent(StatsPage.this, HomePage.class);
        startActivity(home2);
    }

    private void GoToTracking2() {
        Intent track2 = new Intent(StatsPage.this, sensor.class);
        startActivity(track2);
    }

    private void GoToProfile2() {
        Intent profile2 = new Intent(StatsPage.this, ProfilePage.class);
        startActivity(profile2);
    }

    private static final String TAG = "StatsPage";
}