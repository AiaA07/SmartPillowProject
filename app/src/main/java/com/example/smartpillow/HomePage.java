package com.example.smartpillow;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HomePage extends AppCompatActivity {

    private TextView welcome, goalTextView, tipsTextView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Handler tipsHandler;
    private Random random;
    private DatabaseManager dbManager;

    // Added to maintain user session across the activity
    private String currentUsername;

    private final String[] sleepTips = {
            "Maintain a consistent sleep schedule, even on weekends.",
            "Create a relaxing bedtime routine.",
            "Avoid caffeine and heavy meals before bedtime.",
            "Keep your bedroom cool, dark, and quiet.",
            "Limit screen time 1 hour before bed.",
            "Regular exercise helps you sleep better.",
            "Try reading or listening to calm music before bed.",
            "Avoid late afternoon naps.",
            "Get natural sunlight during the day.",
            "Manage stress through meditation.",
            "Invest in a comfortable mattress and pillows.",
            "Avoid drinking too much before bed.",
            "Keep a sleep diary to track patterns.",
            "Try the 4-7-8 breathing technique.",
            "Use your bedroom only for sleep."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tipsHandler = new Handler();
        random = new Random();

        dbManager = new DatabaseManager(this);
        dbManager.open();

        // FIXED ORDER: Initialize views first so they aren't null when setupUserWelcome runs
        initializeViews();
        setupUserWelcome();

        loadSleepGoalFromFirestore();
        setupTipsRotation();
    }

    private void initializeViews() {
        welcome = findViewById(R.id.WelcomeText);
        goalTextView = findViewById(R.id.GoalTextView);
        tipsTextView = findViewById(R.id.TipsTextView);

        // Stats Button - Using original ID
        View statsBtn = findViewById(R.id.Stats_Btn);
        if (statsBtn != null) {
            statsBtn.setOnClickListener(v -> {
                if (currentUsername != null) {
                    long localUserId = dbManager.getUserIdByUsername(currentUsername);
                    Intent intent = new Intent(HomePage.this, StatsPage.class);
                    intent.putExtra("LOCAL_USER_ID", localUserId);
                    intent.putExtra("USERNAME", currentUsername);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Tracking Button - Using original ID
        View trackingBtn = findViewById(R.id.Tracking_Btn);
        if (trackingBtn != null) {
            trackingBtn.setOnClickListener(v -> {
                if (currentUsername != null) {
                    long localUserId = dbManager.getUserIdByUsername(currentUsername);
                    Intent intent = new Intent(this, sensor.class);
                    intent.putExtra("LOCAL_USER_ID", localUserId);
                    intent.putExtra("USERNAME", currentUsername);
                    startActivity(intent);
                }
            });
        }

        // Profile Button - Using original ID
        View profileBtn = findViewById(R.id.Profile_Btn);
        if (profileBtn != null) {
            profileBtn.setOnClickListener(v -> {
                Intent intent = new Intent(HomePage.this, ProfilePage.class);
                intent.putExtra("USERNAME", currentUsername);
                startActivity(intent);
            });
        }

        // Team Goal & Demo Buttons
        Button setGoalBtn = findViewById(R.id.SetGoalBtn);
        if (setGoalBtn != null) {
            setGoalBtn.setOnClickListener(v -> showSleepGoalDialog());
        }

        Button simulateBtn = findViewById(R.id.btnSimulateSleep);
        if (simulateBtn != null) {
            simulateBtn.setOnClickListener(v -> Toast.makeText(this, "Simulating session...", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupUserWelcome() {
        // Get username from LoginPage intent
        currentUsername = getIntent().getStringExtra("USERNAME");

        // Fallback to Firebase if intent is null
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = getUsernameFromFirebase();
        }

        if (welcome != null) {
            welcome.setText(currentUsername != null ? "Welcome, " + currentUsername + "!" : "Welcome, User!");
        }
    }

    private String getUsernameFromFirebase() {
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            if (email != null && email.contains("@")) {
                return email.split("@")[0];
            }
        }
        return null;
    }

    private void showSleepGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Sleep Goal");
        builder.setMessage("Enter desired sleep hours (1-12):");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String goal = input.getText().toString().trim();
            if (!goal.isEmpty()) saveSleepGoalToFirestore(goal);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void saveSleepGoalToFirestore(String goal) {
        try {
            int hours = Integer.parseInt(goal);
            if (hours < 1 || hours > 12) return;

            String userId = auth.getCurrentUser().getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            Map<String, Object> userData = new HashMap<>();
            userData.put("sleepGoal", goal);
            userData.put("username", currentUsername);

            userRef.set(userData).addOnSuccessListener(aVoid -> {
                goalTextView.setText("Current Goal: " + goal + " hours");
            });
        } catch (Exception e) {
            Log.e("Home", "Error saving goal: " + e.getMessage());
        }
    }

    private void loadSleepGoalFromFirestore() {
        if (auth.getCurrentUser() != null) {
            db.collection("users").document(auth.getCurrentUser().getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.contains("sleepGoal")) {
                            goalTextView.setText("Current Goal: " + doc.get("sleepGoal") + " hours");
                        }
                    });
        }
    }

    private void setupTipsRotation() {
        showRandomTip();
        tipsHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showRandomTip();
                tipsHandler.postDelayed(this, 15000);
            }
        }, 15000);
    }

    private void showRandomTip() {
        if (tipsTextView != null) tipsTextView.setText(sleepTips[random.nextInt(sleepTips.length)]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tipsHandler != null) tipsHandler.removeCallbacksAndMessages(null);
        if (dbManager != null) dbManager.close();
    }
}
