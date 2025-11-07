package com.example.hearme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log; // ⭐️ IMPORT
import android.view.View; // ⭐️ IMPORT
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hearme.R;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ApiInterface;
import com.example.hearme.models.BasicResponse;
import com.example.hearme.models.ForgotPasswordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText etEmail, etOtp;
    private Button btnVerifyOtp, btnResendOtp;
    private ProgressBar progressResend;
    private TextView tvCountdown, tvBackToForgot;
    private ApiInterface api;

    private CountDownTimer countDownTimer;
    private static final int RESEND_INTERVAL = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        etEmail = findViewById(R.id.etEmail);
        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        progressResend = findViewById(R.id.progressResend);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvBackToForgot = findViewById(R.id.tvBackToForgot);

        api = ApiClient.getClient().create(ApiInterface.class);

        String email = getIntent().getStringExtra("email");
        if (email != null) etEmail.setText(email);

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyOtp(email, otp);
            }
        });

        btnResendOtp.setOnClickListener(v -> resendOtp(email));

        tvBackToForgot.setOnClickListener(v -> {
            Intent intent = new Intent(VerifyOtpActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        });

        startCountdown();
        startAnimations();
    }

    private void startAnimations() {
        // ⭐️ ADDED A TRY-CATCH in case you don't add the anim files
        try {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_color);
            progressResend.startAnimation(pulse);
            progressResend.startAnimation(fade);
        } catch (Exception e) {
            Log.w("VerifyOtpActivity", "Animation files not found. Skipping animations.");
        }
    }

    private void startCountdown() {
        btnResendOtp.setEnabled(false);
        progressResend.setVisibility(ProgressBar.VISIBLE);
        progressResend.setProgress(0);
        tvCountdown.setText((RESEND_INTERVAL / 1000) + "s");

        countDownTimer = new CountDownTimer(RESEND_INTERVAL, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                int progress = (int) (((RESEND_INTERVAL - millisUntilFinished) * 100) / RESEND_INTERVAL);
                progressResend.setProgress(progress);
                tvCountdown.setText(secondsLeft + "s");
            }

            @Override
            public void onFinish() {
                btnResendOtp.setEnabled(true);
                tvCountdown.setText("Ready to resend");
                if (progressResend != null) {
                    progressResend.clearAnimation();
                }
                progressResend.setVisibility(ProgressBar.GONE);
            }
        }.start();
    }

    private void resendOtp(String email) {
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();

        api.forgotPassword(email).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForgotPasswordResponse res = response.body();
                    Toast.makeText(VerifyOtpActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();

                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        if (res.getOtp() != null) {
                            Toast.makeText(VerifyOtpActivity.this,
                                    "New OTP: " + res.getOtp(), Toast.LENGTH_LONG).show();
                        }
                        startCountdown();
                        startAnimations();
                    }
                } else {
                    Toast.makeText(VerifyOtpActivity.this,
                            "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                Toast.makeText(VerifyOtpActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyOtp(String email, String otp) {
        Toast.makeText(this, "Verifying OTP...", Toast.LENGTH_SHORT).show();

        api.verifyOtp(email, otp).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse res = response.body();
                    Toast.makeText(VerifyOtpActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();

                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(VerifyOtpActivity.this,
                            "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(VerifyOtpActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (progressResend != null) progressResend.clearAnimation();
    }
}