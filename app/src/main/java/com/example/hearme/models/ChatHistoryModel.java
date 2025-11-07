// file: ChatHistoryModel.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

public class ChatHistoryModel {

    @SerializedName("chat_id")
    private int chat_id;

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("hear")
    private String hear;

    @SerializedName("speak")
    private String speak;

    @SerializedName("timestamp")
    private String timestamp;

    // --- Getters (and setters if you need them) ---

    public int getChat_id() {
        return chat_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getHear() {
        return hear;
    }

    public String getSpeak() {
        return speak;
    }

    public String getTimestamp() {
        return timestamp;
    }
}