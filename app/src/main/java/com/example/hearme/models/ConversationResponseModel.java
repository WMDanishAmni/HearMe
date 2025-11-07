// file: ConversationResponseModel.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

public class ConversationResponseModel {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    // --- Getters ---

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}