package com.example.smartpillow;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackingPage extends AppCompatActivity {

    private ImageView homeBtn3;
    private ImageView statsBtn3;
    private ImageView profileBtn3;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_page);

        homeBtn3 = findViewById(R.id.home3_Btn);
        statsBtn3 = findViewById(R.id.stats3_Btn);
        profileBtn3 = findViewById(R.id.profile3_Btn);

        homeBtn3.setOnClickListener(v -> GotoHome3());
        statsBtn3.setOnClickListener(v -> GoToStats3());
        profileBtn3.setOnClickListener(v -> GoToProfile3());

    }

    private void GotoHome3(){
        Intent home3 =  new Intent(TrackingPage.this, HomePage.class);
        startActivity(home3);
    }

    private void GoToStats3(){
        Intent stats3 = new Intent(TrackingPage.this, StatsPage.class);
        startActivity(stats3);
    }

    private void GoToProfile3(){
        Intent profile3 = new Intent(TrackingPage.this, ProfilePage.class);
        startActivity(profile3);
    }






}
