package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;
import java.util.Date;
import com.google.firebase.auth.FirebaseAuth;


public class SignUp extends AppCompatActivity {

    private EditText UsernameSP;
    private EditText PasswordSP;
    private EditText RepassSP;
    private Button SignUpBtn2;

    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);



            UsernameSP = findViewById(R.id.CreateUsername);
            PasswordSP = findViewById(R.id.CreatePassword);
            RepassSP =  findViewById(R.id.RenterPass);
            SignUpBtn2 = findViewById(R.id.SignUpBtnP);

            db = FirebaseFirestore.getInstance();


            SignUpBtn2.setOnClickListener(v -> signUpUser());

        };


    private void signUpUser() {
        String username = UsernameSP.getText().toString().trim();
        String password = PasswordSP.getText().toString().trim();
        String confirmPass = RepassSP.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();

            return;
        }

        //checking if "password" and "re-enter password" fields match
        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return;
        }


        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // points to 'users' folder in the firestore database
        db.collection("users")

                //only shows documents where the username equals what the user typed
                .whereEqualTo("username", username)

                //sends the request to firestore. nothing happens without this line.
                //like clicking 'search' button after you type something in the search bar
                .get()

                //sets up a callback that will run when firestore responds
                //callback = since database operations take time, we don't wait , we say "call me back when you're done"
                .addOnCompleteListener(task -> {

                    //checks if the tasks we did above were able to be completed without errors (network issues, etc)
                    if(task.isSuccessful()) {

                        //task.getResult() gets the search results
                        //.isEmpty() checks if we got zero results back
                        //If empty, it means no user has this username
                        if (task.getResult().isEmpty()) {
                            // Username available, create account with email pattern
                            //Creates a fake email address from the username
                            String email = username + "@smartpillow.com";  // Create fake email

                            //Calls the method to create the actual user account
                            createAuthUser(username, email, password);

                            //if username already exists, give this error message
                        } else {
                            Toast.makeText(SignUp.this, "Username already exists", Toast.LENGTH_SHORT).show();
                        }

                        //If task.isSuccessful() was false, this handles any errors that occurred during the database query
                    } else {

                        //task.getException() gets the error object that contains what went wrong
                        //.geMessage() extracts the error description from the exception. It returns the actual error message as a string
                        Toast.makeText(SignUp.this, "Error checking username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createAuthUser(String username, String email, String password) {

        //FirebaseAuth.getInstance() gets a reference to the Firebase Authentication service

        /*.createUserWithEmailAndPassword(email, password): Tells Firebase Auth to create a new user account with the provided email and password. This is where Firebase:
        - Encrypts the password securely
        - Creates a unique user ID
        - Stores the account in their secure system
        */
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)

                //Sets up a callback that will run when Firebase Auth finishes creating the account.
                    .addOnCompleteListener(task -> {

                    /*
                    task.isSuccessful() checks if Firebase Auth successfully created the account
                    If yes, it calls storeUsernameInFirestore(username) to save the username separately in your database
                     */
                    if (task.isSuccessful()) {
                        // User created in Firebase Auth, now store username in Firestore
                        storeUsernameInFirestore(username);
                    } else {
                        Toast.makeText(SignUp.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Creates a helper method that takes the username as a parameter.
    // This method is called after Firebase Auth has successfully created the user account.
    private void storeUsernameInFirestore(String username) {

        /*
        What it does:
        - FirebaseAuth.getInstance(): gets the firebase authentication service
        - .getCurrentUser() - Gets the user who was just created and logged in
        - getUid() - Gets the unique ID that firebase assigned to this user
         */
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Creates an empty Map (key-value pairs) to store the user data.
        Map<String, Object> user = new HashMap<>();

        /*Fills the map with user information:
          - username: the display name the user chose
          - createdAt: the current date/time when the account was created
        */
        user.put("username", username);
        user.put("createdAt", new Date());

        //db.collection("users"): points to the "users" collection
        //.document(userID): targets a specific document using the Firebase Auth user ID
        db.collection("users").document(userId)
                //creates a new user document (only replaces if user id is the same but that will most likely never happen)
                .set(user)

                /*
                What is does when save succeeds:
                - aVoid: empty parameter (we don't need any data back)
                - Shows success message to user
                - Creates an Intent to navigate to login page
                - startActivity(Intent): opens the login page
                - finish(): closes the current signup page (so user can't go back)
                 */
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUp.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUp.this, LoginPage.class);
                    startActivity(intent);
                    finish();
                })

                /*
                What it does when the save fails:
                - e: contains the error information
                - Shows the error message with the specfici details
                 */
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUp.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }


}
