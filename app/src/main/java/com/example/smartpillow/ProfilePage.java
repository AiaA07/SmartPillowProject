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

    // NEW: Drawer and star rating views
    private DrawerLayout drawerLayout;
    private ImageView hamburgerBtn;
    private LinearLayout drawerHistory, drawerReport, drawerSleepGoal, drawerEditProfile, drawerLogout;
    private Button star1Btn, star2Btn, star3Btn, star4Btn, star5Btn;
    private TextView ratingLabel;
    private Button logoutButton, editProfileButton;

    // NEW: Bottom nav
    private ImageView homeNavBtn, statsNavBtn, trackingNavBtn, profileNavBtn;

    private DatabaseManager dbManager;
    private String usernameFromIntent;
    private int selectedRating = 0;

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

        // NEW: Find drawer and star views
        drawerLayout      = findViewById(R.id.drawerLayout);
        hamburgerBtn      = findViewById(R.id.hamburgerBtn);
        drawerHistory     = findViewById(R.id.drawerHistory);
        drawerReport      = findViewById(R.id.drawerReport);
        drawerSleepGoal   = findViewById(R.id.drawerSleepGoal);
        drawerEditProfile = findViewById(R.id.drawerEditProfile);
        drawerLogout      = findViewById(R.id.drawerLogout);
        star1Btn          = findViewById(R.id.star1Btn);
        star2Btn          = findViewById(R.id.star2Btn);
        star3Btn          = findViewById(R.id.star3Btn);
        star4Btn          = findViewById(R.id.star4Btn);
        star5Btn          = findViewById(R.id.star5Btn);
        ratingLabel       = findViewById(R.id.ratingLabel);
        logoutButton      = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);

        // NEW: Bottom nav
        homeNavBtn     = findViewById(R.id.homeNavBtn);
        statsNavBtn    = findViewById(R.id.statsNavBtn);
        trackingNavBtn = findViewById(R.id.trackingNavBtn);
        profileNavBtn  = findViewById(R.id.profileNavBtn);

        // NEW: Hamburger opens drawer
        hamburgerBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // NEW: Drawer menu items
        drawerHistory.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            long userId = getUserIdFromUsername();
            Intent i = new Intent(this, HistoryPage.class);
            i.putExtra("LOCAL_USER_ID", userId);
            startActivity(i);
        });

        drawerReport.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            long userId = getUserIdFromUsername();
            Intent i = new Intent(this, ResultsPage.class);
            i.putExtra("LOCAL_USER_ID", userId);
            startActivity(i);
        });

        drawerSleepGoal.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            showSleepGoalDialog();
        });

        drawerEditProfile.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            // TODO: open edit profile screen
            Toast.makeText(this, "Edit Profile coming soon", Toast.LENGTH_SHORT).show();
        });

        drawerLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            logoutUser();
        });

        // NEW: Logout button on main screen
        logoutButton.setOnClickListener(v -> logoutUser());

        // NEW: Edit profile button on main screen
        editProfileButton.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile coming soon", Toast.LENGTH_SHORT).show());

        // NEW: Star rating buttons
        Button[] stars = {star1Btn, star2Btn, star3Btn, star4Btn, star5Btn};
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> updateStarRating(rating, stars));
        }

        // NEW: Bottom nav listeners
        homeNavBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HomePage.class)));
        statsNavBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, StatsPage.class);
            i.putExtra("LOCAL_USER_ID", getUserIdFromUsername());
            startActivity(i);
        });
        trackingNavBtn.setOnClickListener(v ->
                startActivity(new Intent(this, sensor.class)));
        profileNavBtn.setOnClickListener(v -> { /* already here */ });

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

    // NEW: Update star colors based on selected rating
    private void updateStarRating(int rating, Button[] stars) {
        selectedRating = rating;
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setTextColor(android.graphics.Color.parseColor("#facc15")); // yellow
            } else {
                stars[i].setTextColor(android.graphics.Color.parseColor("#888888")); // grey
            }
        }
        String[] labels = {"", "Poor 😟", "Fair 😐", "Okay 🙂", "Good 😊", "Great! 😴"};
        ratingLabel.setText(labels[rating]);
        Toast.makeText(this, "Rating saved: " + rating + "/5", Toast.LENGTH_SHORT).show();
        Log.d("ProfilePage", "User rated sleep: " + rating + "/5");
    }

    // NEW: Helper to get user ID from username
    private long getUserIdFromUsername() {
        if (usernameFromIntent != null && dbManager != null) {
            return dbManager.getUserIdByUsername(usernameFromIntent);
        }
        return -1;
    }

    // NEW: Sleep goal dialog
    private void showSleepGoalDialog() {
        AppCompatEditText input = new AppCompatEditText(this);
        input.setHint("e.g., 8");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Change Sleep Goal")
                .setMessage("Enter your desired sleep hours per night (1-12):")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) ->
                        Toast.makeText(this, "Sleep goal updated!", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // NEW: Logout user from Firebase and go back to login
    private void logoutUser() {
        auth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, LoginPage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
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
                        lastNightTextView.setText(dbSleepDuration + " hrs");
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