package com.example.smartpillow;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryPage extends AppCompatActivity {

    private static final String TAG = "HistoryPage";

    private DatabaseManager dbManager;
    private long currentUserId = -1;

    private BarChart durationChart;
    private LineChart qualityChart;
    private TextView tvAvgDuration, tvAvgQuality, tvTotalSessions, tvImprovementMsg;
    private ImageView homeBtn, statsBtn, trackingBtn, profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_page);

        // Get user ID
        currentUserId = getIntent().getLongExtra("LOCAL_USER_ID", -1);
        if (currentUserId == -1) {
            Log.e(TAG, "No user ID passed - using fallback 1");
            currentUserId = 1;
        }

        // Find views
        durationChart    = findViewById(R.id.durationChart);
        qualityChart     = findViewById(R.id.qualityChart);
        tvAvgDuration    = findViewById(R.id.tvAvgDuration);
        tvAvgQuality     = findViewById(R.id.tvAvgQuality);
        tvTotalSessions  = findViewById(R.id.tvTotalSessions);
        tvImprovementMsg = findViewById(R.id.tvImprovementMsg);

        // Bottom nav
        homeBtn     = findViewById(R.id.homeBtn);
        statsBtn    = findViewById(R.id.statsBtn);
        trackingBtn = findViewById(R.id.trackingBtn);
        profileBtn  = findViewById(R.id.profileBtn);

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

        // Open DB and load data
        dbManager = new DatabaseManager(this);
        dbManager.open();
        loadHistoryData();
    }

    private void loadHistoryData() {
        Cursor cursor = dbManager.fetchUserSessions(currentUserId);

        List<Float> durations = new ArrayList<>();
        List<Float> qualities = new ArrayList<>();
        List<String> labels   = new ArrayList<>();

        int totalDuration = 0;
        int totalQuality  = 0;
        int count         = 0;

        if (cursor != null) {
            // fetchUserSessions returns newest first — we want oldest first for charts
            // So collect all then reverse
            List<int[]> rows = new ArrayList<>();

            while (cursor.moveToNext() && count < 30) {
                int durationIdx  = cursor.getColumnIndex("duration_minutes");
                int qualityIdx   = cursor.getColumnIndex("sleep_quality");
                int timestampIdx = cursor.getColumnIndex("timestamp");

                int duration = durationIdx != -1  ? cursor.getInt(durationIdx)  : 0;
                int quality  = qualityIdx != -1   ? cursor.getInt(qualityIdx)   : 0;
                String timestamp = timestampIdx != -1 ? cursor.getString(timestampIdx) : "";

                rows.add(new int[]{duration, quality});

                // Short label from timestamp (day only e.g. "03-05")
                if (timestamp.length() >= 10) {
                    labels.add(timestamp.substring(5, 10));
                } else {
                    labels.add("Day " + (count + 1));
                }

                totalDuration += duration;
                totalQuality  += quality;
                count++;
            }
            cursor.close();

            // Reverse so oldest is first (left side of chart)
            Collections.reverse(rows);
            Collections.reverse(labels);

            for (int[] row : rows) {
                durations.add(row[0] / 60f); // convert minutes to hours
                qualities.add((float) row[1]);
            }
        }

        if (count == 0) {
            tvAvgDuration.setText("No data yet");
            tvAvgQuality.setText("No data yet");
            tvTotalSessions.setText("0");
            tvImprovementMsg.setText("Start tracking your sleep to see history! 🌙");
            return;
        }

        // Summary stats
        float avgDurHours = (totalDuration / (float) count) / 60f;
        int avgDurMins    = (int) ((avgDurHours % 1) * 60);
        int avgDurH       = (int) avgDurHours;
        tvAvgDuration.setText(avgDurH + "h " + avgDurMins + "m");
        tvAvgQuality.setText(String.format("%.1f / 10", totalQuality / (float) count));
        tvTotalSessions.setText(String.valueOf(count));

        // Improvement message
        if (count >= 2) {
            float firstQuality = qualities.get(0);
            float lastQuality  = qualities.get(qualities.size() - 1);
            if (lastQuality > firstQuality) {
                tvImprovementMsg.setText("Your sleep quality is improving! Keep it up 🌟");
            } else if (lastQuality < firstQuality) {
                tvImprovementMsg.setText("Your sleep has declined recently. Try sleeping earlier 💤");
            } else {
                tvImprovementMsg.setText("Your sleep quality is consistent. Great habit! 😴");
            }
        }

        // Build charts
        buildDurationChart(durations, labels);
        buildQualityChart(qualities, labels);
    }

    private void buildDurationChart(List<Float> durations, List<String> labels) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < durations.size(); i++) {
            entries.add(new BarEntry(i, durations.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Sleep Duration (hrs)");
        dataSet.setColor(Color.parseColor("#4ade80"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        durationChart.setData(barData);
        durationChart.setBackgroundColor(Color.parseColor("#1A1A1A"));
        durationChart.getDescription().setEnabled(false);
        durationChart.getLegend().setEnabled(false);
        durationChart.setDrawGridBackground(false);

        // X axis
        XAxis xAxis = durationChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(9f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // Y axis
        YAxis leftAxis = durationChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(12f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#333333"));
        durationChart.getAxisRight().setEnabled(false);

        // Reference line at 8 hours (recommended)
        com.github.mikephil.charting.components.LimitLine goal =
                new com.github.mikephil.charting.components.LimitLine(8f, "Goal: 8h");
        goal.setLineColor(Color.parseColor("#facc15"));
        goal.setTextColor(Color.parseColor("#facc15"));
        goal.setTextSize(10f);
        goal.enableDashedLine(10f, 5f, 0f);
        leftAxis.addLimitLine(goal);

        durationChart.animateY(800);
        durationChart.invalidate();
    }

    private void buildQualityChart(List<Float> qualities, List<String> labels) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < qualities.size(); i++) {
            entries.add(new Entry(i, qualities.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Sleep Quality");
        dataSet.setColor(Color.parseColor("#a78bfa"));
        dataSet.setCircleColor(Color.parseColor("#a78bfa"));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#a78bfa"));
        dataSet.setFillAlpha(40);

        LineData lineData = new LineData(dataSet);

        qualityChart.setData(lineData);
        qualityChart.setBackgroundColor(Color.parseColor("#1A1A1A"));
        qualityChart.getDescription().setEnabled(false);
        qualityChart.getLegend().setEnabled(false);
        qualityChart.setDrawGridBackground(false);

        // X axis
        XAxis xAxis = qualityChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(9f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // Y axis
        YAxis leftAxis = qualityChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#333333"));
        qualityChart.getAxisRight().setEnabled(false);

        qualityChart.animateX(800);
        qualityChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}