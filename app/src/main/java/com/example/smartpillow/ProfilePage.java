package com.example.smartpillow;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    private TextView profileNameTextView, profileEmailTextView, profileSleepGoalTextView;
    private TextView lastNightTextView, avgSleepTextView, streakTextView;
    private Button logoutButton;
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
        logoutButton = findViewById(R.id.logoutButton); // 👈 NEW

        lastNightTextView = findViewById(R.id.lastNightTextView);
        avgSleepTextView = findViewById(R.id.avgSleepTextView);
        streakTextView = findViewById(R.id.streakTextView);


        // Set up logout button
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(ProfilePage.this, "Logged out", Toast.LENGTH_SHORT).show();

            // TODO: replace LoginActivity.class with your actual login screen class name
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
