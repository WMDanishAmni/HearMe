package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Data data; // server returns data object { user_id, username, ... }

    // getters
    public boolean isSuccess() { return success; }
    public Data getData() { return data; }

    public static class Data {
        @SerializedName("user_id")
        private int userId;

        @SerializedName("username")
        private String username;

        @SerializedName("full_name")
        private String fullName;

        @SerializedName("email")
        private String email;

        @SerializedName("token")
        private String token;

        // getters
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getToken() { return token; }
    }
}
