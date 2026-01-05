package com.example.prctc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    Button btnLogin;
    CheckBox cbRememberMe;
    TextView tvRegister;
    SharedPrefManager sharedPrefManager;

    private FirebaseAuth mAuth;
    // Added Database variables
    private DatabaseReference mDatabase;
    private final String DB_URL = "https://prctc-5b113-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // FIX: Initialize Database with the correct region URL
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("Users");

        sharedPrefManager = new SharedPrefManager(this);

        // Auto-login logic
        if (mAuth.getCurrentUser() != null && sharedPrefManager.isRemembered()) {
            startActivity(new Intent(LoginActivity.this, homepage.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvRegister = findViewById(R.id.tvRegister);

        String savedEmail = sharedPrefManager.getEmail();
        String savedPassword = sharedPrefManager.getPassword();

        if (!savedEmail.isEmpty()) {
            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);
        }

        btnLogin.setOnClickListener(v -> {
            loginUser();
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, register.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Sign In
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        sharedPrefManager.setRememberMe(cbRememberMe.isChecked());

                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        // Proceed to Homepage
                        Intent intent = new Intent(LoginActivity.this, homepage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}