// file: CustomPhraseResponseModel.java
package com.example.hearme.models;

public class CustomPhraseResponseModel {
    private boolean success;
    private String message;

    // Default constructor (required by Gson)
    public CustomPhraseResponseModel() {}

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}