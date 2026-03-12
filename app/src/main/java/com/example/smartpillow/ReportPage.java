package com.example.smartpillow;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReportPage extends AppCompatActivity {

    private DatabaseManager dbManager;
    private TextView tvReportSummary;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_page);

        tvReportSummary = findViewById(R.id.tvReportSummary);
        currentUserId = getIntent().getLongExtra("LOCAL_USER_ID", 1);

        dbManager = new DatabaseManager(this);
        dbManager.open();

        loadReportData();
    }

    private void loadReportData() {
        Cursor cursor = dbManager.fetchLatestSession(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            int score = cursor.getInt(cursor.getColumnIndexOrThrow("sleep_score"));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes"));

            String report = "Based on your last session, your sleep score was " + score + ". ";
            if (score < 50) {
                report += "You moved frequently. Try to limit screen time before bed.";
            } else {
                report += "Great stability! Keep maintaining your current routine.";
            }
            tvReportSummary.setText(report);
            cursor.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }
}