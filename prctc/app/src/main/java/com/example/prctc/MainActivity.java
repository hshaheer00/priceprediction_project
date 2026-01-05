package com.example.prctc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout, btnPredict;
    private static final String PREFS_NAME = "UserPrefs";
    private ApiService apiService;
    private DatabaseReference firebaseRef; // Reference to Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. Initialize Firebase ---
        // Access the "predictions" node in your database
        firebaseRef = FirebaseDatabase.getInstance().getReference("predictions");

        // --- 2. Initialize Retrofit ---
        // 10.0.2.2 is the alias to your laptop's localhost for the Android Emulator
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8010/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // --- 3. Initialize UI Components ---
        btnLogout = findViewById(R.id.btnLogout);
        btnPredict = findViewById(R.id.btnPredict);

        // --- 4. Prediction Logic ---
        btnPredict.setOnClickListener(v -> performPrediction());

        // --- 5. Logout Logic ---
        btnLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("rememberMe", false);
            editor.apply();

            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void performPrediction() {
        // Data to send to Python (Battery, RAM, Memory, Screen, Camera)
        MobileRequest request = new MobileRequest(1500, 4096, 64, 6.4f, 48);

        // Execute API call
        apiService.predictMobile(request).enqueue(new Callback<PriceResponse>() {
            @Override
            public void onResponse(Call<PriceResponse> call, Response<PriceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int priceRange = response.body().price_range;

                    // --- SAVE TO FIREBASE ---
                    // Creates a unique ID for each prediction
                    firebaseRef.push().setValue("Mobile Price Range: " + priceRange);

                    Toast.makeText(MainActivity.this, "Predicted Price: " + priceRange, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PriceResponse> call, Throwable t) {
                // If this fails, ensure your Python server is running in VS Code
                Toast.makeText(MainActivity.this, "Connection Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}