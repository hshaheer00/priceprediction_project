package com.example.prctc;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.prctc.ml.MobilePriceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MobilePredictorActivity extends AppCompatActivity {

    private Spinner spinnerBrand, spinnerRam, spinnerStorage, spinnerBattery;
    private EditText etCamera;
    private Button btnPredict;
    private CardView cardResult;
    private TextView tvPredictedPrice;
    
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_predictor);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Predictions");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        spinnerBrand = findViewById(R.id.spinnerMobileBrand);
        spinnerRam = findViewById(R.id.spinnerMobileRam);
        spinnerStorage = findViewById(R.id.spinnerMobileStorage);
        spinnerBattery = findViewById(R.id.spinnerMobileBattery);
        etCamera = findViewById(R.id.etMobileCamera);
        btnPredict = findViewById(R.id.btnPredictMobile);
        cardResult = findViewById(R.id.cardResultMobile);
        tvPredictedPrice = findViewById(R.id.tvPredictedPriceMobile);

        setupSpinners();

        btnPredict.setOnClickListener(v -> predictMobilePrice());
    }

    private void setupSpinners() {
        String[] brands = {"Select Brand", "Samsung", "Apple", "Xiaomi", "Oppo", "Vivo", "Google", "OnePlus", "Realme"};
        String[] rams = {"Select RAM (GB)", "2", "3", "4", "6", "8", "12", "16"};
        String[] storage = {"Select Storage (GB)", "16", "32", "64", "128", "256", "512", "1024"};
        String[] battery = {"Select Battery (mAh)", "3000", "4000", "5000", "6000"};

        spinnerBrand.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands));
        spinnerRam.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rams));
        spinnerStorage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, storage));
        spinnerBattery.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, battery));
    }

    private void predictMobilePrice() {
        if (spinnerBrand.getSelectedItemPosition() == 0 ||
            spinnerRam.getSelectedItemPosition() == 0 ||
            spinnerStorage.getSelectedItemPosition() == 0 ||
            spinnerBattery.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select all specifications", Toast.LENGTH_SHORT).show();
            return;
        }

        String cameraStr = etCamera.getText().toString();
        if (cameraStr.isEmpty()) {
            Toast.makeText(this, "Please enter camera MP", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MobilePriceModel model = MobilePriceModel.newInstance(this);

            // Create input buffer. Assuming 5 features: Brand_Index, RAM, Storage, Battery, Camera
            // IMPORTANT: The order must match your training data!
            float[] inputFeatures = new float[5];
            inputFeatures[0] = (float) spinnerBrand.getSelectedItemPosition(); // Index of brand
            inputFeatures[1] = Float.parseFloat(spinnerRam.getSelectedItem().toString());
            inputFeatures[2] = Float.parseFloat(spinnerStorage.getSelectedItem().toString());
            inputFeatures[3] = Float.parseFloat(spinnerBattery.getSelectedItem().toString());
            inputFeatures[4] = Float.parseFloat(cameraStr);

            TensorBuffer inputFeatureBuffer = TensorBuffer.createFixedSize(new int[]{1, 5}, DataType.FLOAT32);
            inputFeatureBuffer.loadArray(inputFeatures);

            // Runs model inference and gets result.
            MobilePriceModel.Outputs outputs = model.process(inputFeatureBuffer);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float predictedPrice = outputFeature0.getFloatArray()[0];
            String priceString = String.format("$%.2f", predictedPrice);

            cardResult.setVisibility(View.VISIBLE);
            tvPredictedPrice.setText(priceString);

            savePrediction("Mobile", spinnerBrand.getSelectedItem().toString() + ", RAM: " + inputFeatures[1] + "GB, Storage: " + inputFeatures[2] + "GB, Camera: " + inputFeatures[4] + "MP", priceString);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error loading model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Prediction failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePrediction(String productType, String details, String price) {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            String predictionId = mDatabase.child(userId).push().getKey();
            PredictionRecord record = new PredictionRecord(productType, details, price, System.currentTimeMillis());
            if (predictionId != null) {
                mDatabase.child(userId).child(predictionId).setValue(record);
            }
        }
    }
}
