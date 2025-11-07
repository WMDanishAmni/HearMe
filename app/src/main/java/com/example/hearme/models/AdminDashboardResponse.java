package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminDashboardResponse {
    private String status;

    @SerializedName("total_users")
    private int totalUsers;

    @SerializedName("emergency_stats")
    private List<EmergencyStat> emergencyStats;

    public String getStatus() { return status; }
    public int getTotalUsers() { return totalUsers; }
    public List<EmergencyStat> getEmergencyStats() { return emergencyStats; }

    public static class EmergencyStat {
        @SerializedName("category_name")
        private String categoryName;
        private int total;

        public String getCategoryName() { return categoryName; }
        public int getTotal() { return total; }
    }
}
