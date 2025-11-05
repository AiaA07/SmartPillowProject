package com.example.smartpillow;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    private ImageView statsBtn;
    private ImageView trackingBtn;
    private ImageView profileBtn;

    private TextView welcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        statsBtn = findViewById(R.id.Stats_Btn);
        trackingBtn = findViewById(R.id.Tracking_Btn);
        profileBtn = findViewById(R.id.Profile_Btn);
        welcome = findViewById(R.id.WelcomeText);

        statsBtn.setOnClickListener(v -> GotoStats());
        trackingBtn.setOnClickListener(v -> GotoTracking());
        profileBtn.setOnClickListener(v -> GotoProfile());
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