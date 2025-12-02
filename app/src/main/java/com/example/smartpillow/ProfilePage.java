package com.example.smartpillow;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ProgressBar;




public class ProfilePage extends AppCompatActivity {
    private TextView weeklyTotalSleepTextView;
    private TextView weeklyAverageSleepTextView;
    private TextView weeklyBestNightTextView;

    private TextView profileNameTextView, profileEmailTextView, profileSleepGoalTextView;
    private TextView lastNightTextView, avgSleepTextView, streakTextView;
    private Button logoutButton;
    private Button editProfileButton;
    private ProgressBar sleepGoalProgressBar; //new
    private TextView sleepGoalProgressLabelTextView; //new
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Connect XML to Java
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        profileSleepGoalTextView = findViewById(R.id.profileSleepGoalTextView);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);

        lastNightTextView = findViewById(R.id.lastNightTextView);
        avgSleepTextView = findViewById(R.id.avgSleepTextView);
        streakTextView = findViewById(R.id.streakTextView);

        // NEW: sleep goal progress views
        sleepGoalProgressBar = findViewById(R.id.sleepGoalProgressBar);
        sleepGoalProgressLabelTextView = findViewById(R.id.sleepGoalProgressLabelTextView);
        weeklyTotalSleepTextView = findViewById(R.id.weeklyTotalSleepTextView);
        weeklyAverageSleepTextView = findViewById(R.id.weeklyAverageSleepTextView);
        weeklyBestNightTextView = findViewById(R.id.weeklyBestNightTextView);

        //temporary values
        int[] last7Nights = {7, 6, 8, 5, 7, 6, 8}; // example hours per night
        int total = 0;
        int best = 0;

        for (int h : last7Nights) {
            total += h;
            if (h > best) best = h;
        }

        float average = total / 7.0f;

        weeklyTotalSleepTextView.setText("Total sleep: " + total + " hrs");
        weeklyAverageSleepTextView.setText("Average per night: " + String.format("%.1f", average) + " hrs");
        weeklyBestNightTextView.setText("Best night: " + best + " hrs");



        //NEW: temporary demo values
        int goalHours = 8;       // example sleep goal
        int lastNightHours = 6;  // example last night's sleep

        int progressPercent = (int) ((lastNightHours * 100.0f) / goalHours);
        if (progressPercent > 100) progressPercent = 100;

        sleepGoalProgressBar.setMax(100);
        sleepGoalProgressBar.setProgress(progressPercent);
        sleepGoalProgressLabelTextView.setText(progressPercent + "% of sleep goal reached");

        // Set up logout button
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(ProfilePage.this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfilePage.this, LoginPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        loadBasicProfile();
        loadSleepGoal();
        loadSleepSummary();
    }


    private void loadBasicProfile() {
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            profileEmailTextView.setText(email != null ? email : "");

            // If you don't have a name in Firestore yet, set default for now
            profileNameTextView.setText("User");
        }
    }

    private void loadSleepSummary() {
        // Sample values for now — looks real for presentation
        lastNightTextView.setText("Last night: 7h 15m");
        avgSleepTextView.setText("Average sleep (7 days): 6h 48m");
        streakTextView.setText("Sleep goal streak: 3 days");
    }

    private void loadSleepGoal() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {

                            Object goalObj = doc.get("sleepGoal");

                            if (goalObj != null) {
                                profileSleepGoalTextView.setText("Sleep goal: " + goalObj + " hours per night");
                            } else {
                                profileSleepGoalTextView.setText("Sleep goal: Not set");
                            }
                        }
                    });
        }
    }
}

