// file: ChatHistoryAdapter.java
package com.example.hearme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hearme.R;
import com.example.hearme.models.ChatHistoryModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder> {

    private Context context;
    private List<ChatHistoryModel> chatHistoryList;
    private OnItemClickListener listener;

    // UPDATED: Added onItemLongClick to interface
    public interface OnItemClickListener {
        void onItemClick(ChatHistoryModel chatItem);
        void onItemLongClick(ChatHistoryModel chatItem, int position); // NEW
    }

    public ChatHistoryAdapter(Context context, List<ChatHistoryModel> chatHistoryList, OnItemClickListener listener) {
        this.context = context;
        this.chatHistoryList = chatHistoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_history, parent, false);
        return new ChatHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHistoryViewHolder holder, int position) {
        ChatHistoryModel chatItem = chatHistoryList.get(position);

        // Set chat preview (using hear text as preview)
        String hearText = chatItem.getHear();
        if (hearText != null) {
            hearText = hearText.replace("\n", " "); // Replace newlines with spaces
            if (hearText.length() > 100) {
                hearText = hearText.substring(0, 100) + "..."; // Truncate if too long
            }
        }

        holder.tvChatPreview.setText(hearText);

        // Format and set timestamp
        String timestamp = chatItem.getTimestamp();
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
                // If parsing fails, just show the original timestamp
                holder.tvTimestamp.setText(timestamp);
            }
        } else {
            holder.tvTimestamp.setText("No timestamp");
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(chatItem);
            }
        });

        // NEW: Set long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(chatItem, position);
            }
            return true; // Indicate that the long click was handled
        });
    }

    @Override
    public int getItemCount() {
        return chatHistoryList.size();
    }

    public static class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatPreview;
        TextView tvTimestamp;

        public ChatHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatPreview = itemView.findViewById(R.id.tv_chat_preview);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}