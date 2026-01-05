package com.example.prctc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class register extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    EditText etName, etEmail, etPassword, etPhone;
    AutoCompleteTextView etCountry;
    Button btnRegister;
    TextView tvBackToLogin;
    SharedPrefManager sharedPrefManager;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UPDATED: This matches your Logcat error exactly
    private final String DB_URL = "https://prctc-5b113-default-rtdb.asia-southeast1.firebasedatabase.app";

    String[] countries = {"Pakistan", "United States", "United Kingdom", "Canada", "India", "Australia"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // FIX: Using the correct regional URL for Singapore
        try {
            mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("Users");
        } catch (Exception e) {
            Log.e(TAG, "Database Initialization Failed: " + e.getMessage());
        }

        sharedPrefManager = new SharedPrefManager(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etCountry = findViewById(R.id.etCountry);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);
        etCountry.setAdapter(adapter);

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
        String country = etCountry.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Creating a HashMap to ensure data saves even if User class has issues
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("country", country);

                        mDatabase.child(userId).setValue(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    if (progressDialog.isShowing()) progressDialog.dismiss();

                                    if (dbTask.isSuccessful()) {
                                        sharedPrefManager.saveUser(name, email, password, phone, country);
                                        sharedPrefManager.setRememberMe(true);
                                        Toast.makeText(register.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(register.this, homepage.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If this fails, it's usually a "Rules" or "Region" issue
                                        Log.e(TAG, "Database Write Failed", dbTask.getException());
                                        Toast.makeText(register.this, "Database Error: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(register.this, "Email already registered.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(register.this, "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Log.e(TAG, "Registration Failure", e);
                    Toast.makeText(register.this, "Network/Server Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}