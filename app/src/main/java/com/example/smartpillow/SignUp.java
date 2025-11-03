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

        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return;
        }


        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            String email = username + "@smartpillow.com";
                            createAuthUser(username, email, password);
                        } else {
                            Toast.makeText(SignUp.this, "Username already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Error checking username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createAuthUser(String username, String email, String password) {


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)


                    .addOnCompleteListener(task -> {


                    if (task.isSuccessful()) {

                        storeUsernameInFirestore(username);
                    } else {
                        Toast.makeText(SignUp.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void storeUsernameInFirestore(String username) {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("createdAt", new Date());


        db.collection("users").document(userId)

                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUp.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUp.this, LoginPage.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUp.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }


}
