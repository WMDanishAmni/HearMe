package com.example.hearme.activities.home.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hearme.R;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity;
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.EmergencyApiService;
import com.example.hearme.models.CustomCallModel;
import com.example.hearme.models.CustomCallResponseModel;
import com.example.hearme.models.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyActivity extends AppCompatActivity {

    private Button btnSegera;
    private LinearLayout containerCustomButtons;
    private Button btnAddCustomCall;
    private ImageView btnBack;

    private SessionManager sessionManager;
    private static final String TAG = "EmergencyActivity";

    private static final int MAX_CUSTOM_CONTACTS = 5;
    private static final int REQUEST_CODE_ADD_CONTACT = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        btnSegera = findViewById(R.id.btnSegera);
        containerCustomButtons = findViewById(R.id.containerCustomButtons);
        btnAddCustomCall = findViewById(R.id.btn_add_custom_call);

        ConstraintLayout accidentCell = findViewById(R.id.btn_accident_cell);
        ConstraintLayout theftCell = findViewById(R.id.btn_theft_cell);
        ConstraintLayout healthCell = findViewById(R.id.btn_health_cell);
        ConstraintLayout fireCell = findViewById(R.id.btn_fire_cell);
        ConstraintLayout wildlifeCell = findViewById(R.id.btn_wildlife_cell);
        ConstraintLayout injuryCell = findViewById(R.id.btn_injury_cell);

        btnSegera.setOnClickListener(v -> startLocationTypeFlow("Segera", null, null));
        accidentCell.setOnClickListener(v -> startLocationTypeFlow("Kemalangan", null, null));
        theftCell.setOnClickListener(v -> startLocationTypeFlow("Pencurian", null, null));
        healthCell.setOnClickListener(v -> startLocationTypeFlow("Kesihatan", null, null));
        fireCell.setOnClickListener(v -> startLocationTypeFlow("Kebakaran", null, null));
        wildlifeCell.setOnClickListener(v -> startLocationTypeFlow("Serangan Haiwan", null, null));
        injuryCell.setOnClickListener(v -> startLocationTypeFlow("Cedera", null, null));

        btnAddCustomCall.setOnClickListener(v -> {
            Intent addIntent = new Intent(EmergencyActivity.this, AddCustomCallActivity.class);
            startActivityForResult(addIntent, REQUEST_CODE_ADD_CONTACT);
        });

        // ⭐️ REMOVED: fetchCustomCalls();
        // We will call this in onResume() instead

        setupBottomNavigation("home");
    }

    // ⭐️ --- FIX #3: ADD onResume() --- ⭐️
    @Override
    protected void onResume() {
        super.onResume();
        // This will now run EVERY TIME you return to this page
        fetchCustomCalls();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // This is still needed in case the user is already on the page
        if (requestCode == REQUEST_CODE_ADD_CONTACT && resultCode == RESULT_OK) {
            fetchCustomCalls();
        }
    }

    private void setupBottomNavigation(String activePage) {
        View bottomNavView = findViewById(R.id.bottom_navigation);
        if (bottomNavView == null) {
            Log.e(TAG, "FATAL: bottom_navigation view not found.");
            return;
        }

        View navHome = bottomNavView.findViewById(R.id.nav_home);
        View navHistory = bottomNavView.findViewById(R.id.nav_history);
        View navGuideAdmin = bottomNavView.findViewById(R.id.nav_guide_admin);
        View navProfile = bottomNavView.findViewById(R.id.nav_profile);

        if (navHome == null || navHistory == null || navGuideAdmin == null || navProfile == null) {
            Log.e(TAG, "FATAL: A navigation button was not found.");
            return;
        }

        ImageView navGuideAdminIcon = bottomNavView.findViewById(R.id.nav_guide_admin_icon);
        TextView navGuideAdminText = bottomNavView.findViewById(R.id.nav_guide_admin_text);

        if (sessionManager.isAdmin()) {
            navGuideAdminText.setText("ADMIN");
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin);

            navGuideAdmin.setOnClickListener(v -> {
                if (!"admin".equals(activePage)) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            navGuideAdminText.setText("GUIDE");
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help);

            navGuideAdmin.setOnClickListener(v -> {
                if (!"guide".equals(activePage)) {
                    Toast.makeText(this, "Guide page coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }

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

        if ("home".equals(activePage)) {
            navHome.setBackgroundColor(0x55FFC107);
        } else if ("history".equals(activePage)) {
            navHistory.setBackgroundColor(0x55FFC107);
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107);
        } else if ("admin".equals(activePage) && sessionManager.isAdmin()) {
            navGuideAdmin.setBackgroundColor(0x55FFC107);
        } else if ("guide".equals(activePage) && !sessionManager.isAdmin()) {
            navGuideAdmin.setBackgroundColor(0x55FFC107);
        }
    }


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
            b.setOnClickListener(v -> {
                startLocationTypeFlow("Custom", call.getCustom_name(), call.getCustom_number());
            });
            containerCustomButtons.addView(b);
        }
    }
}