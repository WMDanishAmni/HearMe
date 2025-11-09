// file: LocationTypeActivity.java
package com.example.hearme.activities.home.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.models.SessionManager;

public class LocationTypeActivity extends AppCompatActivity {

    private static final int REQ_MAP = 3001;
    private SessionManager sessionManager;
    private String alamatRumah;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_location_type);

        sessionManager = new SessionManager(this);

        String jenis = getIntent().getStringExtra("jenis");
        String customName = getIntent().getStringExtra("custom_name");
        String customNumber = getIntent().getStringExtra("custom_number");

        View btnSemasa = findViewById(R.id.btnLokasiSemasa);
        View btnRumah = findViewById(R.id.btnAlamatRumah);
        TextView tvAlamatRumah = findViewById(R.id.tvAlamatRumah);

        // Call the setup methods
        setupHeader();
        loadHomeAddress(tvAlamatRumah);

        btnSemasa.setOnClickListener(v -> {
            Intent i = new Intent(LocationTypeActivity.this, MapsActivity.class);
            i.putExtra("jenis", jenis);
            if (customName != null) i.putExtra("custom_name", customName);
            if (customNumber != null) i.putExtra("custom_number", customNumber);
            startActivityForResult(i, REQ_MAP);
        });

        btnRumah.setOnClickListener(v -> {
            if (alamatRumah == null || alamatRumah.isEmpty()) {
                Toast.makeText(this, "No home address is saved in your profile.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent i = new Intent(LocationTypeActivity.this, MessageActivity.class);
            i.putExtra("jenis", jenis);
            i.putExtra("alamat", alamatRumah);
            i.putExtra("lat", 0.0);
            i.putExtra("lng", 0.0);
            if (customName != null) i.putExtra("custom_name", customName);
            if (customNumber != null) i.putExtra("custom_number", customNumber);
            startActivity(i);
        });
    }

    private void setupHeader() {
        View header = findViewById(R.id.header_layout);
        if (header != null) {
            // --- (FIX) Using the correct ID from your header.xml ---
            View btnBack = header.findViewById(R.id.btn_back_header);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        }
    }

    private void loadHomeAddress(TextView tvAlamatRumah) {
        alamatRumah = sessionManager.getAddress();
        if (alamatRumah != null && !alamatRumah.isEmpty()) {
            tvAlamatRumah.setText(alamatRumah);
        } else {
            tvAlamatRumah.setText("(No location saved)");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // (This function is correct)
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_MAP) {
            if (resultCode == RESULT_OK && data != null) {
                double lat = data.getDoubleExtra("lat", 0.0);
                double lng = data.getDoubleExtra("lng", 0.0);

                Intent i = new Intent(LocationTypeActivity.this, MessageActivity.class);
                i.putExtra("jenis", getIntent().getStringExtra("jenis"));
                i.putExtra("alamat", "");
                i.putExtra("lat", lat);
                i.putExtra("lng", lng);

                String customName = getIntent().getStringExtra("custom_name");
                String customNumber = getIntent().getStringExtra("custom_number");
                if (customName != null) i.putExtra("custom_name", customName);
                if (customNumber != null) i.putExtra("custom_number", customNumber);

                startActivity(i);
            } else {
                Toast.makeText(this, "No location found. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }
}