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

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnDeleteClickListener {

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvHistory = findViewById(R.id.rvHistory);
        progressBar = findViewById(R.id.pbHistory);
        tvEmpty = findViewById(R.id.tvEmptyHistory);
        btnClearAll = findViewById(R.id.btnClearAll);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("Predictions")
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
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    PredictionRecord record = postSnapshot.getValue(PredictionRecord.class);
                    if (record != null) {
                        record.key = postSnapshot.getKey();
                        historyList.add(record);
                    }
                }
                
                Collections.sort(historyList, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));
                
                progressBar.setVisibility(View.GONE);
                if (historyList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    btnClearAll.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    btnClearAll.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HistoryActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(PredictionRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("Delete History")
                .setMessage("Are you sure you want to delete this prediction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDatabase.child(record.key).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(HistoryActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HistoryActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All History")
                .setMessage("This will permanently delete ALL your prediction history. Proceed?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    mDatabase.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(HistoryActivity.this, "History cleared", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HistoryActivity.this, "Failed to clear history", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
