package com.example.prctc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Splashscreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        mAuth = FirebaseAuth.getInstance();
        sharedPrefManager = new SharedPrefManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Option B: Check Firebase + SharedPref "Remember Me"
            if (sharedPrefManager.isRememberMe() && mAuth.getCurrentUser() != null) {
                startActivity(new Intent(Splashscreen.this, MainActivity.class));
            } else {
                startActivity(new Intent(Splashscreen.this, LoginActivity.class));
            }
            finish();
        }, 2000); // 2 seconds splash delay
    }
}
