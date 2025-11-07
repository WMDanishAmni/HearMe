// file: UserData.java
package com.example.hearme.models;

import com.google.gson.annotations.SerializedName;

// This model holds the user's data returned from the API
public class UserData {

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("username")
    private String username;

    @SerializedName("full_name")
    private String full_name;

    @SerializedName("email")
    private String email;

    @SerializedName("address")
    private String address;

    // --- Getters ---
    public int getUserId() { return user_id; }
    public String getUsername() { return username; }
    public String getFullName() { return full_name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
}