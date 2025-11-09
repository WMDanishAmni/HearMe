// file: ChatDetailActivity.java
package com.example.hearme.activities.history;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView; // ⭐️ IMPORT
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity; // ⭐️ IMPORT
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ConversationApiService;
import com.example.hearme.models.ConversationResponseModel;
import com.example.hearme.models.SessionManager; // ⭐️ IMPORT

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChatDetailActivity";
    private TextView tvHearContent;
    private TextView tvSpeakContent;
    private int chatId;
    private String hearText;
    private String speakText;

    private SessionManager sessionManager; // This was already here, which is great

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        sessionManager = new SessionManager(this); // Already here

        // Initialize views
        tvHearContent = findViewById(R.id.tv_hear_content);
        tvSpeakContent = findViewById(R.id.tv_speak_content);

        // Get data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatId = extras.getInt("chat_id");
            hearText = extras.getString("hear");
            speakText = extras.getString("speak");

            if (hearText != null) {
                tvHearContent.setText(hearText);
            }
            if (speakText != null) {
                tvSpeakContent.setText(speakText);
            }
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_delete).setOnClickListener(v -> confirmDelete());

        // ⭐️ UPDATED CALL ⭐️
        setupBottomNavigation("history"); // Highlight the "history" tab
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteConversation();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteConversation() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ConversationApiService apiService = ApiClient.getClient().create(ConversationApiService.class);
        Call<ConversationResponseModel> call = apiService.deleteConversation(chatId, token);

        call.enqueue(new Callback<ConversationResponseModel>() {
            @Override
            public void onResponse(Call<ConversationResponseModel> call, Response<ConversationResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChatDetailActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    String error = (response.body() != null) ? response.body().getMessage() : "Server error";
                    Toast.makeText(ChatDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConversationResponseModel> call, Throwable t) {
                Log.e(TAG, "Error deleting conversation", t);
                Toast.makeText(ChatDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        View navProfile = bottomNavView.findViewById(R.id.nav_profile); // Renamed from nav_settings

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
                // finish(); // Finish current activity
            });

            if ("guide".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107);
            }
        }

        // 3. Set click listeners for the other 3 buttons
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navHistory.setOnClickListener(v -> {
            // Already on this page, but finish() goes back to the list
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // 4. Set highlight for the active page
        if ("home".equals(activePage)) {
            navHome.setBackgroundColor(0x55FFC107);
        } else if ("history".equals(activePage)) {
            navHistory.setBackgroundColor(0x55FFC107); // Highlight History
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107);
        }
    }
}