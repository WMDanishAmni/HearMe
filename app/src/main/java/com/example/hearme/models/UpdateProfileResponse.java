// file: UpdateProfileResponse.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    @SerializedName("data")
    private UserData data;

    // --- Getters ---
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getError() { return (error != null) ? error : message; } // Helper
    public UserData getData() { return data; }
}