package com.example.smartpillow;

import android.content.Context;
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
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        long currentTime = System.currentTimeMillis();

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Update UI
        tvX.setText("X: " + x);
        tvY.setText("Y: " + y);
        tvZ.setText("Z: " + z);

        //Sleep metric implementation
        float minX = -10;
        float maxX = 10;

        if(x >= minX && x <= maxX)
            tvX.setText("X: " + x + " (Normal)");
        else
            tvX.setText("X: " + x + " (Abnormal)");


        // Optional throttling if you plan future logic
        if ((currentTime - lastUpdate) >= UPDATE_INTERVAL_MS) {
            lastUpdate = currentTime;

            Log.d(TAG, "Accelerometer: X=" + x + " Y=" + y + " Z=" + z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}