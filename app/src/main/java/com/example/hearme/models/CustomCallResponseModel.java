// file: CustomCallResponseModel.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CustomCallResponseModel {

    @SerializedName("success")
    private boolean success;

    @SerializedName("error")
    private String error;

    @SerializedName("data")
    private List<CustomCallModel> data;

    // --- Getters ---

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public List<CustomCallModel> getData() {
        return data;
    }
}