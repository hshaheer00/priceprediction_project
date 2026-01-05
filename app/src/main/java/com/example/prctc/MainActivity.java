package com.example.prctc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardLaptop, cardMobile, cardTablet;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize FirebaseAuth and SharedPrefManager
        mAuth = FirebaseAuth.getInstance();
        sharedPrefManager = new SharedPrefManager(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);

        // Cards
        cardLaptop = findViewById(R.id.cardLaptop);
        cardMobile = findViewById(R.id.cardMobile);
        cardTablet = findViewById(R.id.cardTablet);

        // Logout button
        btnLogout = findViewById(R.id.btnLogout);

        // Card click listeners
        cardLaptop.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LaptopPredictorActivity.class)));

        cardMobile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MobilePredictorActivity.class)));

        cardTablet.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TabletPredictorActivity.class)));

        // Logout click listener
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Firebase logout
            sharedPrefManager.setRememberMe(false); // clear remember me
            sharedPrefManager.clear(); // clear saved user data

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
