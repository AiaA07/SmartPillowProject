package com.example.smartpillow;


import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.auth.User;

public class HomePage extends AppCompatActivity {

    private ImageView statsBtn;
    private ImageView alarmBtn;
    private ImageView profileBtn;

    private TextView welcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        statsBtn = findViewById(R.id.Stats_Btn);
        alarmBtn = findViewById(R.id.Alarm_Btn);
        profileBtn = findViewById(R.id.Profile_Btn);
        welcome = findViewById(R.id.WelcomeText);

        statsBtn.setOnClickListener(v -> GotoStats());
        alarmBtn.setOnClickListener(v -> GotoAlarm());
        profileBtn.setOnClickListener(v -> GotoProfile());
    }



    private void GotoStats(){
        Intent stats = new Intent(HomePage.this, StatsPage.class);
        startActivity(stats);

    }

    private void GotoAlarm(){
        Intent alarm = new Intent(HomePage.this, AlarmPage.class);
        startActivity(alarm);
    }

    private void GotoProfile(){
        Intent profile = new Intent(HomePage.this, ProfilePage.class);
        startActivity(profile);
    }


}