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

import java.util.HashMap;
import java.util.Map;

public class LoginPage extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button login;
    private Button signUp;
    private DatabaseManager dbManager;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        username = findViewById(R.id.UsernameText);
        password = findViewById(R.id.PasswordText);
        login = findViewById(R.id.Loginbtn);
        signUp = findViewById(R.id.SignUpBtn);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize SQLite database
        try {
            dbManager = new DatabaseManager(this);
            dbManager.open();
        } catch (Exception e) {
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Login", "DB Error: " + e.getMessage());
        }
        signUp.setOnClickListener(v -> GotoSignUp());
        login.setOnClickListener(v -> LoginUser());
    }


    private void LoginUser() {
        String inputUsername = username.getText().toString().trim();
        String inputPassword = password.getText().toString().trim();

        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try Firebase login first
        attemptFirebaseLogin(inputUsername, inputPassword);
    }

    private void attemptFirebaseLogin(String username, String password) {
        String email = username + "@smartpillow.com";

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase login successful
                        String userId = auth.getCurrentUser().getUid();
                        Log.d("Login", "Firebase login successful for: " + username);

                        // Step 1: Check if user exists in SQLite
                        if (!userExistsInSQLite(username, password)) {
                            // User doesn't exist in SQLite, add them
                            addUserToSQLite(username, password, email, userId);
                        }

                        // Step 2: Sync any local changes to Firebase
                        syncLocalDataToFirebase(userId);

                        // Step 3: Proceed to home page
                        completeLogin(username);

                    } else {
                        // Firebase login failed, try SQLite
                        Log.d("Login", "Firebase login failed, trying SQLite");
                        attemptSQLLogin(username, password);
                    }
                });
    }

    private void attemptSQLLogin(String username, String password) {
        try {
            if (validateUserInSQLite(username, password)) {
                Log.d("Login", "SQLite login successful for: " + username);

                // SQLite login successful
                String email = username + "@smartpillow.com";

                // Try to sync this user to Firebase (for when user was created offline)
                attemptFirebaseSyncAfterSQLLogin(username, password, email);

                completeLogin(username);
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Login", "SQLite login error: " + e.getMessage());
        }
    }

    private boolean validateUserInSQLite(String username, String password) {
        try {
            android.database.Cursor cursor = dbManager.fetch();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String dbUsername = cursor.getString(1); // COLUMN_USERNAME at index 1
                    String dbPassword = cursor.getString(2); // COLUMN_PASSWORD at index 2

                    if (dbUsername.equals(username) && dbPassword.equals(password)) {
                        cursor.close();
                        return true;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            Log.e("Login", "SQLite validation error: " + e.getMessage());
            return false;
        }
    }

    private boolean userExistsInSQLite(String username, String password) {
        return validateUserInSQLite(username, password);
    }

    private void addUserToSQLite(String username, String password, String email, String firebaseUserId) {
        try {
            dbManager.insert(username, password, email, "", "", 0, 0, 0, 0, 0);
            Log.d("Login", "User added to SQLite: " + username);
        } catch (Exception e) {
            Log.e("Login", "Error adding user to SQLite: " + e.getMessage());
        }
    }

    private void attemptFirebaseSyncAfterSQLLogin(String username, String password, String email) {
        // Try to create Firebase account for this SQLite user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        Log.d("Login", "Created Firebase account for SQLite user: " + username);

                        // Sync SQLite data to Firestore
                        syncUserDataToFirestore(username, email, userId);
                    } else {
                        Log.d("Login", "Could not create Firebase account (user may exist): " + task.getException().getMessage());
                    }
                });
    }

    private void syncUserDataToFirestore(String username, String email, String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("syncedFromSQLite", true);

        firestore.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Login", "SQLite user synced to Firestore: " + username);
                })
                .addOnFailureListener(e -> {
                    Log.e("Login", "Firestore sync error: " + e.getMessage());
                });
    }

    private void syncLocalDataToFirebase(String userId) {
        // Get user data from SQLite and sync to Firebase
        try {
            android.database.Cursor cursor = dbManager.fetch();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String dbUsername = cursor.getString(1);
                    String dbPassword = cursor.getString(2);
                    String dbEmail = cursor.getString(3);
                    String dbPhone = cursor.getString(4);
                    String dbGender = cursor.getString(5);
                    int dbAge = cursor.getInt(6);
                    int dbHeight = cursor.getInt(7);
                    int dbWeight = cursor.getInt(8);
                    int dbSleepDuration = cursor.getInt(9);
                    int dbSleepQuality = cursor.getInt(10);

                    // Sync this data to Firestore
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", dbUsername);
                    userData.put("email", dbEmail);
                    userData.put("phone", dbPhone);
                    userData.put("gender", dbGender);
                    userData.put("age", dbAge);
                    userData.put("height", dbHeight);
                    userData.put("weight", dbWeight);
                    userData.put("sleep_duration", dbSleepDuration);
                    userData.put("sleep_quality", dbSleepQuality);
                    userData.put("lastSynced", new java.util.Date());

                    firestore.collection("users").document(userId)
                            .update(userData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Login", "User data synced to Firebase");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Login", "Sync failed: " + e.getMessage());
                            });

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("Login", "Sync error: " + e.getMessage());
        }
    }

    private void completeLogin(String username) {
        Toast.makeText(LoginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();

        Intent gotoHome = new Intent(LoginPage.this, HomePage.class);
        gotoHome.putExtra("USERNAME", username);
        startActivity(gotoHome);
        finish();
    }

    private void GotoSignUp() {
        Intent goSignUp = new Intent(LoginPage.this, SignUp.class);
        startActivity(goSignUp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}