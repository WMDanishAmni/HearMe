package com.example.hearme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // ⭐️ IMPORT
import android.view.View;
import android.widget.ImageView; // ⭐️ IMPORT
import android.widget.TextView; // ⭐️ IMPORT
import android.widget.Toast; // ⭐️ IMPORT

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.example.hearme.R;
import com.example.hearme.activities.admin.AdminDashboardActivity; // ⭐️ IMPORT
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.home.SpeakAndHear.SpeakAndHearActivity;
import com.example.hearme.activities.home.audio.AudioTranscriptionActivity;
import com.example.hearme.activities.home.emergency.EmergencyActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.models.SessionManager; // ⭐️ IMPORT

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // ⭐️ ADDED
    private CardView cardHearFromNonDeaf;
    private CardView cardSpeakToNonDeaf;
    private CardView cardAudioTranscription;
    private CardView cardEmergency;

    private SessionManager sessionManager; // ⭐️ ADDED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ⭐️ ADDED
        sessionManager = new SessionManager(this);

        // ⭐️ ADDED: Redirect to Login if not logged in
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Stop running onCreate
        }

        initializeViews();
        setupClickListeners();

        // ⭐️ UPDATED CALL ⭐️
        setupBottomNavigation("home"); // Highlight the "home" tab
    }

    private void initializeViews() {
        cardHearFromNonDeaf = findViewById(R.id.card_hear_from_nondeaf);
        cardSpeakToNonDeaf = findViewById(R.id.card_speak_to_nondeaf);
        cardAudioTranscription = findViewById(R.id.card_audio_transcription);
        cardEmergency = findViewById(R.id.card_emergency);
    }

    private void setupClickListeners() {
        // This is correct. It launches SpeakAndHearActivity and tells it to open in "HEAR" mode.
        cardHearFromNonDeaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpeakAndHearActivity.class);
                intent.putExtra("INITIAL_MODE", "HEAR"); // Pass the initial mode
                startActivity(intent);
            }
        });

        // This is correct. It launches SpeakAndHearActivity and tells it to open in "SPEAK" mode.
        cardSpeakToNonDeaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpeakAndHearActivity.class);
                intent.putExtra("INITIAL_MODE", "SPEAK"); // Pass the initial mode
                startActivity(intent);
            }
        });

        // Audio transcription button click
        cardAudioTranscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AudioTranscriptionActivity.class);
                startActivity(intent);
            }
        });

        // Emergency button click
        cardEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EmergencyActivity.class);
                startActivity(intent);
            }
        });

        // ⭐️ REMOVED: setupBottomNavigation();
        // This is now called from onCreate()
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
            // Already on this page
        });

        navHistory.setOnClickListener(v -> {
            if (!"history".equals(activePage)) {
                Intent intent = new Intent(this, ChatHistoryActivity.class);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!"profile".equals(activePage)) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
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
}