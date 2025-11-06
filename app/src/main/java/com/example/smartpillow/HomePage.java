package com.example.smartpillow;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

        initializeViews();
        setupUserWelcome();
        loadSleepGoalFromFirestore();
        setupTipsRotation();
    }

    private void initializeViews() {
        welcome = findViewById(R.id.WelcomeText);
        goalTextView = findViewById(R.id.GoalTextView);
        tipsTextView = findViewById(R.id.TipsTextView);

        // Navigation
        findViewById(R.id.Stats_Btn).setOnClickListener(v -> startActivity(new Intent(this, StatsPage.class)));
        findViewById(R.id.Tracking_Btn).setOnClickListener(v -> startActivity(new Intent(this, TrackingPage.class)));
        findViewById(R.id.Profile_Btn).setOnClickListener(v -> startActivity(new Intent(this, ProfilePage.class)));

        // Set Sleep Goal button - now using dialog
        Button setGoalBtn = findViewById(R.id.SetGoalBtn);
        setGoalBtn.setOnClickListener(v -> showSleepGoalDialog());

        // Remove the input layout from view since we're using dialog
        findViewById(R.id.GoalInputLayout).setVisibility(android.view.View.GONE);
    }

    private void setupUserWelcome() {
        String username = getUsernameFromFirebase();
        welcome.setText(username != null ? "Welcome, " + username + "!" : "Welcome, User!");
    }

    private String getUsernameFromFirebase() {
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            if (email != null && email.contains("@smartpillow.com")) {
                return email.split("@")[0];
            }
        }
        return null;
    }

    private void showSleepGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Sleep Goal");
        builder.setMessage("Enter your desired sleep hours per night (1-12):");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g., 8");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String goal = input.getText().toString().trim();
            if (!goal.isEmpty()) {
                saveSleepGoalToFirestore(goal);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveSleepGoalToFirestore(String goal) {
        try {
            int hours = Integer.parseInt(goal);
            if (hours < 1 || hours > 12) {
                Toast.makeText(this, "Please enter 1-12 hours", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            Map<String, Object> userData = new HashMap<>();
            userData.put("sleepGoal", goal);
            userData.put("username", getUsernameFromFirebase());

            userRef.set(userData)
                    .addOnSuccessListener(aVoid -> {
                        goalTextView.setText("Current Goal: " + goal + " hours per night");
                        Toast.makeText(HomePage.this, "Sleep goal saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HomePage.this, "Failed to save goal", Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSleepGoalFromFirestore() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("sleepGoal")) {
                            String goal = documentSnapshot.getString("sleepGoal");
                            goalTextView.setText("Current Goal: " + goal + " hours per night");
                        } else {
                            goalTextView.setText("No sleep goal set");
                        }
                    })
                    .addOnFailureListener(e -> {
                        goalTextView.setText("No sleep goal set");
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
        tipsTextView.setText(sleepTips[random.nextInt(sleepTips.length)]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tipsHandler != null) {
            tipsHandler.removeCallbacksAndMessages(null);
        }
    }
}