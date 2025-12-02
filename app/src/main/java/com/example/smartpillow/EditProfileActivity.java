package com.example.smartpillow;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText sleepGoalEditText;
    private Button saveButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.nameEditText);
        sleepGoalEditText = findViewById(R.id.sleepGoalEditText);
        saveButton = findViewById(R.id.saveButton);

        // TODO: (Optional later) Pre-fill with current values from Firestore

        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newGoal = sleepGoalEditText.getText().toString().trim();

            if (newName.isEmpty() || newGoal.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();

            db.collection("users").document(userId)
                    .update("name", newName, "sleepGoal", newGoal)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish(); // go back to ProfilePage
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}

