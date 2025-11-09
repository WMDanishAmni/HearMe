// file: SessionManager.java
package com.example.hearme.models;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "HearMeSession";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Context _context;

    // --- SHARED KEYS ---
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role"; // "user" or "admin"
    private static final String KEY_USERNAME = "username";

    // --- USER-ONLY KEYS ---
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_EMAIL = "email";

    // --- ADMIN-ONLY KEYS ---
    private static final String KEY_ADMIN_ID = "admin_id";

    public SessionManager(Context context) {
        this._context = context;
        prefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Creates a login session for a regular USER.
     */
    public void createLoginSession(String token, UserData user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_ROLE, "user");
        editor.putString(KEY_TOKEN, token);

        // User-specific data
        editor.putInt(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putString(KEY_EMAIL, user.getEmail());

        // Clear any old admin data
        editor.remove(KEY_ADMIN_ID);

        editor.apply();
    }

    /**
     * Creates a login session for an ADMIN.
     * Admin data contains admin_id, user_data contains user_id
     */
    public void createAdminSession(String token, AdminData adminData) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_ROLE, "admin");
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, adminData.getUsername());
        editor.putInt(KEY_ADMIN_ID, adminData.getAdminId());

        // Get the embedded user data and save it
        UserData userData = adminData.getUserData();
        if (userData != null) {
            editor.putInt(KEY_USER_ID, userData.getUserId());
            editor.putString(KEY_FULL_NAME, userData.getFullName());
            editor.putString(KEY_ADDRESS, userData.getAddress());
            editor.putString(KEY_EMAIL, userData.getEmail());
        }

        editor.apply();
    }

    /**
     * ⭐️ THIS IS THE NEW METHOD ⭐️
     * Updates user details after an edit, using the UserData object.
     */
    public void updateUserDetails(UserData updatedUser) {
        // Only update if it's a user or an admin with user data
        if (isLoggedIn() && updatedUser != null) {
            editor.putString(KEY_USERNAME, updatedUser.getUsername());
            editor.putString(KEY_FULL_NAME, updatedUser.getFullName());
            editor.putString(KEY_ADDRESS, updatedUser.getAddress());
            editor.putString(KEY_EMAIL, updatedUser.getEmail());
            editor.apply();
        }
    }

    /**
     * Clears all session data on logout.
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // --- GETTERS ---

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isAdmin() {
        return "admin".equals(prefs.getString(KEY_ROLE, null));
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "user"); // Default to user
    }

    /**
     * Gets the session token (for User or Admin)
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Guest");
    }

    // --- User-Specific Getters (Admins also have these) ---
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "User");
    }

    public String getAddress() {
        return prefs.getString(KEY_ADDRESS, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // --- Admin-Specific Getters ---
    public int getAdminId() {
        return prefs.getInt(KEY_ADMIN_ID, 0);
    }
}