// file: CustomCallModel.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

public class CustomCallModel {

    @SerializedName("custom_id")
    private int custom_id;

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("custom_name")
    private String custom_name;

    @SerializedName("custom_number")
    private String custom_number;

    // --- Getters ---

    public int getCustom_id() {
        return custom_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getCustom_name() {
        return custom_name;
    }

    public String getCustom_number() {
        return custom_number;
    }
}