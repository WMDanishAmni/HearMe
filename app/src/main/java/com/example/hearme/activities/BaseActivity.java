// file: app/src/main/java/com/example/hearme/activities/BaseActivity.java
package com.example.hearme.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.hearme.R;
import com.example.hearme.helpers.SettingsManager;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    private SettingsManager settingsManager;

    // ⭐️ --- ADD THESE 3 VARIABLES --- ⭐️
    private String appliedLanguage = "";
    private String appliedTextSize = "";
    private int appliedTheme = -1;
    // ⭐️ ------------------------------ ⭐️

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);

        // 1. Apply Theme (Dark Mode)
        appliedTheme = settingsManager.getTheme(); // We still save this for the onResume() check
        AppCompatDelegate.setDefaultNightMode(settingsManager.getTheme()); // <-- Pass the value directly

        // 2. Apply Text Size
        // ⭐️ --- UPDATE THESE 2 LINES --- ⭐️
        appliedTextSize = settingsManager.getTextSize();
        setAppTheme(appliedTextSize);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // 3. Apply Language
        settingsManager = new SettingsManager(newBase);
        // ⭐️ --- UPDATE THESE 2 LINES --- ⭐️
        appliedLanguage = settingsManager.getLanguage();
        super.attachBaseContext(updateBaseContextLocale(newBase, appliedLanguage));
    }

    // ⭐️ --- ADD THIS ENTIRE onResume() METHOD --- ⭐️
    @Override
    protected void onResume() {
        super.onResume();
        if (settingsManager == null) {
            settingsManager = new SettingsManager(this);
        }

        // Check if settings have changed since the Activity was last visible
        final String currentLanguage = settingsManager.getLanguage();
        final String currentTextSize = settingsManager.getTextSize();
        final int currentTheme = settingsManager.getTheme();

        // If anything doesn't match, recreate the Activity to apply the new settings
        if (!appliedLanguage.equals(currentLanguage) ||
                !appliedTextSize.equals(currentTextSize) ||
                appliedTheme != currentTheme) {

            recreateActivity();
        }
    }
    // ⭐️ --- END OF NEW METHOD --- ⭐️

    private Context updateBaseContextLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    private void setAppTheme(String textSize) {
        // Set the theme based on the saved text size
        int themeId;
        if (SettingsManager.TEXT_SIZE_SMALL.equals(textSize)) {
            themeId = R.style.AppTheme_Small;
        } else if (SettingsManager.TEXT_SIZE_LARGE.equals(textSize)) {
            themeId = R.style.AppTheme_Large;
        } else {
            themeId = R.style.AppTheme_Medium;
        }
        setTheme(themeId);
    }

    // Call this from ProfileActivity to restart it
    protected void recreateActivity() {
        recreate();
    }
}