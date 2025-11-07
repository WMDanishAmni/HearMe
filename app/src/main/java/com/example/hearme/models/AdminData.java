package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

public class AdminData {

    @SerializedName("admin_id")
    private int admin_id;

    @SerializedName("admin_username")
    private String admin_username;

    // ⭐️ ADD THIS ⭐️
    @SerializedName("user_data")
    private UserData user_data;

    // --- Getters ---
    public int getAdminId() {
        return admin_id;
    }

    public String getUsername() {
        return admin_username;
    }

    // ⭐️ ADD THIS ⭐️
    public UserData getUserData() {
        return user_data;
    }
}