package com.example.hearme.models;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName; // ⭐️ ADD THIS IMPORT

public class UnifiedLoginResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("role")
    private String role; // "admin" or "user"

    @SerializedName("token") // ⭐️ ADD THIS LINE
    private String token;  // ⭐️ ADD THIS LINE

    @SerializedName("data")
    private JsonElement data;

    // --- Getters ---

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getRole() { return role; }
    public JsonElement getData() { return data; }

    // ⭐️ ADD THIS METHOD ⭐️
    public String getToken() {
        return token;
    }
}