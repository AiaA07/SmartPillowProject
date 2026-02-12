package com.example.smartpillow;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class sensor extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "SensorActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private DatabaseManager dbManager;
    private long currentSessionId = -1;

    private long lastUpdate = 0L;
    private static final long UPDATE_INTERVAL_MS = 200L;

    private TextView tvX;
    private TextView tvY;
    private TextView tvZ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tracking_page);

        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);

        // 1. Initialize and Open DB first
        dbManager = new DatabaseManager(this);
        dbManager.open();

        //
        currentSessionId = dbManager.insertSleepSession(1, 0, 0);

        // 3. NOW simulate the data using that ID
        //dbManager.simulateSleepData(currentSessionId);

        // 4. Trigger the Processing Service to prove the pipeline works
        ProcessingService processingService = new ProcessingService(dbManager);
        processingService.processNightlyData(currentSessionId);

        //
        tvX = findViewById(R.id.TexttvX);
        tvY = findViewById(R.id.TexttvY);
        tvZ = findViewById(R.id.TexttvZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Log.w(TAG, "No accelerometer sensor found on this device.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdate) > UPDATE_INTERVAL_MS) {
            lastUpdate = currentTime;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            double netAccel = Math.abs(magnitude - 9.81);


            // This sends the clean magnitude to our SQLite staging table
            if (currentSessionId != -1) {
                dbManager.saveRawSensorData(currentSessionId, 1, netAccel);
            }

            // UI Updates
            tvX.setText(String.format("X: %.2f", x));
            tvY.setText(String.format("Y: %.2f", y));
            tvZ.setText(String.format("Net: %.2f", netAccel));

            Log.i("RAW_DATA_EXPORT", currentTime + "," + x + "," + y + "," + z + " | Net: " + netAccel);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}