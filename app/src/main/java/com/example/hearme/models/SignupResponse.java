// file: SignupResponse.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

// This model will parse {"success": true, "message": "..."} or {"success": false, "error": "..."}
public class SignupResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    // --- Getters ---

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        // Helper to return error OR message, whichever is not null
        return (error != null) ? error : message;
    }
}