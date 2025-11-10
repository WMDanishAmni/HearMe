// file: app/src/main/java/com/example/hearme/helpers/SettingsManager.java
package com.example.hearme.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {

    private final SharedPreferences prefs;

    // Preference Keys
    private static final String PREF_NAME = "HearMeUserSettings";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_TEXT_SIZE = "text_size";

    // Text Size Values
    public static final String TEXT_SIZE_SMALL = "Small";
    public static final String TEXT_SIZE_MEDIUM = "Medium";
    public static final String TEXT_SIZE_LARGE = "Large";

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- Theme (Dark Mode) ---
    public void saveTheme(int mode) {
        prefs.edit().putInt(KEY_THEME, mode).apply();
    }

    public int getTheme() {
        // Default to Light Mode (MODE_NIGHT_NO)
        return prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO);
    }

    // --- Language ---
    public void saveLanguage(String language) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public String getLanguage() {
        // Default to English ("en")
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    // --- Text Size ---
    public void saveTextSize(String size) {
        prefs.edit().putString(KEY_TEXT_SIZE, size).apply();
    }

    public String getTextSize() {
        // Default to Medium
        return prefs.getString(KEY_TEXT_SIZE, TEXT_SIZE_MEDIUM);
    }
}