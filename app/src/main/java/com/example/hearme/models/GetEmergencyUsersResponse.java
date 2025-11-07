package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetEmergencyUsersResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("category")
    private String category;

    @SerializedName("users")
    private List<UserData> users;

    @SerializedName("message") // 👈 ADD THIS LINE
    private String message;     // 👈 ADD THIS LINE

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public List<UserData> getUsers() {
        return users;
    }

    public String getMessage() {
        return message;
    } // 👈 ADD THIS METHOD

    public static class UserData {
        @SerializedName("user_id")
        private int userId;

        @SerializedName("username")
        private String username;

        @SerializedName("full_name")
        private String fullName;

        @SerializedName("phone_no")
        private String phoneNo;

        @SerializedName("location")
        private String location;

        @SerializedName("timestamp")
        private String timestamp;

        public int getUserId() { return userId; }
        public String getUsername() { return username != null ? username : "-"; }
        public String getFullName() { return fullName != null && !fullName.isEmpty() ? fullName : "Unknown User"; }
        public String getPhoneNo() { return phoneNo != null && !phoneNo.isEmpty() ? phoneNo : "N/A"; }
        public String getLocation() { return location != null ? location : "No location"; }
        public String getTimestamp() { return timestamp != null ? timestamp : ""; }
    }
}
