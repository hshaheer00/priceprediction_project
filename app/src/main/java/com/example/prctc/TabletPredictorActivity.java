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

public class TabletPredictorActivity extends AppCompatActivity {

    private EditText etBattery, etRam, etStorage, etScreenSize, etCamera;
    private Button btnPredict;
    private CardView cardResult;
    private TextView tvPredictedPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet_predictor);

        Toolbar toolbar = findViewById(R.id.toolbarTablet);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etBattery = findViewById(R.id.etBatteryTab);
        etRam = findViewById(R.id.etRamTab);
        etStorage = findViewById(R.id.etStorageTab);
        etScreenSize = findViewById(R.id.etScreenSizeTab);
        etCamera = findViewById(R.id.etCameraTab);

        btnPredict = findViewById(R.id.btnPredictTab);
        cardResult = findViewById(R.id.cardResultTab);
        tvPredictedPrice = findViewById(R.id.tvPredictedPriceTab);

        btnPredict.setOnClickListener(v -> predictTabletPrice());
    }

    private void predictTabletPrice() {
        if (etBattery.getText().toString().isEmpty() ||
                etRam.getText().toString().isEmpty() ||
                etStorage.getText().toString().isEmpty() ||
                etScreenSize.getText().toString().isEmpty() ||
                etCamera.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float battery = Float.parseFloat(etBattery.getText().toString());
            float ram = Float.parseFloat(etRam.getText().toString());
            float storage = Float.parseFloat(etStorage.getText().toString());
            float screenSize = Float.parseFloat(etScreenSize.getText().toString());
            float camera = Float.parseFloat(etCamera.getText().toString());

            Interpreter tflite = new Interpreter(loadModelFile("tablet_price_model.tflite"));

            float[][] input = new float[1][5];
            input[0] = new float[]{battery, ram, storage, screenSize, camera};

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
