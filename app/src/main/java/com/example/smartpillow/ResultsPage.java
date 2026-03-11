package com.example.smartpillow;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsPage extends AppCompatActivity {

    private static final String TAG = "ResultsPage";

    private DatabaseManager dbManager;
    private TextView tvScore, tvScoreLabel, tvDuration, tvHeartRate, tvQuality, tvDate;
    private Button btnViewHistory, btnHome;
    private ImageView homeBtn, statsBtn, trackingBtn, profileBtn;

    private long currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_page);

        // Get user ID passed from StatsPage
        currentUserId = getIntent().getLongExtra("LOCAL_USER_ID", -1);
        if (currentUserId == -1) {
            Log.e(TAG, "No user ID passed - using fallback 1");
            currentUserId = 1;
        }

        // Find views
        tvScore        = findViewById(R.id.tvScore);
        tvScoreLabel   = findViewById(R.id.tvScoreLabel);
        tvDuration     = findViewById(R.id.tvDuration);
        tvHeartRate    = findViewById(R.id.tvHeartRate);
        tvQuality      = findViewById(R.id.tvQuality);
        tvDate         = findViewById(R.id.tvDate);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnHome        = findViewById(R.id.btnHome);

        // Bottom nav
        homeBtn     = findViewById(R.id.homeBtn);
        statsBtn    = findViewById(R.id.statsBtn);
        trackingBtn = findViewById(R.id.trackingBtn);
        profileBtn  = findViewById(R.id.profileBtn);

        // Bottom nav listeners
        homeBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HomePage.class)));
        statsBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, StatsPage.class);
            i.putExtra("LOCAL_USER_ID", currentUserId);
            startActivity(i);
        });
        trackingBtn.setOnClickListener(v ->
                startActivity(new Intent(this, sensor.class)));
        profileBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ProfilePage.class)));

        // View History button
        btnViewHistory.setOnClickListener(v -> {
            Intent i = new Intent(this, HistoryPage.class);
            i.putExtra("LOCAL_USER_ID", currentUserId);
            startActivity(i);
        });

        // Back to Home button
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomePage.class)));

        // Open DB and load data
        dbManager = new DatabaseManager(this);
        dbManager.open();
        loadLatestSession();
    }

    private void loadLatestSession() {
        Cursor cursor = dbManager.fetchLatestSession(currentUserId);

        if (cursor != null && cursor.moveToFirst()) {
            int scoreIdx     = cursor.getColumnIndex("sleep_score");
            int durationIdx  = cursor.getColumnIndex("duration_minutes");
            int hrIdx        = cursor.getColumnIndex("avg_heart_rate");
            int qualityIdx   = cursor.getColumnIndex("sleep_quality");
            int timestampIdx = cursor.getColumnIndex("timestamp");

            int score     = scoreIdx != -1    ? cursor.getInt(scoreIdx)    : 0;
            int duration  = durationIdx != -1 ? cursor.getInt(durationIdx) : 0;
            float hr      = hrIdx != -1       ? cursor.getFloat(hrIdx)     : 0;
            int quality   = qualityIdx != -1  ? cursor.getInt(qualityIdx)  : 0;
            String timestamp = timestampIdx != -1 ? cursor.getString(timestampIdx) : "N/A";

            // Score with color coding
            tvScore.setText(String.valueOf(score));
            if (score >= 75) {
                tvScore.setTextColor(Color.parseColor("#4ade80"));
                tvScoreLabel.setText("Great Sleep! 😴");
            } else if (score >= 50) {
                tvScore.setTextColor(Color.parseColor("#facc15"));
                tvScoreLabel.setText("Fair Sleep 😐");
            } else {
                tvScore.setTextColor(Color.parseColor("#f87171"));
                tvScoreLabel.setText("Poor Sleep 😟");
            }

            // Duration in hours and minutes
            int hours = duration / 60;
            int mins  = duration % 60;
            tvDuration.setText(hours + "h " + mins + "m");

            // Heart rate
            tvHeartRate.setText(String.format("%.0f bpm", hr));

            // Quality out of 10
            tvQuality.setText(quality + "/10");

            // Date
            if (timestamp.length() >= 10) {
                tvDate.setText(timestamp.substring(0, 10));
            } else {
                tvDate.setText(timestamp);
            }

            cursor.close();
        } else {
            tvScore.setText("--");
            tvScoreLabel.setText("No session recorded");
            tvDuration.setText("--");
            tvHeartRate.setText("-- bpm");
            tvQuality.setText("--");
            tvDate.setText("");
            Toast.makeText(this, "No session data found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}