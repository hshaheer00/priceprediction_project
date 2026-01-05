package com.example.prctc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity
        implements HistoryAdapter.OnDeleteClickListener {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<PredictionRecord> historyList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnClearAll;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbarHistory);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvHistory = findViewById(R.id.rvHistory);
        progressBar = findViewById(R.id.pbHistory);
        tvEmpty = findViewById(R.id.tvEmptyHistory);
        btnClearAll = findViewById(R.id.btnClearAll);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance()
                .getReference("Predictions")
                .child(mAuth.getCurrentUser().getUid());

        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(historyList, this);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        fetchHistory();

        btnClearAll.setOnClickListener(v -> clearAllHistory());
    }

    private void fetchHistory() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    PredictionRecord record = snap.getValue(PredictionRecord.class);
                    if (record != null) {
                        record.key = snap.getKey();
                        historyList.add(record);
                    }
                }

                Collections.sort(historyList,
                        (a, b) -> Long.compare(b.timestamp, a.timestamp));

                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
                btnClearAll.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HistoryActivity.this,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(PredictionRecord record) {
        if (record.key == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete this prediction?")
                .setPositiveButton("Yes",
                        (d, w) -> mDatabase.child(record.key).removeValue())
                .setNegativeButton("No", null)
                .show();
    }

    private void clearAllHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All")
                .setMessage("Delete all history?")
                .setPositiveButton("Yes",
                        (d, w) -> mDatabase.removeValue())
                .setNegativeButton("No", null)
                .show();
    }
}
