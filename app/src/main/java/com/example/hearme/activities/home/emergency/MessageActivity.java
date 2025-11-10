// file: MessageActivity.java
package com.example.hearme.activities.home.emergency;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hearme.R;
import com.example.hearme.activities.BaseActivity;
import com.example.hearme.models.SessionManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends BaseActivity {

    // --- (FIX 1) Define the new emergency numbers ---
    private static final String POLICE_NUMBER = "047747222";
    private static final String FIREFIGHTER_NUMBER = "047344444";
    private static final String AMBULANCE_NUMBER = "0138889829";

    // This is your computer's IP for saving the record
    private static final String API_URL = "http://10.56.239.233/hear_me_api/save_emergency.php";

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_message);

        sessionManager = new SessionManager(this);
        setupHeader();

        TextView tvPreview = findViewById(R.id.tvPreview);
        Button btnHantar = findViewById(R.id.btnHantar);

        String jenis = getIntent().getStringExtra("jenis");
        String alamat = getIntent().getStringExtra("alamat");
        double lat = getIntent().getDoubleExtra("lat", 0.0);
        double lng = getIntent().getDoubleExtra("lng", 0.0);
        String customName = getIntent().getStringExtra("custom_name");
        String customNumber = getIntent().getStringExtra("custom_number");

        String nama = sessionManager.getFullName();
        if (nama == null || nama.isEmpty()) {
            nama = "User";
        }
        final String finalNama = nama;

        String jenisPesan = getMessageForJenis(jenis, customName);

        String alamatTeks;
        if (alamat != null && !alamat.isEmpty()) {
            alamatTeks = alamat; // From "Alamat Rumah"
        } else {
            alamatTeks = String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", lat, lng); // From "Lokasi Semasa"
        }

        String fullMessage;
        if ("Custom".equals(jenis) && customName != null && customNumber != null) {
            fullMessage = getString(R.string.message_template_custom, finalNama, customName, alamatTeks);
        } else {
            fullMessage = getString(R.string.message_template_standard, finalNama, jenisPesan, alamatTeks);
        }

        tvPreview.setText(fullMessage);

        btnHantar.setOnClickListener(v -> {

            // --- (FIX 2) This is the new logic to select the correct number ---
            String recipient;
            if ("Custom".equals(jenis)) {
                // Use the custom number passed from the last screen
                recipient = (customNumber != null && !customNumber.isEmpty()) ? customNumber : POLICE_NUMBER; // Default to police if custom number is bad
            } else {
                // Otherwise, pick the authority based on 'jenis'
                recipient = getRecipientForJenis(jenis);
            }
            // --- End of Fix ---

            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + Uri.encode(recipient)));
                intent.putExtra("sms_body", fullMessage);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MessageActivity.this, getString(R.string.toast_no_sms_app), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MessageActivity.this, getString(R.string.toast_sms_fail), Toast.LENGTH_SHORT).show();
            }

            saveRecordToServer(finalNama, jenis, alamatTeks, lat, lng, recipient);
        });
    }

    private void setupHeader() {
        View header = findViewById(R.id.header_layout);
        if (header != null) {
            View btnBack = header.findViewById(R.id.btn_back_header);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        }
    }

    // --- (NEW) This helper method returns the correct phone number ---
    private String getRecipientForJenis(String jenis) {
        if (jenis == null) return POLICE_NUMBER; // Default to Police

        switch (jenis) {
            // Police
            case "Instant":
            case "Theft":
                return POLICE_NUMBER;

            // Firefighter / Bomba
            case "Wildlife":
            case "Fire":
            case "Health":
            case "Accident":
                return FIREFIGHTER_NUMBER;

            // Ambulance
            case "Injury":
                return AMBULANCE_NUMBER;

            default:
                return POLICE_NUMBER; // Default for any other case
        }
    }

    private String getMessageForJenis(String jenis, String customName) {
        // (This method is already correct and uses your string resources)
        if (jenis == null) return getString(R.string.message_type_default);
        // ... (rest of the method) ...

        switch (jenis) {
            case "Instant":
                return getString(R.string.message_type_instant);
            case "Accident":
                return getString(R.string.message_type_accident);
            case "Theft":
                return getString(R.string.message_type_theft);
            case "Health":
                return getString(R.string.message_type_health);
            case "Fire":
                return getString(R.string.message_type_fire);
            case "Wildlife":
                return getString(R.string.message_type_wildlife);
            case "Injury":
                return getString(R.string.message_type_injury);
            case "Custom":
                if (customName != null) return getString(R.string.message_type_custom, customName);
                return getString(R.string.message_type_default);
            default:
                return getString(R.string.message_type_default);
        }
    }

    private void saveRecordToServer(String nama, String jenis, String alamatTeks, double lat, double lng, String recipient) {
        // (This method is correct)
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(Request.Method.POST, API_URL,
                response -> Toast.makeText(MessageActivity.this, getString(R.string.toast_record_saved), Toast.LENGTH_SHORT).show(),
                error -> {
                    error.printStackTrace();
                    Toast.makeText(MessageActivity.this, getString(R.string.toast_server_connect_fail), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> p = new HashMap<>();
                p.put("name", nama);
                p.put("type", jenis);
                p.put("address", alamatTeks);
                p.put("lat", String.valueOf(lat));
                p.put("lng", String.valueOf(lng));
                p.put("recipient", recipient);

                if (sessionManager != null && sessionManager.isLoggedIn()) {
                    p.put("user_id", String.valueOf(sessionManager.getUserId()));
                } else {
                    p.put("user_id", "0");
                }

                return p;
            }
        };
        q.add(req);
    }
}