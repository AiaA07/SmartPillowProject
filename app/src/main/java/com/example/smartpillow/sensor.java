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
//import androidx.cor1e.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest; //Added(for ppg)
import android.content.pm.PackageManager; //Added(for ppg)
import android.widget.Toast;


public class sensor extends AppCompatActivity implements SensorEventListener {


    private ImageView homeBtn3;
    private ImageView statsBtn3;
    private ImageView profileBtn3;
    private static final String TAG = "SensorActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Sensor heartRateSensor; //Added(for ppg)
    private Sensor gyroscope; //Added(for gyroscope)

    private long lastUpdate = 0L;
    private static final long UPDATE_INTERVAL_MS = 200L;

    private TextView tvX;
    private TextView tvY;
    private TextView tvZ;
    private TextView tvHeartRate; //Added(for ppg)
    private TextView tvGyroX; //Added(for gyroscope)
    private TextView tvGyroY; //Added(for gyroscope)
    private TextView tvGyroZ; //Added(for gyroscope)

    private static final int PERMISSION_REQUEST_BODY_SENSORS = 100; //Added(for ppg)

    private DatabaseManager dbManager; //Added(for gyroscope)
    private long currentSessionId = -1; //Added(for gyroscope)

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
        tvHeartRate = findViewById(R.id.tvHeartRate); //Added(for ppg)
        tvGyroX = findViewById(R.id.tvGyroX); //Added(for gyroscope)
        tvGyroY = findViewById(R.id.tvGyroY); //Added(for gyroscope)
        tvGyroZ = findViewById(R.id.tvGyroZ); //Added(for gyroscope)

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE); //Added(for gyroscope)
        }

        //Added(for ppg)

        if (accelerometer == null) {
            Log.w(TAG, "No accelerometer sensor found on this device.");
        }

        //Added(for ppg)
        if (heartRateSensor == null) {                     // ADDED
            Log.w(TAG, "No heart rate sensor found on this device.");
            tvHeartRate.setText("Heart Rate: not available");
        }

        //Added(for gyroscope)
        if (gyroscope == null) {
            Log.w(TAG, "No gyroscope sensor found on this device.");
            tvGyroX.setText("Gyro X: not available");
            tvGyroY.setText("Gyro Y: not available");
            tvGyroZ.setText("Gyro Z: not available");
        }

        //Added(for gyroscope)
        dbManager = new DatabaseManager(this);
        dbManager.open();
        currentSessionId = 1; // replace with real session ID later

        //Added(for ppg)
        // Request permission for heart rate sensor (required for Android 6+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    PERMISSION_REQUEST_BODY_SENSORS);
        } else {
            registerSensors();   // ADDED – helper method to register both sensors
        }

    }

    // ADDED: handle permission result
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
    }

    // ADDED: helper to register sensors (called when permission granted)
    private void registerSensors() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) { //Added(for gyroscope)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Register heart rate only if permission granted
        if (heartRateSensor != null &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) { //Added(for gyroscope)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (dbManager != null) dbManager.close(); //Added(for gyroscope)
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        Log.d(TAG, "onSensorChanged: type=" + event.sensor.getType());
        // Handle accelerometer
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Update UI
            tvX.setText("X: " + x);
            tvY.setText("Y: " + y);
            tvZ.setText("Z: " + z);

            // Sleep metric implementation
            float minX = -10;
            float maxX = 10;
            if (x >= minX && x <= maxX)
                tvX.setText("X: " + x + " (Normal)");
            else
                tvX.setText("X: " + x + " (Abnormal)");

            // Optional throttling
            if ((currentTime - lastUpdate) >= UPDATE_INTERVAL_MS) {
                lastUpdate = currentTime;
                Log.d(TAG, "Accelerometer: X=" + x + " Y=" + y + " Z=" + z);
            }
        }
        // Handle heart rate
        else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float heartRate = event.values[0];
            int accuracy = event.accuracy;

            // Update UI
            tvHeartRate.setText("Heart Rate: " + heartRate + " bpm");
            Log.d(TAG, "Heart rate: " + heartRate + " bpm (accuracy=" + accuracy + ")");

            // Add to collector if plausible and accuracy acceptable
            if (heartRate > 30 && heartRate < 200 && accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                HeartRateCollector.getInstance().addHeartRate(heartRate);
            }


        }
        // Handle gyroscope //Added(for gyroscope)
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float gyroX = event.values[0];
            float gyroY = event.values[1];
            float gyroZ = event.values[2];

            tvGyroX.setText("Gyro X: " + gyroX + " rad/s");
            tvGyroY.setText("Gyro Y: " + gyroY + " rad/s");
            tvGyroZ.setText("Gyro Z: " + gyroZ + " rad/s");
            Log.d(TAG, "Gyroscope: X=" + gyroX + " Y=" + gyroY + " Z=" + gyroZ);

            // Save to database //Added(for gyroscope)
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