package com.example.smartpillow;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;


public class sensor extends AppCompatActivity implements SensorEventListener {


    private ImageView homeBtn3;
    private ImageView statsBtn3;
    private ImageView profileBtn3;
    private static final String TAG = "SensorActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Sensor heartRateSensor;
    private Sensor gyroscope;

    private long lastUpdate = 0L;
    private static final long UPDATE_INTERVAL_MS = 200L;

    private TextView tvX;
    private TextView tvY;
    private TextView tvZ;
    private TextView tvHeartRate;
    private TextView tvGyroX;
    private TextView tvGyroY;
    private TextView tvGyroZ;

    private static final int PERMISSION_REQUEST_BODY_SENSORS = 100;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 101;  // ← NEW: Microphone permission code

    private DatabaseManager dbManager;
    private long currentSessionId = -1;

    private Microphone microphone;  // ← NEW: Microphone instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tracking_page);

        homeBtn3 = findViewById(R.id.home3_Btn);
        statsBtn3 = findViewById(R.id.stats3_Btn);
        profileBtn3 = findViewById(R.id.profile3_Btn);

        homeBtn3.setOnClickListener(v -> GotoHome3());
        statsBtn3.setOnClickListener(v -> GoToStats3());
        profileBtn3.setOnClickListener(v -> GoToProfile3());

        tvX = findViewById(R.id.TexttvX);
        tvY = findViewById(R.id.TexttvY);
        tvZ = findViewById(R.id.TexttvZ);
        tvHeartRate = findViewById(R.id.tvHeartRate);
        tvGyroX = findViewById(R.id.tvGyroX);
        tvGyroY = findViewById(R.id.tvGyroY);
        tvGyroZ = findViewById(R.id.tvGyroZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        if (accelerometer == null) {
            Log.w(TAG, "No accelerometer sensor found on this device.");
        }

        if (heartRateSensor == null) {
            Log.w(TAG, "No heart rate sensor found on this device.");
            tvHeartRate.setText("Heart Rate: not available");
        }

        if (gyroscope == null) {
            Log.w(TAG, "No gyroscope sensor found on this device.");
            tvGyroX.setText("Gyro X: not available");
            tvGyroY.setText("Gyro Y: not available");
            tvGyroZ.setText("Gyro Z: not available");
        }

        dbManager = new DatabaseManager(this);
        dbManager.open();
        currentSessionId = 1;

        // Request permission for heart rate sensor (required for Android 6+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    PERMISSION_REQUEST_BODY_SENSORS);
        } else {
            registerSensors();
        }

        // ← NEW: Request microphone permission with privacy popup
        requestMicrophonePermission();
    }

    // ← NEW: Shows privacy explanation dialog before requesting microphone permission
    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Show privacy explanation dialog BEFORE the system permission prompt
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Microphone Access")
                    .setMessage("SmartPillow uses the microphone to detect ambient noise levels " +
                            "during sleep (e.g., snoring, room noise).\n\n" +
                            "We do NOT record or store any audio — only decibel levels are saved.\n\n" +
                            "You can deny this permission and still use other sensors.")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        // User agreed, now show the system permission dialog
                        ActivityCompat.requestPermissions(sensor.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_REQUEST_RECORD_AUDIO);
                    })
                    .setNegativeButton("Deny", (dialog, which) -> {
                        Toast.makeText(this, "Microphone permission denied. " +
                                "Noise tracking will be unavailable.", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            // Permission already granted, start microphone
            startMicrophone();
        }
    }

    // ← NEW: Starts the microphone for decibel tracking
    private void startMicrophone() {
        microphone = new Microphone(this);
        if (microphone.start()) {
            Log.d(TAG, "Microphone started successfully");
        } else {
            Log.w(TAG, "Failed to start microphone");
        }
    }

    // UPDATED: Now handles BOTH body sensor AND microphone permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerSensors();
            } else {
                Toast.makeText(this, "Heart rate permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        // ← NEW: Handle microphone permission result
        else if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMicrophone();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerSensors() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (heartRateSensor != null &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // UPDATED: Now also cleans up microphone
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        // ← NEW: Release microphone when leaving activity
        if (microphone != null) {
            microphone.release();
            microphone = null;
        }
        if (dbManager != null) dbManager.close();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.d(TAG, "onSensorChanged: type=" + event.sensor.getType());

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            tvX.setText("X: " + x);
            tvY.setText("Y: " + y);
            tvZ.setText("Z: " + z);

            float minX = -10;
            float maxX = 10;
            if (x >= minX && x <= maxX)
                tvX.setText("X: " + x + " (Normal)");
            else
                tvX.setText("X: " + x + " (Abnormal)");

            if ((currentTime - lastUpdate) >= UPDATE_INTERVAL_MS) {
                lastUpdate = currentTime;
                Log.d(TAG, "Accelerometer: X=" + x + " Y=" + y + " Z=" + z);
            }

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            if (currentSessionId != -1) {
                dbManager.saveRawSensorData(currentSessionId, 1, magnitude);
            }
        }
        else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float heartRate = event.values[0];
            int accuracy = event.accuracy;

            tvHeartRate.setText("Heart Rate: " + heartRate + " bpm");
            Log.d(TAG, "Heart rate: " + heartRate + " bpm (accuracy=" + accuracy + ")");

            if (heartRate > 30 && heartRate < 200 && accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                HeartRateCollector.getInstance().addHeartRate(heartRate);
            }
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float gyroX = event.values[0];
            float gyroY = event.values[1];
            float gyroZ = event.values[2];

            tvGyroX.setText("Gyro X: " + gyroX + " rad/s");
            tvGyroY.setText("Gyro Y: " + gyroY + " rad/s");
            tvGyroZ.setText("Gyro Z: " + gyroZ + " rad/s");
            Log.d(TAG, "Gyroscope: X=" + gyroX + " Y=" + gyroY + " Z=" + gyroZ);

            double magnitude = Math.sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ);
            if (currentSessionId != -1) {
                dbManager.saveRawSensorData(currentSessionId, 2, magnitude);
            }
        }
    }

    private void GotoHome3(){
        Intent home3 =  new Intent(sensor.this, HomePage.class);
        startActivity(home3);
    }

    private void GoToStats3(){
        Intent stats3 = new Intent(sensor.this, StatsPage.class);
        startActivity(stats3);
    }

    private void GoToProfile3(){
        Intent profile3 = new Intent(sensor.this, ProfilePage.class);
        startActivity(profile3);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}