package com.example.smartpillow.watch2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.ComponentActivity; // Lighter for Wear OS than AppCompat

public class Sensors extends ComponentActivity {

    private Button startBtn, stopBtn;
    private TextView hrText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Connects to your FrameLayout XML
        setContentView(R.layout.sensor_page);

        // Initialize your UI elements
        hrText = findViewById(R.id.HeartRateText);
        startBtn = findViewById(R.id.start_tracking_btn);
        stopBtn = findViewById(R.id.stop_tracking_btn);

        // Simple logic for the demo
        startBtn.setOnClickListener(v -> {
            startBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            hrText.setText("HR: 72 bpm"); // Mock data for the meeting
        });

        stopBtn.setOnClickListener(v -> {
            stopBtn.setVisibility(View.GONE);
            startBtn.setVisibility(View.VISIBLE);
            hrText.setText("HR: -- bpm");
        });
    }
}