package com.example.prctc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.io.IOException;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.prctc.ml.LaptopPriceModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class homepage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner spinnerBrand, spinnerType, spinnerRam, spinnerCpu, spinnerSsd, spinnerGpu;
    private EditText etWeight;
    private CheckBox cbTouchscreen, cbIps;
    private Button btnPredict, btnLogoutHome;
    private ImageButton btnPower, btnMenu;
    private CardView cardResult;
    private TextView tvPredictedPrice, tvWelcomeHome;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private SharedPrefManager sharedPrefManager;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private final String DB_URL = "https://prctc-5b113-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        sharedPrefManager = new SharedPrefManager(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("Predictions");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnMenu = findViewById(R.id.btnMenu);
        tvWelcomeHome = findViewById(R.id.tvWelcomeHome);
        btnPower = findViewById(R.id.btnPower);

        spinnerBrand = findViewById(R.id.spinnerBrand);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerRam = findViewById(R.id.spinnerRam);
        spinnerCpu = findViewById(R.id.spinnerCpu);
        spinnerSsd = findViewById(R.id.spinnerSsd);
        spinnerGpu = findViewById(R.id.spinnerGpu);
        etWeight = findViewById(R.id.etWeight);
        cbTouchscreen = findViewById(R.id.cbTouchscreen);
        cbIps = findViewById(R.id.cbIps);
        btnPredict = findViewById(R.id.btnPredict);
        btnLogoutHome = findViewById(R.id.btnLogoutHome);
        cardResult = findViewById(R.id.cardResult);
        tvPredictedPrice = findViewById(R.id.tvPredictedPrice);

        navigationView.setNavigationItemSelectedListener(this);

        if (navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);
            TextView navName = headerView.findViewById(R.id.nav_header_name);
            TextView navEmail = headerView.findViewById(R.id.nav_header_email);

            if (navName != null) navName.setText(sharedPrefManager.getName());
            if (navEmail != null) navEmail.setText(sharedPrefManager.getEmail());
        }

        String userName = sharedPrefManager.getName();
        tvWelcomeHome.setText("Hello, " + (userName != null ? userName : "User") + "!");

        setupSpinners();

        btnPredict.setOnClickListener(v -> predictPrice());

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        View.OnClickListener logoutListener = v -> {
            sharedPrefManager.logout();
            mAuth.signOut();
            Intent intent = new Intent(homepage.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        };

        btnLogoutHome.setOnClickListener(logoutListener);
        btnPower.setOnClickListener(logoutListener);
    }

    private void setupSpinners() {
        String[] brands = {"Select Brand", "Apple", "HP", "Dell", "Lenovo", "Asus", "MSI", "Acer", "Toshiba", "Razer"};
        String[] types = {"Select Type", "Ultrabook", "Notebook", "Gaming", "2 in 1 Convertible", "Workstation", "Netbook"};
        String[] rams = {"Select RAM", "2", "4", "6", "8", "12", "16", "24", "32", "64"};
        String[] cpus = {"Select CPU", "Intel Core i7", "Intel Core i5", "Intel Core i3", "AMD Ryzen 7", "AMD Ryzen 5", "Apple M1", "Apple M2"};
        String[] ssds = {"Select SSD", "0", "128", "256", "512", "1024", "2048"};
        String[] gpus = {"Select GPU", "Intel Integrated", "AMD Radeon", "Nvidia GTX", "Nvidia RTX 3050", "Nvidia RTX 3060", "Nvidia RTX 4070"};

        spinnerBrand.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands));
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
        spinnerRam.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rams));
        spinnerCpu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cpus));
        spinnerSsd.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ssds));
        spinnerGpu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gpus));
    }

    private void predictPrice() {
        if (spinnerBrand.getSelectedItemPosition() == 0 ||
                spinnerType.getSelectedItemPosition() == 0 ||
                spinnerRam.getSelectedItemPosition() == 0 ||
                spinnerCpu.getSelectedItemPosition() == 0 ||
                spinnerSsd.getSelectedItemPosition() == 0 ||
                spinnerGpu.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select all specifications", Toast.LENGTH_SHORT).show();
            return;
        }

        String weightStr = etWeight.getText().toString();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 1. Initialize TFLite model
            LaptopPriceModel model = LaptopPriceModel.newInstance(this);

            // 2. Prepare inputs (assuming 9 features in this specific order)
            // Brand, Type, Ram, Cpu, Ssd, Gpu, Weight, Touchscreen, Ips
            float[] inputFeatures = new float[9];
            inputFeatures[0] = (float) spinnerBrand.getSelectedItemPosition();
            inputFeatures[1] = (float) spinnerType.getSelectedItemPosition();
            inputFeatures[2] = Float.parseFloat(spinnerRam.getSelectedItem().toString());
            inputFeatures[3] = (float) spinnerCpu.getSelectedItemPosition();
            inputFeatures[4] = Float.parseFloat(spinnerSsd.getSelectedItem().toString());
            inputFeatures[5] = (float) spinnerGpu.getSelectedItemPosition();
            inputFeatures[6] = Float.parseFloat(weightStr);
            inputFeatures[7] = cbTouchscreen.isChecked() ? 1.0f : 0.0f;
            inputFeatures[8] = cbIps.isChecked() ? 1.0f : 0.0f;

            TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 9}, DataType.FLOAT32);
            inputBuffer.loadArray(inputFeatures);

            // 3. Run inference
            LaptopPriceModel.Outputs outputs = model.process(inputBuffer);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float predictedPrice = outputFeature0.getFloatArray()[0];
            String priceString = String.format("$%.2f", predictedPrice);

            // 4. Update UI
            tvPredictedPrice.setText("Predicted Price: " + priceString);
            cardResult.setVisibility(View.VISIBLE);

            // 5. Save history
            String details = spinnerBrand.getSelectedItem().toString() + ", " + 
                             spinnerRam.getSelectedItem().toString() + "GB RAM, " + 
                             spinnerCpu.getSelectedItem().toString();
            savePrediction("Laptop", details, priceString);

            // 6. Close model
            model.close();

        } catch (IOException e) {
            Toast.makeText(this, "Error loading model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Prediction failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePrediction(String productType, String details, String price) {
        if (mAuth.getCurrentUser() != null) {
            DatabaseReference globalHistoryRef = FirebaseDatabase.getInstance(DB_URL).getReference("All_History");
            String predictionId = globalHistoryRef.push().getKey();
            String userName = sharedPrefManager.getName();

            HashMap<String, Object> historyData = new HashMap<>();
            historyData.put("userName", userName != null ? userName : "User");
            historyData.put("productType", productType);
            historyData.put("details", details);
            historyData.put("price", price);
            historyData.put("timestamp", System.currentTimeMillis());

            if (predictionId != null) {
                globalHistoryRef.child(predictionId).setValue(historyData)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Global history updated successfully"))
                        .addOnFailureListener(e -> Toast.makeText(homepage.this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mobile) {
            startActivity(new Intent(homepage.this, MobilePredictorActivity.class));
        } else if (id == R.id.nav_tablet) {
            startActivity(new Intent(homepage.this, TabletPredictorActivity.class));
        } else if (id == R.id.nav_laptop) {
            Toast.makeText(this, "You are already here!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(homepage.this, HistoryActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
