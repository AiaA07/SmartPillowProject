package com.example.smartpillow;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WhiteNoisePage extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Button btnRain, btnOcean, btnFan, btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.white_noise_page);

        btnRain = findViewById(R.id.btnRain);
        btnOcean = findViewById(R.id.btnOcean);
        btnFan = findViewById(R.id.btnFan);
        btnStop = findViewById(R.id.btnStop);

        btnRain.setOnClickListener(v -> playSound(R.raw.rain));
        btnOcean.setOnClickListener(v -> playSound(R.raw.ocean));
        btnFan.setOnClickListener(v -> playSound(R.raw.fan));
        btnStop.setOnClickListener(v -> stopSound());
    }

    private void playSound(int resId) {
        stopSound(); // stop any sound already playing

        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setLooping(true);   // loop for sleep
        mediaPlayer.start();
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop when leaving the screen (you can change this later if you want it to keep playing)
        stopSound();
    }
}
