package com.example.smartpillow;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReportActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private TextView tvSleepState, tvActivityScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        tvSleepState = findViewById(R.id.tvSleepState);
        tvActivityScore = findViewById(R.id.tvActivityScore);

        dbManager = new DatabaseManager(this);
        dbManager.open();

        displayLatestResults();
    }

    private void displayLatestResults() {
        // For the demo, we assume User ID 1
        Cursor cursor = dbManager.fetchLatestSession(1);

        if (cursor != null && cursor.moveToFirst()) {
            // Get the score and quality we calculated in our pipeline
            int score = cursor.getInt(cursor.getColumnIndex("sleep_score"));
            int quality = cursor.getInt(cursor.getColumnIndex("sleep_quality"));

            // We can also re-run the logic or fetch the specific Sadeh status
            tvActivityScore.setText("Signal Strength: " + score);

            if (score < 50) { // Using our Sadeh threshold logic
                tvSleepState.setText("SLEEPING");
            } else {
                tvSleepState.setText("AWAKE");
            }
            cursor.close();
        }
    }
}