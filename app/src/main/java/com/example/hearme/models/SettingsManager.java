package com.example.hearme.models;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private static final String PREF_NAME = "HearMeSettings";

    // Keys for our settings
    public static final String KEY_THEME = "theme_mode";
    public static final String KEY_TEXT_SIZE = "text_size";
    public static final String KEY_LANGUAGE = "language";

    // Theme constants
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    // Text Size constants
    public static final String TEXT_SMALL = "small";
    public static final String TEXT_MEDIUM = "medium";
    public static final String TEXT_LARGE = "large";

    // Language constants
    public static final String LANG_ENGLISH = "en";
    public static final String LANG_MALAY = "ms";

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // --- THEME ---
    public void saveTheme(String theme) {
        editor.putString(KEY_THEME, theme);
        editor.apply();

        // Apply the theme to the running app
        if (theme.equals(THEME_DARK)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, THEME_LIGHT); // Default to Light
    }

    // --- TEXT SIZE ---
    public void saveTextSize(String size) {
        editor.putString(KEY_TEXT_SIZE, size);
        editor.apply();
    }

    public String getTextSize() {
        return prefs.getString(KEY_TEXT_SIZE, TEXT_MEDIUM); // Default to Medium
    }

    // --- LANGUAGE ---
    public void saveLanguage(String language) {
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, LANG_MALAY); // Default to Malay
    }
}