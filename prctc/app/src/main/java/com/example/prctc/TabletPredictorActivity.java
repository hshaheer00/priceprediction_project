package com.example.prctc;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.prctc.ml.TabletPriceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TabletPredictorActivity extends AppCompatActivity {

    private Spinner spinnerBrand, spinnerScreen, spinnerRam, spinnerStorage;
    private Button btnPredict;
    private CardView cardResult;
    private TextView tvPredictedPrice;
    
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet_predictor);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Predictions");

        Toolbar toolbar = findViewById(R.id.toolbarTab);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        spinnerBrand = findViewById(R.id.spinnerTabBrand);
        spinnerScreen = findViewById(R.id.spinnerTabScreen);
        spinnerRam = findViewById(R.id.spinnerTabRam);
        spinnerStorage = findViewById(R.id.spinnerTabStorage);
        btnPredict = findViewById(R.id.btnPredictTab);
        cardResult = findViewById(R.id.cardResultTab);
        tvPredictedPrice = findViewById(R.id.tvPredictedPriceTab);

        setupSpinners();

        btnPredict.setOnClickListener(v -> predictTabletPrice());
    }

    private void setupSpinners() {
        String[] brands = {"Select Brand", "Apple (iPad)", "Samsung (Galaxy Tab)", "Lenovo", "Amazon (Fire)", "Microsoft (Surface)", "Huawei"};
        String[] screenSizes = {"Select Screen Size", "7", "8", "10", "11", "12"}; // simplified to numbers for model
        String[] rams = {"Select RAM", "2", "3", "4", "6", "8", "12", "16"};
        String[] storage = {"Select Storage", "32", "64", "128", "256", "512", "1024"};

        spinnerBrand.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands));
        spinnerScreen.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, screenSizes));
        spinnerRam.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rams));
        spinnerStorage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, storage));
    }

    private void predictTabletPrice() {
        if (spinnerBrand.getSelectedItemPosition() == 0 ||
            spinnerScreen.getSelectedItemPosition() == 0 ||
            spinnerRam.getSelectedItemPosition() == 0 ||
            spinnerStorage.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select all specifications", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            TabletPriceModel model = TabletPriceModel.newInstance(this);

            // Assuming 4 features: Brand_Index, Screen_Size, RAM, Storage
            float[] inputFeatures = new float[4];
            inputFeatures[0] = (float) spinnerBrand.getSelectedItemPosition();
            inputFeatures[1] = Float.parseFloat(spinnerScreen.getSelectedItem().toString());
            inputFeatures[2] = Float.parseFloat(spinnerRam.getSelectedItem().toString());
            inputFeatures[3] = Float.parseFloat(spinnerStorage.getSelectedItem().toString());

            TensorBuffer inputFeatureBuffer = TensorBuffer.createFixedSize(new int[]{1, 4}, DataType.FLOAT32);
            inputFeatureBuffer.loadArray(inputFeatures);

            TabletPriceModel.Outputs outputs = model.process(inputFeatureBuffer);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float predictedPrice = outputFeature0.getFloatArray()[0];
            String priceString = String.format("$%.2f", predictedPrice);

            cardResult.setVisibility(View.VISIBLE);
            tvPredictedPrice.setText(priceString);

            savePrediction("Tablet", spinnerBrand.getSelectedItem().toString() + ", Screen: " + inputFeatures[1] + "\", RAM: " + inputFeatures[2] + "GB, Storage: " + inputFeatures[3] + "GB", priceString);

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
