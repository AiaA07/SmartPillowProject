package com.example.smartpillow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {

    private ImageView statsBtn;
    private ImageView trackingBtn;
    private ImageView profileBtn;
    private TextView welcome;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        statsBtn = findViewById(R.id.Stats_Btn);
        trackingBtn = findViewById(R.id.Tracking_Btn);
        profileBtn = findViewById(R.id.Profile_Btn);
        welcome = findViewById(R.id.WelcomeText);

        auth = FirebaseAuth.getInstance();

        // Get username from Firebase Auth and display it
        String username = getUsernameFromFirebase();
        updateWelcomeText(username);

        statsBtn.setOnClickListener(v -> GotoStats());
        trackingBtn.setOnClickListener(v -> GotoTracking());
        profileBtn.setOnClickListener(v -> GotoProfile());
    }

    private String getUsernameFromFirebase() {
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            if (email != null && email.contains("@smartpillow.com")) {
                // Extract username from email (part before @smartpillow.com)
                return email.split("@")[0];
            }
        }
        return null;
    }

    private void updateWelcomeText(String username) {
        if (username != null && !username.isEmpty()) {
            welcome.setText("Welcome, " + username + "!");
        } else {
            welcome.setText("Welcome, User!");
        }
    }

    private void GotoStats(){
        Intent stats = new Intent(HomePage.this, StatsPage.class);
        startActivity(stats);
    }

    private void GotoTracking(){
        Intent tracking = new Intent(HomePage.this, TrackingPage.class);
        startActivity(tracking);
    }

    private void GotoProfile(){
        Intent profile = new Intent(HomePage.this, ProfilePage.class);
        startActivity(profile);
    }
}