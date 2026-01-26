package com.example.smartpillow;

import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class StatsPage extends AppCompatActivity {

    private ImageView homeBtn2;
    private ImageView trackingBtn2;
    private ImageView profileBtn2;

    private TextView trackingStatusText;
    private TextView sessionTimerText;
    private Button startTrackingBtn;
    private Button stopTrackingBtn;

    private boolean isTracking = false;
    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private DatabaseManager dbManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_page);

        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (SQLException e) {
            Log.e("StatsPage", "Error opening database", e);
            Toast.makeText(this, "Error opening database", Toast.LENGTH_SHORT).show();
        }

        homeBtn2 = findViewById(R.id.home_Btn);
        trackingBtn2 = findViewById(R.id.tracking_Btn);
        profileBtn2 = findViewById(R.id.profile_Btn);

        trackingStatusText = findViewById(R.id.tracking_status_text);
        sessionTimerText = findViewById(R.id.session_timer_text);
        startTrackingBtn = findViewById(R.id.start_tracking_btn);
        stopTrackingBtn = findViewById(R.id.stop_tracking_btn);

        homeBtn2.setOnClickListener(v -> GotoHome2());
        trackingBtn2.setOnClickListener(v -> GoToTracking2());
        profileBtn2.setOnClickListener(v -> GoToProfile2());

        startTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });

        stopTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();
            }
        });
    }

    private void startTracking() {
        isTracking = true;
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        trackingStatusText.setText("TRACKING");
        sessionTimerText.setVisibility(View.VISIBLE);
        startTrackingBtn.setVisibility(View.GONE);
        stopTrackingBtn.setVisibility(View.VISIBLE);
    }

    private void stopTracking() {
        isTracking = false;
        timerHandler.removeCallbacks(timerRunnable);

        long millis = SystemClock.uptimeMillis() - startTime;
        int minutes = (int) (millis / 1000) / 60;

        // TODO: Replace 1 with the actual user ID
        long userId = 1;
        dbManager.updateSleepDuration(userId, minutes);

        trackingStatusText.setText("READY TO TRACK");
        sessionTimerText.setVisibility(View.GONE);
        startTrackingBtn.setVisibility(View.VISIBLE);
        stopTrackingBtn.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
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
}
