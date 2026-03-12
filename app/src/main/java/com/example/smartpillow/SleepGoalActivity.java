package com.example.smartpillow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SleepGoalActivity extends AppCompatActivity {

    private EditText goalInput;
    private Button saveGoalBtn, btnCancelGoal;
    private DatabaseManager dbManager;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_goal);

        // Get username from Intent so we know who to update
        username = getIntent().getStringExtra("USERNAME");

        goalInput = findViewById(R.id.goalInput);
        saveGoalBtn = findViewById(R.id.saveGoalBtn);
        btnCancelGoal = findViewById(R.id.btnCancelGoal);

        dbManager = new DatabaseManager(this);
        dbManager.open();

        saveGoalBtn.setOnClickListener(v -> {
            String newGoal = goalInput.getText().toString().trim();
            if (!newGoal.isEmpty()) {
                // Update the goal in your SQLite database
                boolean success = dbManager.updateSleepGoal(username, Integer.parseInt(newGoal));
                if (success) {
                    Toast.makeText(this, "Goal updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to ProfilePage
                } else {
                    Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter a number", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelGoal.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}