package com.example.prctc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<PredictionRecord> historyList;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(PredictionRecord record);
    }

    public HistoryAdapter(List<PredictionRecord> historyList, OnDeleteClickListener deleteClickListener) {
        this.historyList = historyList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredictionRecord record = historyList.get(position);
        holder.tvType.setText(record.productType);
        holder.tvDetails.setText(record.details);
        holder.tvPrice.setText(record.predictedPrice);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(record.timestamp)));

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDetails, tvPrice, tvDate;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvHistType);
            tvDetails = itemView.findViewById(R.id.tvHistDetails);
            tvPrice = itemView.findViewById(R.id.tvHistPrice);
            tvDate = itemView.findViewById(R.id.tvHistDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
