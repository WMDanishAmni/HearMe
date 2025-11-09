// file: EmergencyHistoryAdapter.java
package com.example.hearme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hearme.R;
import com.example.hearme.models.EmergencyHistoryModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyHistoryAdapter extends RecyclerView.Adapter<EmergencyHistoryAdapter.EmergencyViewHolder> {

    private Context context;
    private List<EmergencyHistoryModel> historyList;

    public EmergencyHistoryAdapter(Context context, List<EmergencyHistoryModel> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emergency_history, parent, false);
        return new EmergencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
        EmergencyHistoryModel item = historyList.get(position);

        holder.tvType.setText(item.getEmergency_type());
        holder.tvDetails.setText(item.getLocation_text());

        // Format and set timestamp
        String timestamp = item.getCreated_at();
        if (timestamp != null && !timestamp.isEmpty()) {
            try {
                // Parse the timestamp from database format
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(timestamp);

                // Format to a more readable format
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                String formattedTimestamp = outputFormat.format(date);

                holder.tvTimestamp.setText(formattedTimestamp);
            } catch (Exception e) {
                holder.tvTimestamp.setText(timestamp); // Fallback
            }
        } else {
            holder.tvTimestamp.setText("No timestamp");
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class EmergencyViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDetails, tvTimestamp;

        public EmergencyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_emergency_type);
            tvDetails = itemView.findViewById(R.id.tv_emergency_details);
            tvTimestamp = itemView.findViewById(R.id.tv_emergency_timestamp);
        }
    }
}