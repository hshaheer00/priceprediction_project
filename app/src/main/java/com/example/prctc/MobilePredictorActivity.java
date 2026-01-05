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

public class MobilePredictorActivity extends AppCompatActivity {

    private EditText etBattery, etRam, etInternalMemory, etScreenSize, etCamera;
    private Button btnPredict;
    private CardView cardResult;
    private TextView tvPredictedPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_predictor);

        Toolbar toolbar = findViewById(R.id.toolbarMobile);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etBattery = findViewById(R.id.etBattery);
        etRam = findViewById(R.id.etRam);
        etInternalMemory = findViewById(R.id.etInternalMemory);
        etScreenSize = findViewById(R.id.etScreenSize);
        etCamera = findViewById(R.id.etCamera);

        btnPredict = findViewById(R.id.btnPredictMobile);
        cardResult = findViewById(R.id.cardResultMobile);
        tvPredictedPrice = findViewById(R.id.tvPredictedPriceMobile);

        btnPredict.setOnClickListener(v -> predictPrice());
    }

    private void predictPrice() {
        if (etBattery.getText().toString().isEmpty() ||
                etRam.getText().toString().isEmpty() ||
                etInternalMemory.getText().toString().isEmpty() ||
                etScreenSize.getText().toString().isEmpty() ||
                etCamera.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float battery = Float.parseFloat(etBattery.getText().toString());
            float ram = Float.parseFloat(etRam.getText().toString());
            float memory = Float.parseFloat(etInternalMemory.getText().toString());
            float screen = Float.parseFloat(etScreenSize.getText().toString());
            float camera = Float.parseFloat(etCamera.getText().toString());

            Interpreter tflite = new Interpreter(loadModelFile("mobile_price_model.tflite"));

            float[][] input = new float[1][5];
            input[0] = new float[]{battery, ram, memory, screen, camera};

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
