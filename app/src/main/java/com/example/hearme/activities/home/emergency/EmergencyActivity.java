package com.example.hearme.activities.home.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView; // ⭐️ IMPORT
import android.widget.LinearLayout;
import android.widget.TextView; // ⭐️ IMPORT
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hearme.R;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity; // ⭐️ IMPORT
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.EmergencyApiService;
import com.example.hearme.models.CustomCallModel;
import com.example.hearme.models.CustomCallResponseModel;
import com.example.hearme.models.SessionManager; // ⭐️ IMPORT

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyActivity extends AppCompatActivity {

    private Button btnSegera;
    private ImageView btnBack;
    private LinearLayout containerCustomButtons;
    private Button btnAddCustomCall;

    private SessionManager sessionManager; // This was already here
    private static final String TAG = "EmergencyActivity";

    private static final int MAX_CUSTOM_CONTACTS = 5;
    private static final int REQUEST_CODE_ADD_CONTACT = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        sessionManager = new SessionManager(this); // Already here

        // --- Find & Set Back Button ---
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Find views
        btnSegera = findViewById(R.id.btnSegera);
        containerCustomButtons = findViewById(R.id.containerCustomButtons);
        btnAddCustomCall = findViewById(R.id.btn_add_custom_call);

        ConstraintLayout accidentCell = findViewById(R.id.btn_accident_cell);
        ConstraintLayout theftCell = findViewById(R.id.btn_theft_cell);
        ConstraintLayout healthCell = findViewById(R.id.btn_health_cell);
        ConstraintLayout fireCell = findViewById(R.id.btn_fire_cell);
        ConstraintLayout wildlifeCell = findViewById(R.id.btn_wildlife_cell);
        ConstraintLayout injuryCell = findViewById(R.id.btn_injury_cell);

        // Listeners (no change)
        btnSegera.setOnClickListener(v -> startLocationTypeFlow("Segera", null, null));
        accidentCell.setOnClickListener(v -> startLocationTypeFlow("Kemalangan", null, null));
        theftCell.setOnClickListener(v -> startLocationTypeFlow("Pencurian", null, null));
        healthCell.setOnClickListener(v -> startLocationTypeFlow("Kesihatan", null, null));
        fireCell.setOnClickListener(v -> startLocationTypeFlow("Kebakaran", null, null));
        wildlifeCell.setOnClickListener(v -> startLocationTypeFlow("Serangan Haiwan", null, null));
        injuryCell.setOnClickListener(v -> startLocationTypeFlow("Cedera", null, null));

        btnAddCustomCall.setOnClickListener(v -> {
            Toast.makeText(this, "Add Contact page will be built next!", Toast.LENGTH_SHORT).show();
            // TODO:
            // Intent addIntent = new Intent(EmergencyActivity.this, AddCustomCallActivity.class);
            // startActivityForResult(addIntent, REQUEST_CODE_ADD_CONTACT);
        });

        fetchCustomCalls();

        // ⭐️ UPDATED CALL ⭐️
        setupBottomNavigation("home"); // This is part of the "home" flow
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_CONTACT && resultCode == RESULT_OK) {
            fetchCustomCalls();
        }
    }

    // --- ⭐️ OLD METHOD (setupBottomNavigation) IS DELETED ---

    // --- ⭐️ NEW DYNAMIC NAVIGATION METHOD ⭐️ ---

    /**
     * Sets up the bottom navigation bar based on the user's role.
     * @param activePage A string ("home", "history", "admin", "profile") to highlight the current page.
     */
    private void setupBottomNavigation(String activePage) {
        View bottomNavView = findViewById(R.id.bottom_navigation);
        if (bottomNavView == null) {
            Log.e(TAG, "FATAL: bottom_navigation view not found.");
            return;
        }

        // 1. Get references to all 4 nav buttons
        View navHome = bottomNavView.findViewById(R.id.nav_home);
        View navHistory = bottomNavView.findViewById(R.id.nav_history);
        View navGuideAdmin = bottomNavView.findViewById(R.id.nav_guide_admin); // The dynamic button
        View navProfile = bottomNavView.findViewById(R.id.nav_profile);

        // Get the inner parts of the dynamic button
        ImageView navGuideAdminIcon = bottomNavView.findViewById(R.id.nav_guide_admin_icon);
        TextView navGuideAdminText = bottomNavView.findViewById(R.id.nav_guide_admin_text);

        // 2. Check the role from SessionManager
        if (sessionManager.isAdmin()) {
            // --- ADMIN ---
            navGuideAdminText.setText("ADMIN");
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin); // (Requires ic_admin.png)

            navGuideAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                startActivity(intent);
                finish(); // Finish current activity
            });

            if ("admin".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107); // Semi-transparent yellow
            }

        } else {
            // --- USER ---
            navGuideAdminText.setText("GUIDE");
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help); // Use built-in icon

            navGuideAdmin.setOnClickListener(v -> {
                // Intent intent = new Intent(this, GuideActivity.class);
                // startActivity(intent);
                Toast.makeText(this, "Guide page coming soon", Toast.LENGTH_SHORT).show();
            });

            if ("guide".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107);
            }
        }

        // 3. Set click listeners for the other 3 buttons
        navHome.setOnClickListener(v -> {
            if (!"home".equals(activePage)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        navHistory.setOnClickListener(v -> {
            if (!"history".equals(activePage)) {
                Intent intent = new Intent(this, ChatHistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!"profile".equals(activePage)) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 4. Set highlight for the active page
        if ("home".equals(activePage)) {
            navHome.setBackgroundColor(0x55FFC107);
        } else if ("history".equals(activePage)) {
            navHistory.setBackgroundColor(0x55FFC107);
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107);
        }
    }


    // --- (Rest of your methods are unchanged) ---

    private void startLocationTypeFlow(String jenis, String customName, String customNumber) {
        Intent i = new Intent(EmergencyActivity.this, LocationTypeActivity.class);
        i.putExtra("jenis", jenis);
        if (customName != null) i.putExtra("custom_name", customName);
        if (customNumber != null) i.putExtra("custom_number", customNumber);
        startActivity(i);
    }

    private void fetchCustomCalls() {
        if (!sessionManager.isLoggedIn()) {
            Log.w(TAG, "Not logged in, cannot fetch custom calls.");
            return;
        }

        String token = sessionManager.getToken();
        EmergencyApiService apiService = ApiClient.getClient().create(EmergencyApiService.class);
        Call<CustomCallResponseModel> call = apiService.getCustomCalls(token);

        call.enqueue(new Callback<CustomCallResponseModel>() {
            @Override
            public void onResponse(Call<CustomCallResponseModel> call, Response<CustomCallResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    displayCustomButtons(response.body().getData());
                } else {
                    String error = "Failed to load custom calls";
                    if (response.body() != null) error = response.body().getError();
                    Log.e(TAG, "API Error: " + error);
                    Toast.makeText(EmergencyActivity.this, error, Toast.LENGTH_SHORT).show();
                    displayCustomButtons(null);
                }
            }

            @Override
            public void onFailure(Call<CustomCallResponseModel> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                Toast.makeText(EmergencyActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                displayCustomButtons(null);
            }
        });
    }

    private void displayCustomButtons(List<CustomCallModel> calls) {
        containerCustomButtons.removeAllViews();

        int contactCount = (calls != null) ? calls.size() : 0;

        if (contactCount >= MAX_CUSTOM_CONTACTS) {
            btnAddCustomCall.setVisibility(View.GONE);
        } else {
            btnAddCustomCall.setVisibility(View.VISIBLE);
        }

        if (calls == null || calls.isEmpty()) {
            return;
        }

        for (CustomCallModel call : calls) {
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(call.getCustom_name() + " — " + call.getCustom_number());
            b.setPadding(16, 16, 16, 16);
            // You can add more styling here

            b.setOnClickListener(v -> {
                startLocationTypeFlow("Custom", call.getCustom_name(), call.getCustom_number());
            });
            containerCustomButtons.addView(b);
        }
    }
}