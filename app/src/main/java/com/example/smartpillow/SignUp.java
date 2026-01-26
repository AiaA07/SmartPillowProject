package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText UsernameSP;
    private EditText PasswordSP;
    private EditText RepassSP;
    private Button SignUpBtn2;
    private Button BackBtn2;
    private DatabaseManager dbManager;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        UsernameSP = findViewById(R.id.CreateUsername);
        PasswordSP = findViewById(R.id.CreatePassword);
        RepassSP = findViewById(R.id.RenterPass);
        SignUpBtn2 = findViewById(R.id.SignUpBtn);
        BackBtn2 = findViewById(R.id.BackBtn);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize SQLite database - FIXED: Use getApplicationContext()
        try {
            dbManager = new DatabaseManager(getApplicationContext());
            dbManager.open();
            Log.d(TAG, "Database opened successfully");

            // Debug: Check if database was created
            debugDatabaseSetup();

        } catch (Exception e) {
            Toast.makeText(this, "Database initialization error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "DB Open Error: " + e.getMessage());
            e.printStackTrace();
        }

        SignUpBtn2.setOnClickListener(v -> signUpUser());
        BackBtn2.setOnClickListener(v -> GoBack());
    }

    private void debugDatabaseSetup() {
        try {
            // Check if we can query the database
            android.database.Cursor cursor = dbManager.fetch();
            if (cursor != null) {
                int count = cursor.getCount();
                Log.d(TAG, "Database has " + count + " users");
                cursor.close();
            }

            // Check table structure
            Log.d(TAG, "Table name: " + DatabaseHelper.TABLE_NAME);
            Log.d(TAG, "Database name: " + DatabaseHelper.DATABASE_NAME);

        } catch (Exception e) {
            Log.e(TAG, "Debug error: " + e.getMessage());
        }
    }

    private void GoBack() {
        Intent login = new Intent(SignUp.this, LoginPage.class);
        startActivity(login);
        finish();
    }

    private void signUpUser() {
        String username = UsernameSP.getText().toString().trim();
        String password = PasswordSP.getText().toString().trim();
        String confirmPass = RepassSP.getText().toString().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username exists in SQLite first
        if (isUsernameTakenInSQLite(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Firebase email
        String email = username + "@smartpillow.com";

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firebase auth successful
                        String userId = auth.getCurrentUser().getUid();
                        Log.d(TAG, "Firebase auth success, userId: " + userId);

                        // Step 1: Save to SQLite (local storage)
                        saveToSQLite(username, password, email, userId);

                        // Step 2: Save to Firestore (cloud storage)
                        saveToFirestore(username, email, userId);

                    } else {
                        // Firebase auth failed
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(SignUp.this, "Firebase error: " + errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Firebase Auth Error: " + errorMsg);

                        // Still save to SQLite for offline use
                        saveToSQLite(username, password, email, null);
                    }
                });
    }

    private boolean isUsernameTakenInSQLite(String username) {
        if (dbManager == null) {
            Log.e(TAG, "DatabaseManager is null!");
            Toast.makeText(this, "Database not ready", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            android.database.Cursor cursor = dbManager.fetch();

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            // COLUMN_USERNAME should be at index 1 (after ID at index 0)
                            String dbUsername = cursor.getString(1);
                            Log.d(TAG, "Checking username: " + dbUsername + " vs " + username);

                            if (dbUsername != null && dbUsername.equals(username)) {
                                cursor.close();
                                return true;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading cursor: " + e.getMessage());
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "SQLite check error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void saveToSQLite(String username, String password, String email, String firebaseUserId) {
        if (dbManager == null) {
            Log.e(TAG, "Cannot save to SQLite - DatabaseManager is null!");
            Toast.makeText(this, "Database not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "Attempting to save user to SQLite: " + username);

            // Save all user data to SQLite
            dbManager.insert(username, password, email, "", "", 0, 0, 0, 0, 0);
            Log.d(TAG, "User saved to SQLite: " + username);

            // Show success message
            Toast.makeText(this, "Account created (saved locally)", Toast.LENGTH_SHORT).show();

            // Verify the user was saved
            verifyUserInSQLite(username);

            // Navigate to login
            Intent intent = new Intent(SignUp.this, LoginPage.class);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "SQLite save error: " + e.getMessage());
            e.printStackTrace(); // This will show the full stack trace

            // Show detailed error
            String errorMsg = "Local save failed: ";
            if (e.getMessage() != null) {
                errorMsg += e.getMessage();
            } else {
                errorMsg += "Unknown error";
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void verifyUserInSQLite(String username) {
        try {
            android.database.Cursor cursor = dbManager.fetch();
            if (cursor != null) {
                int count = cursor.getCount();
                Log.d(TAG, "Total users in database: " + count);

                if (cursor.moveToFirst()) {
                    do {
                        try {
                            String dbUsername = cursor.getString(1);
                            Log.d(TAG, "Found user in DB: " + dbUsername);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading user from cursor");
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Verification error: " + e.getMessage());
        }
    }

    private void saveToFirestore(String username, String email, String userId) {
        // Create user data map for Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("createdAt", new Date());
        userData.put("phone", "");
        userData.put("gender", "");
        userData.put("age", 0);
        userData.put("height", 0);
        userData.put("weight", 0);
        userData.put("sleep_duration", 0);
        userData.put("sleep_quality", 0);
        userData.put("lastSync", new Date());
        userData.put("source", "Android App - SQLite Sync");

        // Save to Firestore
        firestore.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved to Firestore: " + username);
                    Toast.makeText(SignUp.this, "Account synced to cloud!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore save error: " + e.getMessage());
                    Toast.makeText(SignUp.this, "Cloud sync failed (saved locally)", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
            Log.d(TAG, "Database closed");
        }
    }
}
