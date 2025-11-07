package com.example.hearme.models;

import java.util.List;

public class AdminUserListResponse {
    private String status;
    private List<UserInfo> users;

    public String getStatus() {
        return status;
    }

    public List<UserInfo> getUsers() {
        return users;
    }

    // Inner class for user info
    public static class UserInfo {
        private String username;
        private String email;
        private String full_name;
        private String address;
        private String phone_no;
        private String created_at;

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getFull_name() {
            return full_name;
        }

        public String getAddress() {
            return address;
        }

        public String getPhone_no() {
            return phone_no;
        }

        public String getCreated_at() {
            return created_at;
        }
    }
}
