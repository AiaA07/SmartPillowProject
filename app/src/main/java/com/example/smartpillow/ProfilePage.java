package com.example.smartpillow;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

public class ProfilePage extends AppCompatActivity {

    private TextView profileNameTextView, profileEmailTextView, profileSleepGoalTextView;
    private TextView lastNightTextView, avgSleepTextView, streakTextView;
    private TextView weeklyTotalSleepTextView, weeklyAverageSleepTextView, weeklyBestNightTextView;
    private TextView sleepGoalProgressLabelTextView;
    private ProgressBar sleepGoalProgressBar;

    private DatabaseManager dbManager;
    private String usernameFromIntent;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);   // 👈 make sure this matches your XML file name

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // 1. Get username passed from HomePage
        usernameFromIntent = getIntent().getStringExtra("USERNAME");
        Log.d("ProfilePage", "Username received: " + usernameFromIntent);

        // 2. Find views
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        profileSleepGoalTextView = findViewById(R.id.profileSleepGoalTextView);

        lastNightTextView = findViewById(R.id.lastNightTextView);
        avgSleepTextView = findViewById(R.id.avgSleepTextView);
        streakTextView = findViewById(R.id.streakTextView);

        weeklyTotalSleepTextView = findViewById(R.id.weeklyTotalSleepTextView);
        weeklyAverageSleepTextView = findViewById(R.id.weeklyAverageSleepTextView);
        weeklyBestNightTextView = findViewById(R.id.weeklyBestNightTextView);

        sleepGoalProgressLabelTextView = findViewById(R.id.sleepGoalProgressLabelTextView);
        sleepGoalProgressBar = findViewById(R.id.sleepGoalProgressBar);

        // 3. Open local database
        try {
            dbManager = new DatabaseManager(this);
            dbManager.open();
        } catch (Exception e) {
            Toast.makeText(this, "DB error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ProfilePage", "DB open error: " + e.getMessage());
        }

        // 4. Load user info from SQLite
        loadUserFromSQLite();
    }

    private void loadUserFromSQLite() {
        if (usernameFromIntent == null || usernameFromIntent.isEmpty()) {
            Toast.makeText(this, "No username passed to ProfilePage", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Cursor cursor = dbManager.fetch();  // same as LoginPage

            if (cursor != null && cursor.moveToFirst()) {
                boolean found = false;

                // Log what username we are searching for
                Log.d("ProfilePage", "Intent username = " + usernameFromIntent);

                do {
                    String dbUsername = cursor.getString(1); // COLUMN_USERNAME at index 1

                    // Log each username from the DB
                    Log.d("ProfilePage", "Row username from DB = " + dbUsername);

                    // ✅ Only enter this block when the username matches (case-insensitive)
                    if (dbUsername != null && dbUsername.equalsIgnoreCase(usernameFromIntent)) {
                        Log.d("ProfilePage", "MATCH FOUND in DB for username: " + dbUsername);

                        found = true;

                        String dbEmail = cursor.getString(3);      // COLUMN_EMAIL
                        int dbSleepDuration = cursor.getInt(9);    // sleep_duration
                        int dbSleepQuality = cursor.getInt(10);    // sleep_quality

                        // Basic profile fields
                        profileNameTextView.setText(dbUsername);
                        profileEmailTextView.setText(dbEmail != null ? dbEmail : "No email");
                        profileSleepGoalTextView.setText("Sleep goal: 8 hours/day");

                        // ✅ Show only real basic data for now
                        lastNightTextView.setText("Last night: " + dbSleepDuration + " hrs");
                        avgSleepTextView.setText("Average sleep: coming soon");
                        streakTextView.setText("Streak: coming soon");

                        // ✅ Weekly stats not implemented yet
                        weeklyTotalSleepTextView.setText("Weekly total: coming soon");
                        weeklyAverageSleepTextView.setText("Weekly average: coming soon");
                        weeklyBestNightTextView.setText("Best night: coming soon");

                        // ✅ Goal progress removed (placeholder only)
                        sleepGoalProgressBar.setProgress(0);
                        sleepGoalProgressLabelTextView.setText("Goal tracking coming soon");

                        // Stop looping once we found the user
                        break;
                    }

                } while (cursor.moveToNext());

                if (!found) {
                    Log.d("ProfilePage", "NO MATCH FOUND in DB for username: " + usernameFromIntent);
                    Toast.makeText(this, "User not found in local DB", Toast.LENGTH_SHORT).show();
                }

                cursor.close();
            } else {
                Log.d("ProfilePage", "No users in local DB or cursor is null");
                Toast.makeText(this, "No users in local DB", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("ProfilePage", "Error loading from SQLite: " + e.getMessage());
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}
