package com.example.hearme.models;

import java.util.List;

public class CustomPhraseListResponseModel {
    private boolean success;
    private String message;
    private List<CustomPhraseModel> data; // server returns data array

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<CustomPhraseModel> getPhrases() { return data; }
}
