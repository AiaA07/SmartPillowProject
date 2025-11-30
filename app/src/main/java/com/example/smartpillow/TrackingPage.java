package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class TrackingPage extends AppCompatActivity {

    private LinearLayout homeTab;
    private LinearLayout statsTab;
    private LinearLayout trackingTab;
    private LinearLayout profileTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_page);

        // Bottom navigation tabs
        homeTab = findViewById(R.id.home_tab);
        statsTab = findViewById(R.id.stats_tab);
        trackingTab = findViewById(R.id.tracking_tab);
        profileTab = findViewById(R.id.profile_tab);

        // Navigation listeners
        homeTab.setOnClickListener(v -> {
            // If this is your real home screen, use HomePage.class
            Intent i = new Intent(TrackingPage.this, HomePage.class);
            startActivity(i);
            finish();
        });

        statsTab.setOnClickListener(v -> {
            Intent i = new Intent(TrackingPage.this, StatsPage.class);
            startActivity(i);
            finish();
        });

        trackingTab.setOnClickListener(v -> {
            // Already here
        });

        profileTab.setOnClickListener(v -> {
            Intent i = new Intent(TrackingPage.this, ProfilePage.class);
            startActivity(i);
            finish();
        });
    }
}
