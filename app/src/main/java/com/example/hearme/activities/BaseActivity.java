package com.example.hearme.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.hearme.models.SettingsManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the saved theme BEFORE the layout is set
        applyTheme();
    }

    /**
     * Reads the saved theme from SettingsManager and applies it.
     */
    private void applyTheme() {
        SettingsManager settingsManager = new SettingsManager(this);
        String theme = settingsManager.getTheme();

        if (theme.equals(SettingsManager.THEME_DARK)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // We will add text size and language logic here later
}