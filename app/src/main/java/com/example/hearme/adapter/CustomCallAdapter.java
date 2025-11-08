package com.example.hearme.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hearme.R;
import com.example.hearme.models.CustomCallModel;

import java.util.List;

public class CustomCallAdapter extends RecyclerView.Adapter<CustomCallAdapter.ViewHolder> {

    private List<CustomCallModel> contactList; // This list is managed by AddCustomCallActivity
    private final OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(CustomCallModel contact, int position);
    }

    public CustomCallAdapter(List<CustomCallModel> contactList, OnDeleteClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_call, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomCallModel contact = contactList.get(position);
        holder.tvName.setText(contact.getCustom_name());
        holder.tvNumber.setText(contact.getCustom_number());

        holder.btnDelete.setOnClickListener(v ->
                listener.onDeleteClick(contact, holder.getAdapterPosition())
        );
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // ⭐️ The updateList() and removeItem() methods are removed.
    // The Activity will now modify the list directly and call
    // adapter.notifyDataSetChanged() or adapter.notifyItemRemoved().
    // This is a cleaner pattern and avoids reference bugs.

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvNumber = itemView.findViewById(R.id.tv_contact_number);
            btnDelete = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}