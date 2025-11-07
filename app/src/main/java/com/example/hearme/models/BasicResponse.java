// file: BasicResponse.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

// This will parse responses from verify_otp.php and reset_password.php
public class BasicResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
}