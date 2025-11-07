package com.example.hearme.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // ⭐️ IMPORT
import android.view.View;
import android.widget.Button;
import android.widget.ImageView; // ⭐️ IMPORT
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.activities.LoginActivity;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity; // ⭐️ IMPORT
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.models.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity"; // ⭐️ ADDED
    private SessionManager sessionManager;
    private TextView tvUsername;
    private LinearLayout llEditProfile;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        tvUsername = findViewById(R.id.tv_username);
        llEditProfile = findViewById(R.id.ll_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);

        llEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            redirectToLogin();
        });

        // ⭐️ UPDATED CALL ⭐️
        setupBottomNavigation("profile"); // Highlight the "profile" tab
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This is correct, it reloads the username if it was changed
        loadUserData();
    }

    private void loadUserData() {
        // This is correct, it works for both admin and user
        tvUsername.setText(sessionManager.getUsername());
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- ⭐️ REPLACED THIS ENTIRE METHOD ⭐️ ---

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
            // Already on this page
        });

        // 4. Set highlight for the active page
        if ("home".equals(activePage)) {
            navHome.setBackgroundColor(0x55FFC107);
        } else if ("history".equals(activePage)) {
            navHistory.setBackgroundColor(0x55FFC107);
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107); // Highlight Profile
        }
    }
}