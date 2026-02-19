package com.example.smartpillow;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class StatsPage extends AppCompatActivity {

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

    // Timer runnable
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = SystemClock.uptimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            sessionTimerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    // Live heart rate updater
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
                hrHandler.postDelayed(this, 1000); // update every second
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_page);

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
    }

    private void startTracking() {
        isTracking = true;
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        // Reset and start heart rate collector
        HeartRateCollector.getInstance().reset();
        HeartRateCollector.getInstance().startTracking();

        // Start live heart rate updates
        hrHandler.post(hrRunnable);

        // Update UI
        trackingStatusText.setText("TRACKING");
        sessionTimerText.setVisibility(View.VISIBLE);
        startTrackingBtn.setVisibility(View.GONE);
        stopTrackingBtn.setVisibility(View.VISIBLE);
    }

    private void stopTracking() {
        isTracking = false;
        timerHandler.removeCallbacks(timerRunnable);
        hrHandler.removeCallbacks(hrRunnable); // stop live heart rate updates

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
            Toast.makeText(this, "Session Saved! Duration: " + minutes + " mins", Toast.LENGTH_LONG).show();

            //Sync to Firebase if user is logged in
            String firebaseUid = getFirebaseUid();
            if (firebaseUid != null) {
                // Compute score (same logic as in DatabaseManager)
                int score = dbManager.calculateScoreLogic(minutes, simulatedQuality);
                dbManager.syncSingleSessionToFirebase(sessionId, currentUserId, minutes,
                        simulatedQuality, score, avgHeartRate, firebaseUid);
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

    /**
     * Query the database for the most recent sleep session and update the summary cards.
     */
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
            // No sessions yet
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
        if (dbManager != null) {
            dbManager.close();
        }
    }

    // Navigation methods
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
}