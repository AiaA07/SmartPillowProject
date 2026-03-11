package com.example.smartpillow;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    private TextView profileNameTextView, profileEmailTextView, profileSleepGoalTextView;
    private TextView lastNightTextView, avgSleepTextView, streakTextView;
    private TextView weeklyTotalSleepTextView, weeklyAverageSleepTextView, weeklyBestNightTextView;
    private TextView sleepGoalProgressLabelTextView;
    private ProgressBar sleepGoalProgressBar;

    private DrawerLayout drawerLayout;
    private ImageView hamburgerBtn;
    private LinearLayout drawerHistory, drawerReport, drawerSleepGoal, drawerEditProfile, drawerLogout;
    private Button star1Btn, star2Btn, star3Btn, star4Btn, star5Btn;
    private TextView ratingLabel;
    private Button logoutButton, editProfileButton;

    private ImageView homeNavBtn, statsNavBtn, trackingNavBtn, profileNavBtn;

    private DatabaseManager dbManager;
    private String usernameFromIntent;
    private int selectedRating = 0;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // 1. Retrieve the username passed from HomePage
        usernameFromIntent = getIntent().getStringExtra("USERNAME");

        initializeViews();
        setupClickListeners();

        try {
            dbManager = new DatabaseManager(this);
            dbManager.open();
            loadUserFromSQLite();
        } catch (Exception e) {
            Log.e("ProfilePage", "DB open error: " + e.getMessage());
        }
    }

    private void initializeViews() {
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
        drawerLayout = findViewById(R.id.drawerLayout);
        hamburgerBtn = findViewById(R.id.hamburgerBtn);
        drawerHistory = findViewById(R.id.drawerHistory);
        drawerReport = findViewById(R.id.drawerReport);
        drawerSleepGoal = findViewById(R.id.drawerSleepGoal);
        drawerEditProfile = findViewById(R.id.drawerEditProfile);
        drawerLogout = findViewById(R.id.drawerLogout);
        star1Btn = findViewById(R.id.star1Btn);
        star2Btn = findViewById(R.id.star2Btn);
        star3Btn = findViewById(R.id.star3Btn);
        star4Btn = findViewById(R.id.star4Btn);
        star5Btn = findViewById(R.id.star5Btn);
        ratingLabel = findViewById(R.id.ratingLabel);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        homeNavBtn = findViewById(R.id.homeNavBtn);
        statsNavBtn = findViewById(R.id.statsNavBtn);
        trackingNavBtn = findViewById(R.id.trackingNavBtn);
        profileNavBtn = findViewById(R.id.profileNavBtn);
    }

    private void setupClickListeners() {
        hamburgerBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        drawerHistory.setOnClickListener(v -> {
            Intent i = new Intent(this, HistoryPage.class);
            i.putExtra("LOCAL_USER_ID", getUserIdFromUsername());
            startActivity(i);
        });

        drawerLogout.setOnClickListener(v -> logoutUser());
        logoutButton.setOnClickListener(v -> logoutUser());

        Button[] stars = {star1Btn, star2Btn, star3Btn, star4Btn, star5Btn};
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> updateStarRating(rating, stars));
        }

        homeNavBtn.setOnClickListener(v -> startActivity(new Intent(this, HomePage.class)));
        trackingNavBtn.setOnClickListener(v -> startActivity(new Intent(this, sensor.class)));
    }

    private void loadUserFromSQLite() {
        if (usernameFromIntent == null || usernameFromIntent.isEmpty()) {
            profileNameTextView.setText("Guest User");
            return;
        }

        // FIXED: Optimized to use direct query instead of looping through all users
        Cursor cursor = dbManager.getUserByUsername(usernameFromIntent);
        if (cursor != null && cursor.moveToFirst()) {
            String dbEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
            int dbSleepDuration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SLEEP_DURATION));

            profileNameTextView.setText(usernameFromIntent);
            profileEmailTextView.setText(dbEmail != null ? dbEmail : "No email linked");
            lastNightTextView.setText(dbSleepDuration + " hrs");
            cursor.close();
        } else {
            profileNameTextView.setText(usernameFromIntent);
        }
    }

    private void updateStarRating(int rating, Button[] stars) {
        selectedRating = rating;
        String[] labels = {"", "Poor 😟", "Fair 😐", "Okay 🙂", "Good 😊", "Great! 😴"};
        ratingLabel.setText(labels[rating]);
        for (int i = 0; i < stars.length; i++) {
            stars[i].setTextColor(android.graphics.Color.parseColor(i < rating ? "#facc15" : "#888888"));
        }
    }

    private long getUserIdFromUsername() {
        return (usernameFromIntent != null) ? dbManager.getUserIdByUsername(usernameFromIntent) : -1;
    }

    private void logoutUser() {
        auth.signOut();
        Intent i = new Intent(this, LoginPage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}