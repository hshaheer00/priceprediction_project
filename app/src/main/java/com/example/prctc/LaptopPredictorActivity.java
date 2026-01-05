package com.example.prctc;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class LaptopPredictorActivity extends AppCompatActivity {

    private EditText etRam, etStorage, etProcessorSpeed, etScreenSize, etGraphics;
    private Button btnPredict;
    private CardView cardResult;
    private TextView tvPredictedPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laptop_predictor);

        Toolbar toolbar = findViewById(R.id.toolbarLaptop);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etRam = findViewById(R.id.etRam);
        etStorage = findViewById(R.id.etStorage);
        etProcessorSpeed = findViewById(R.id.etProcessorSpeed);
        etScreenSize = findViewById(R.id.etScreenSize);
        etGraphics = findViewById(R.id.etGraphics);

        btnPredict = findViewById(R.id.btnPredict);
        cardResult = findViewById(R.id.cardResult);
        tvPredictedPrice = findViewById(R.id.tvPredictedPrice);

        btnPredict.setOnClickListener(v -> predictPrice());
    }

    private void predictPrice() {
        // Input validation
        if (etRam.getText().toString().isEmpty() ||
                etStorage.getText().toString().isEmpty() ||
                etProcessorSpeed.getText().toString().isEmpty() ||
                etScreenSize.getText().toString().isEmpty() ||
                etGraphics.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float ram = Float.parseFloat(etRam.getText().toString());
            float storage = Float.parseFloat(etStorage.getText().toString());
            float processorSpeed = Float.parseFloat(etProcessorSpeed.getText().toString());
            float screenSize = Float.parseFloat(etScreenSize.getText().toString());
            float graphics = Float.parseFloat(etGraphics.getText().toString());

            Interpreter tflite = new Interpreter(loadModelFile("laptop_price_model.tflite"));

            float[][] input = new float[1][5];
            input[0] = new float[]{ram, storage, processorSpeed, screenSize, graphics};

            float[][] output = new float[1][1];
            tflite.run(input, output);
            tflite.close();

            tvPredictedPrice.setText("₹ " + String.format("%.2f", output[0][0]));
            cardResult.setVisibility(android.view.View.VISIBLE);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Model error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private MappedByteBuffer loadModelFile(String modelFileName) throws Exception {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
