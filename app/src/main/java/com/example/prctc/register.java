package com.example.prctc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etName, etEmail, etPassword, etPhone;
    private AutoCompleteTextView etCountry; // Country dropdown
    private Button btnRegister;
    private TextView tvBackToLogin;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        sharedPrefManager = new SharedPrefManager(this);

        // Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etCountry = findViewById(R.id.etCountry); // new
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Setup country dropdown
        String[] countries = {"Pakistan", "United States", "United Kingdom", "Canada", "India", "Australia", "China", "Japan", "Germany", "France"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countries);
        etCountry.setAdapter(adapter);
        etCountry.setOnClickListener(v -> etCountry.showDropDown()); // show dropdown on click

        // Button listeners
        btnRegister.setOnClickListener(v -> registerUser());
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(register.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String country = etCountry.getText().toString().trim(); // get selected country

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sharedPrefManager.setRememberMe(true); // optional: remember after register
                        Toast.makeText(register.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Send user to MainActivity after register
                        Intent intent = new Intent(register.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(register.this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
