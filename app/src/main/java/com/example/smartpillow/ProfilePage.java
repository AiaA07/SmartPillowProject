package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView usernameValue, emailValue, goalValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameValue = findViewById(R.id.usernameValue);
        emailValue = findViewById(R.id.emailValue);
        goalValue = findViewById(R.id.goalValue);

        loadUserData();

        Button logoutBtn = findViewById(R.id.logoutButton);
        logoutBtn.setOnClickListener(v -> logoutUser());


    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null)
            return;

        String userId = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();

        // Set email immediately
        if (email != null)
            emailValue.setText(email);

        // Load username + sleep goal from Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        usernameValue.setText(doc.getString("username"));
                        goalValue.setText(doc.getString("sleepGoal") + " hours/night");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void logoutUser() {
        auth.signOut();
        Toast.makeText(ProfilePage.this, "Logged out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginPage.class));
        finish();
    }
}
