// file: app/src/main/java/com/example/hearme/activities/profile/ProfileActivity.java
package com.example.hearme.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.hearme.R;
import com.example.hearme.activities.BaseActivity; // ⭐️ CHANGED
import com.example.hearme.activities.LoginActivity;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity;
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.guide.GuideActivity;
import com.example.hearme.helpers.SettingsManager; // ⭐️ NEW
import com.example.hearme.models.SessionManager;

import java.util.Locale;

public class ProfileActivity extends BaseActivity { // ⭐️ CHANGED from AppCompatActivity

    private static final String TAG = "ProfileActivity";
    private SessionManager sessionManager;
    private SettingsManager settingsManager; // ⭐️ NEW

    // Views
    private TextView tvUsername;
    private LinearLayout llEditProfile;
    private Button btnLogout;
    private SwitchCompat switchThemeMode; // ⭐️ NEW
    private LinearLayout llLanguage; // ⭐️ NEW
    private TextView tvLanguageValue; // ⭐️ NEW
    private LinearLayout llTextSize; // ⭐️ NEW
    private TextView tvTextSizeValue; // ⭐️ NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        settingsManager = new SettingsManager(this); // ⭐️ NEW

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Initialize all views
        initializeViews();

        // Set click listeners
        setupListeners();

        // Setup bottom navigation
        setupBottomNavigation("profile");
    }

    private void initializeViews() {
        tvUsername = findViewById(R.id.tv_username);
        llEditProfile = findViewById(R.id.ll_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);

        // New Settings Views
        switchThemeMode = findViewById(R.id.switch_theme_mode);
        llLanguage = findViewById(R.id.ll_language);
        tvLanguageValue = findViewById(R.id.tv_language_value);
        llTextSize = findViewById(R.id.ll_text_size);
        tvTextSizeValue = findViewById(R.id.tv_text_size_value);
    }

    private void setupListeners() {
        // Edit Profile
        llEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            redirectToLogin();
        });

        // --- NEW LISTENERS ---

        // Theme (Dark Mode) Switch
        switchThemeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newTheme = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            if (settingsManager.getTheme() != newTheme) {
                settingsManager.saveTheme(newTheme);
                recreateActivity(); // Recreate the activity to apply theme
            }
        });

        // Language Button
        llLanguage.setOnClickListener(v -> showLanguageDialog());

        // Text Size Button
        llTextSize.setOnClickListener(v -> showTextSizeDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load data every time the activity is shown
        loadUserData();
        updateSettingsUI(); // ⭐️ NEW: Update UI with saved settings
    }

    private void loadUserData() {
        // This is correct, it works for both admin and user
        tvUsername.setText(sessionManager.getUsername());
    }

    private void updateSettingsUI() {
        // Update Theme Switch
        int currentTheme = settingsManager.getTheme();
        switchThemeMode.setChecked(currentTheme == AppCompatDelegate.MODE_NIGHT_YES);

        // Update Language Text
        String currentLang = settingsManager.getLanguage();
        if ("ms".equals(currentLang)) {
            tvLanguageValue.setText("Melayu");
        } else {
            tvLanguageValue.setText("English");
        }

        // Update Text Size Text
        String currentTextSize = settingsManager.getTextSize();
        String sizeString;
        if (SettingsManager.TEXT_SIZE_SMALL.equals(currentTextSize)) {
            sizeString = getString(R.string.text_size_small);
        } else if (SettingsManager.TEXT_SIZE_LARGE.equals(currentTextSize)) {
            sizeString = getString(R.string.text_size_large);
        } else {
            sizeString = getString(R.string.text_size_medium);
        }
        tvTextSizeValue.setText(sizeString);
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Melayu"};
        final String[] langCodes = {"en", "ms"};

        String currentLangCode = settingsManager.getLanguage();
        int checkedItem = "ms".equals(currentLangCode) ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_select_language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLangCode = langCodes[which];
                    if (!settingsManager.getLanguage().equals(selectedLangCode)) {
                        settingsManager.saveLanguage(selectedLangCode);
                        dialog.dismiss();
                        recreateActivity(); // Recreate to apply language
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTextSizeDialog() {
        final String[] sizes = {
                getString(R.string.text_size_small),
                getString(R.string.text_size_medium),
                getString(R.string.text_size_large)
        };
        final String[] sizeKeys = {
                SettingsManager.TEXT_SIZE_SMALL,
                SettingsManager.TEXT_SIZE_MEDIUM,
                SettingsManager.TEXT_SIZE_LARGE
        };

        String currentSizeKey = settingsManager.getTextSize();
        int checkedItem = 0;
        if (SettingsManager.TEXT_SIZE_MEDIUM.equals(currentSizeKey)) {
            checkedItem = 1;
        } else if (SettingsManager.TEXT_SIZE_LARGE.equals(currentSizeKey)) {
            checkedItem = 2;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_select_text_size)
                .setSingleChoiceItems(sizes, checkedItem, (dialog, which) -> {
                    String selectedSizeKey = sizeKeys[which];
                    if (!settingsManager.getTextSize().equals(selectedSizeKey)) {
                        settingsManager.saveTextSize(selectedSizeKey);
                        dialog.dismiss();
                        recreateActivity(); // Recreate to apply text size
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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
        View navGuideAdmin = bottomNavView.findViewById(R.id.nav_guide_admin);
        View navProfile = bottomNavView.findViewById(R.id.nav_profile);

        // Get the inner parts of the dynamic button
        ImageView navGuideAdminIcon = bottomNavView.findViewById(R.id.nav_guide_admin_icon);
        TextView navGuideAdminText = bottomNavView.findViewById(R.id.nav_guide_admin_text);

        // 2. Check the role from SessionManager
        if (sessionManager.isAdmin()) {
            // --- ADMIN ---
            navGuideAdminText.setText(R.string.nav_admin); // ⭐️ Use string res
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin);

            navGuideAdmin.setOnClickListener(v -> {
                if (!"admin".equals(activePage)) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            // --- USER ---
            navGuideAdminText.setText(getString(R.string.nav_guide));
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help);

            navGuideAdmin.setOnClickListener(v -> {
                if (!"guide".equals(activePage)) {
                    Intent intent = new Intent(this, GuideActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                }
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
        } else if ("admin".equals(activePage) && sessionManager.isAdmin()) {
            navGuideAdmin.setBackgroundColor(0x55FFC107);
        }
    }
}