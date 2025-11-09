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
import com.example.hearme.models.SessionManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private static final String DEFAULT_RECIPIENT = "01124206586";
    private static final String API_URL = "http://10.119.89.233/hear_me_api/save_emergency.php";

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

        // This variable holds the correct, formatted address
        String alamatTeks;
        if (alamat != null && !alamat.isEmpty()) {
            alamatTeks = alamat; // From "Alamat Rumah"
        } else {
            alamatTeks = String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", lat, lng); // From "Lokasi Semasa"
        }

        String fullMessage;
        if ("Custom".equals(jenis) && customName != null && customNumber != null) {
            fullMessage = "Nama saya " + finalNama + ". Saya ialah orang pekak. " + "Ini mesej kepada " + customName + ". \n\nAlamat saya adalah: " + alamatTeks;
        } else {
            fullMessage = "Nama saya " + finalNama + ". Saya ialah orang pekak. " + jenisPesan + " \n\nAlamat saya adalah: " + alamatTeks;
        }

        tvPreview.setText(fullMessage);

        btnHantar.setOnClickListener(v -> {
            String recipient = (customNumber != null && !customNumber.isEmpty()) ? customNumber : DEFAULT_RECIPIENT;

            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + Uri.encode(recipient)));
                intent.putExtra("sms_body", fullMessage);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MessageActivity.this, "No SMS app found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MessageActivity.this, "Failed to open SMS", Toast.LENGTH_SHORT).show();
            }

            // We pass the correct 'alamatTeks' variable here
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

    private String getMessageForJenis(String jenis, String customName) {
        if (jenis == null) return "Saya memerlukan bantuan.";

        switch (jenis) {
            case "Instant":
                return "Saya memerlukan bantuan segera.";
            case "Accident":
                return "Saya terlibat dalam kemalangan. Perlukan bantuan kecemasan segera.";
            case "Theft":
                return "Terdapat kejadian pencurian. Mohon bantuan polis segera.";
            case "Health":
                return "Saya mempunyai kecemasan perubatan. Perlukan ambulans.";
            case "Fire":
                return "Terdapat kebakaran. Sila hantarkan bantuan bomba.";
            case "Wildlife":
                return "Terdapat serangan haiwan liar. Saya perlukan bantuan segera.";
            case "Injury":
                return "Saya mengalami kecederaan. Mohon bantuan perubatan.";
            case "Custom":
                if (customName != null) return "Saya ingin menghubungi " + customName + ".";
                return "Saya memerlukan bantuan.";
            default:
                return "Saya memerlukan bantuan.";
        }
    }

    // ⭐️ --- THIS IS THE FIX --- ⭐️
    // The parameter name is changed from 'alamat' to 'alamatTeks'
    private void saveRecordToServer(String nama, String jenis, String alamatTeks, double lat, double lng, String recipient) {
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(Request.Method.POST, API_URL,
                response -> Toast.makeText(MessageActivity.this, "Record Saved", Toast.LENGTH_SHORT).show(),
                error -> {
                    error.printStackTrace();
                    Toast.makeText(MessageActivity.this, "Fail to connect to the server.", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> p = new HashMap<>();
                p.put("name", nama);
                p.put("type", jenis);
                p.put("address", alamatTeks); // ⭐️ Now uses the correct variable
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