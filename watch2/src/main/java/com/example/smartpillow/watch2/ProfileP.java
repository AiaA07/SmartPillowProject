package com.example.smartpillow.watch2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity; // Standard for XML layouts

public class ProfileP extends AppCompatActivity { // <--- THIS IS THE KEY
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This connects the Java code to your XML layout
        setContentView(R.layout.profile_page);
    }
}