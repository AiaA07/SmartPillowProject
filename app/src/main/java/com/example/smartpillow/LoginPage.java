package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button login;
    private Button signUp;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        username = findViewById(R.id.UsernameText);
        password = findViewById(R.id.PasswordText);
        login = findViewById(R.id.Loginbtn);
        signUp = findViewById(R.id.SignUpBtn);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(v -> GotoSignUp());
        login.setOnClickListener(v -> LoginUser());

    }

    private void LoginUser(){
        String Username = username.getText().toString().trim();
        String Password = password.getText().toString().trim();

        if (Username.isEmpty() || Password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();

            return;
        }

        //convert username to email format
        String email = Username + "@smartpillow.com";

        //Sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(email, Password)

       /* This line is equivalent to saying:
        "Hey Firebase, when you finish trying to log this user in,
        come back to my LoginPage and give me the result in a variable called task"*/
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent gotoSign = new Intent(LoginPage.this, HomePage.class);
                        startActivity(gotoSign);
                        finish();
                    }else{

                        //For authentication firebase, use: task.getException().getMessage(),
                        //For firestore database firebase, use: e.getMessage()
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void GotoSignUp(){
        Intent goSignUp = new Intent(LoginPage.this, SignUp.class);
        startActivity(goSignUp);
    }



}
