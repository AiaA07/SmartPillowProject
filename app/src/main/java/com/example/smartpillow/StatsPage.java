package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.math.Stats;


public class StatsPage extends AppCompatActivity {

    private ImageView homeBtn2;
    private ImageView trackingBtn2;
    private ImageView profileBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_page);

        homeBtn2 = findViewById(R.id.home_Btn);
        trackingBtn2 = findViewById(R.id.tracking_Btn);
        profileBtn2 = findViewById(R.id.profile_Btn);


        homeBtn2.setOnClickListener(v -> GotoHome2());
        trackingBtn2.setOnClickListener(v -> GoToTracking2());
        profileBtn2.setOnClickListener(v -> GoToProfile2());

    }


    private void GotoHome2(){
        Intent home2 =  new Intent(StatsPage.this, HomePage.class);
        startActivity(home2);
    }

    private void GoToTracking2(){
        Intent track2 = new Intent(StatsPage.this, TrackingPage.class);
        startActivity(track2);
    }

    private void GoToProfile2(){
        Intent profile2 = new Intent(StatsPage.this, ProfilePage.class);
        startActivity(profile2);
    }


}