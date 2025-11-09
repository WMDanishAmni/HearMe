// file: EmergencyHistoryModel.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

public class EmergencyHistoryModel {

    @SerializedName("record_id")
    private int record_id;

    @SerializedName("emergency_type")
    private String emergency_type;

    @SerializedName("location_text")
    private String location_text;

    @SerializedName("created_at")
    private String created_at;

    // --- Getters ---

    public int getRecord_id() {
        return record_id;
    }

    public String getEmergency_type() {
        return emergency_type;
    }

    public String getLocation_text() {
        return location_text;
    }

    public String getCreated_at() {
        return created_at;
    }
}