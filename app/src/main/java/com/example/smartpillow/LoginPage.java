package com.example.smartpillow;

import android.content.Intent;
import android.os.Bundle;
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
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        username = findViewById(R.id.UsernameText);
        password = findViewById(R.id.PasswordText);
        login = findViewById(R.id.Loginbtn);
        signUp = findViewById(R.id.SignUpBtn);

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

        String email = Username + "@smartpillow.com";

        auth.signInWithEmailAndPassword(email, Password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent gotoSign = new Intent(LoginPage.this, HomePage.class);
                        startActivity(gotoSign);
                        finish();
                    }else{
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void GotoSignUp(){
        Intent goSignUp = new Intent(LoginPage.this, SignUp.class);
        startActivity(goSignUp);
    }


}
