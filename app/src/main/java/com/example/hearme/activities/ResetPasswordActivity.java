// file: ResetPasswordActivity.java
package com.example.hearme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hearme.R;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ApiInterface;
import com.example.hearme.models.BasicResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends BaseActivity {

    private EditText etEmail, etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvBackToLogin; // ⭐️ ADDED this line
    private ApiInterface api;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin); // ⭐️ ADDED this line

        api = ApiClient.getClient().create(ApiInterface.class);

        email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
            etEmail.setEnabled(false);
        }

        btnResetPassword.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in both password fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            resetPassword(email, newPass, confirmPass);
        });

        // ⭐️ ADDED this listener
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void resetPassword(String email, String newPassword, String confirmPassword) {
        Toast.makeText(this, "Resetting password...", Toast.LENGTH_SHORT).show();

        api.resetPassword(email, newPassword, confirmPassword).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse res = response.body();
                    Toast.makeText(ResetPasswordActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();

                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this,
                            "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}