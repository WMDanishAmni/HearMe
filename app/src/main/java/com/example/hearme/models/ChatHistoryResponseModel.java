// file: ChatHistoryResponseModel.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatHistoryResponseModel {

    @SerializedName("success")
    private boolean success;

    // This 'data' list holds all the individual chat items
    @SerializedName("data")
    private List<ChatHistoryModel> data;

    // This 'error' field matches the JSON from get_chat.php
    @SerializedName("error")
    private String error;

    // --- Getters ---

    public boolean isSuccess() {
        return success;
    }

    public List<ChatHistoryModel> getData() {
        return data;
    }

    // This is the method that was missing
    public String getError() {
        return error;
    }
}