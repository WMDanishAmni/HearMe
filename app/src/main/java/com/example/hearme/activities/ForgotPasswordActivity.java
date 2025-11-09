// file: ForgotPasswordActivity.java
package com.example.hearme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hearme.R;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ApiInterface;
import com.example.hearme.models.ForgotPasswordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;
    private TextView tvBackToLogin;
    private ApiInterface api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        api = ApiClient.getClient().create(ApiInterface.class);

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your registered email", Toast.LENGTH_SHORT).show();
            } else {
                btnSendOtp.setEnabled(false);
                sendOtp(email);
            }
        });

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void sendOtp(String email) {
        Toast.makeText(this, "Sending OTP...", Toast.LENGTH_SHORT).show();

        api.forgotPassword(email).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                btnSendOtp.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ForgotPasswordResponse res = response.body();

                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "OTP sent to your email.",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                res.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Server error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                btnSendOtp.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}