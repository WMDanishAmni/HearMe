// file: ForgotPasswordResponse.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

// This will parse the response from forgot_password_request.php
public class ForgotPasswordResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("otp")
    private String otp; // For testing, if your API sends it back

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getOtp() { return otp; }
}