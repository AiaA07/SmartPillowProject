package com.example.smartpillow;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.hardware.Sensor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.firestore.FirebaseFirestore;

public class sensor extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_page);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            // Record every 200ms to avoid overloading Firestore
            long lastUpdate = 0;
            if ((currentTime - lastUpdate) > 200) {
                lastUpdate = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // Create timestamp
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                        .format(new Date(currentTime));

                // Create data map
                Map<String, Object> data = new HashMap<>();
                data.put("x", x);
                data.put("y", y);
                data.put("z", z);
                data.put("timestamp", timestamp);

                // Upload to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("accelerometer_data")
                        .add(data)
                        .addOnSuccessListener(documentReference ->
                                Log.d("Firestore", "Data added with ID: " + documentReference.getId()))
                        .addOnFailureListener(e ->
                                Log.w("Firestore", "Error adding document", e));
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
