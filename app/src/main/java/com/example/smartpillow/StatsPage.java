package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class StatsPage extends AppCompatActivity {

    private LinearLayout homeTab;
    private LinearLayout statsTab;
    private LinearLayout trackingTab;
    private LinearLayout profileTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_page);

        // Bottom navigation tabs
        homeTab = findViewById(R.id.home_tab);
        statsTab = findViewById(R.id.stats_tab);
        trackingTab = findViewById(R.id.tracking_tab);
        profileTab = findViewById(R.id.profile_tab);

        // Navigation listeners
        homeTab.setOnClickListener(v -> {
            Intent i = new Intent(StatsPage.this, HomePage.class);
            startActivity(i);
            finish();
        });

        statsTab.setOnClickListener(v -> {
            // Already here
        });

        trackingTab.setOnClickListener(v -> {
            Intent i = new Intent(StatsPage.this, TrackingPage.class);
            startActivity(i);
            finish();
        });

        profileTab.setOnClickListener(v -> {
            Intent i = new Intent(StatsPage.this, ProfilePage.class);
            startActivity(i);
            finish();
        });
    }
}
