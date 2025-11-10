// file: SignupActivity.java
package com.example.hearme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hearme.R;
import com.example.hearme.api.*;
import java.util.Map;
import retrofit2.*;

public class SignupActivity extends BaseActivity {

    EditText etUsername, etPassword, etEmail, etFullName, etAddress, etPhone;
    Button btnSignup;
    TextView tvLoginRedirect;
    ApiInterface api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);

        api = ApiClient.getClient().create(ApiInterface.class);

        btnSignup.setOnClickListener(v -> {

            // ⭐️ Added simple validation ⭐️
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Please fill in Username, Email, Password, and Full Name", Toast.LENGTH_LONG).show();
                return;
            }
            // ⭐️ End validation ⭐️

            api.signup(
                    username,
                    password,
                    email,
                    fullName,
                    address,
                    phone
            ).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if ("success".equals(response.body().get("status"))) {
                            Toast.makeText(SignupActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                            // ⭐️ FIXED INTENT: Points to the correct package ⭐️
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish(); // Finish signup activity
                        } else {
                            Toast.makeText(SignupActivity.this, response.body().get("message"), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(SignupActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvLoginRedirect.setOnClickListener(v ->
                // ⭐️ FIXED INTENT: Points to the correct package ⭐️
                startActivity(new Intent(SignupActivity.this, LoginActivity.class))
        );
    }
}