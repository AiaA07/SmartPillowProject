package com.example.smartpillow;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

        // Initialize SQLite database
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
            android.database.Cursor cursor = dbManager.fetch();
            if (cursor != null) {
                int count = cursor.getCount();
                Log.d(TAG, "Database has " + count + " users");
                cursor.close();
            }

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
                            // COLUMN_USERNAME should be at index 2 (after ID at index 0, name at index 1)
                            String dbUsername = cursor.getString(2);
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

            // Save user with empty profile data (name will be updated in popup)
            long userId = dbManager.insert(username, password, email, "", "", 0, 0, 0, 0, 0);
            Log.d(TAG, "User saved to SQLite with ID: " + userId);

            // Show success message
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

            // Show the popup questionnaire
            showQuestionnairePopup(username, email, firebaseUserId);

        } catch (Exception e) {
            Log.e(TAG, "SQLite save error: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = "Local save failed: ";
            if (e.getMessage() != null) {
                errorMsg += e.getMessage();
            } else {
                errorMsg += "Unknown error";
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();

            // Still show questionnaire even if there's an error
            showQuestionnairePopup(username, email, firebaseUserId);
        }
    }

    private void showQuestionnairePopup(String username, String email, String firebaseUserId) {
        // Create custom dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_questionnare);

        // Set dialog properties
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            params.dimAmount = 0.7f; // Dim background
            window.setAttributes(params);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        // Initialize views
        EditText fullNameEditText = dialog.findViewById(R.id.fullNameEditText);
        EditText ageEditText = dialog.findViewById(R.id.ageEditText);
        RadioGroup genderRadioGroup = dialog.findViewById(R.id.genderRadioGroup);
        EditText heightEditText = dialog.findViewById(R.id.heightEditText);
        EditText weightEditText = dialog.findViewById(R.id.weightEditText);
        Button submitButton = dialog.findViewById(R.id.submitButton);
        Button skipButton = dialog.findViewById(R.id.skipButton);

        // Submit button click
        submitButton.setOnClickListener(v -> {
            // Get values from form
            String fullName = fullNameEditText.getText().toString().trim();
            String ageStr = ageEditText.getText().toString().trim();
            String heightStr = heightEditText.getText().toString().trim();
            String weightStr = weightEditText.getText().toString().trim();

            // Get selected gender
            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            String gender = "";
            if (selectedGenderId != -1) {
                RadioButton selectedRadioButton = dialog.findViewById(selectedGenderId);
                gender = selectedRadioButton.getText().toString();
            }

            // Validate inputs
            if (fullName.isEmpty()) {
                Toast.makeText(SignUp.this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ageStr.isEmpty()) {
                Toast.makeText(SignUp.this, "Please enter your age", Toast.LENGTH_SHORT).show();
                return;
            }

            if (heightStr.isEmpty()) {
                Toast.makeText(SignUp.this, "Please enter your height", Toast.LENGTH_SHORT).show();
                return;
            }

            if (weightStr.isEmpty()) {
                Toast.makeText(SignUp.this, "Please enter your weight", Toast.LENGTH_SHORT).show();
                return;
            }

            if (gender.isEmpty()) {
                Toast.makeText(SignUp.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                int height = Integer.parseInt(heightStr);
                int weight = Integer.parseInt(weightStr);

                // Validate ranges
                if (age < 1 || age > 150) {
                    Toast.makeText(SignUp.this, "Please enter a valid age (1-150)", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (height < 50 || height > 250) {
                    Toast.makeText(SignUp.this, "Please enter a valid height (50-250 cm)", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (weight < 20 || weight > 300) {
                    Toast.makeText(SignUp.this, "Please enter a valid weight (20-300 kg)", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update user in SQLite database
                updateUserProfileInSQLite(username, fullName, gender, age, height, weight);

                // Update user in Firebase Firestore (if available)
                if (firebaseUserId != null && !firebaseUserId.isEmpty()) {
                    updateUserProfileInFirestore(firebaseUserId, fullName, gender, age, height, weight);
                }

                Toast.makeText(SignUp.this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();

                // Close dialog and navigate to login
                dialog.dismiss();
                navigateToLogin();

            } catch (NumberFormatException e) {
                Toast.makeText(SignUp.this, "Please enter valid numbers for age, height, and weight", Toast.LENGTH_SHORT).show();
            }
        });

        // Skip button click
        skipButton.setOnClickListener(v -> {
            Toast.makeText(SignUp.this, "You can update your profile later", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            navigateToLogin();
        });

        // Show the dialog
        dialog.show();
    }

    private void updateUserProfileInSQLite(String username, String fullName, String gender,
                                           int age, int height, int weight) {
        if (dbManager != null) {
            try {
                int rowsAffected = dbManager.updateUserProfile(username, fullName, gender, age, height, weight);
                if (rowsAffected > 0) {
                    Log.d(TAG, "User profile updated in SQLite: " + username);
                } else {
                    Log.w(TAG, "No rows affected when updating user profile");

                    // Try alternative method - get user ID first then update
                    android.database.Cursor cursor = dbManager.getUserByUsername(username);
                    if (cursor != null && cursor.moveToFirst()) {
                        long userId = cursor.getLong(0); // COLUMN_ID
                        cursor.close();

                        // Get current user data to preserve other fields
                        cursor = dbManager.fetch();
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                do {
                                    String dbUsername = cursor.getString(2);
                                    if (dbUsername != null && dbUsername.equals(username)) {
                                        String currentPassword = cursor.getString(3);
                                        String currentEmail = cursor.getString(4);
                                        String currentPhone = cursor.getString(5);

                                        dbManager.update(userId, fullName, username, currentPassword,
                                                currentEmail, currentPhone, gender, age,
                                                height, weight, 0, 0, 0);
                                        Log.d(TAG, "User profile updated using alternative method");
                                        break;
                                    }
                                } while (cursor.moveToNext());
                            }
                            cursor.close();
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating user in SQLite: " + e.getMessage());
            }
        }
    }

    private void updateUserProfileInFirestore(String firebaseUserId, String fullName,
                                              String gender, int age, int height, int weight) {
        if (firestore != null && firebaseUserId != null) {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("name", fullName);
            profileData.put("gender", gender);
            profileData.put("age", age);
            profileData.put("height", height);
            profileData.put("weight", weight);
            profileData.put("profileComplete", true);
            profileData.put("lastUpdated", new Date());

            firestore.collection("users").document(firebaseUserId)
                    .update(profileData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User profile updated in Firestore");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating user in Firestore: " + e.getMessage());
                    });
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignUp.this, LoginPage.class);
        startActivity(intent);
        finish();
    }

    private void saveToFirestore(String username, String email, String userId) {
        // Create user data map for Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("createdAt", new Date());
        userData.put("name", ""); // Empty initially
        userData.put("phone", "");
        userData.put("gender", "");
        userData.put("age", 0);
        userData.put("height", 0);
        userData.put("weight", 0);
        userData.put("sleep_duration", 0);
        userData.put("sleep_quality", 0);
        userData.put("lastSync", new Date());
        userData.put("profileComplete", false);
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