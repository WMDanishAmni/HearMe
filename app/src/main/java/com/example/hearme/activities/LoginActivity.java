// file: LoginActivity.java
package com.example.hearme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ApiInterface;
import com.example.hearme.models.AdminData;
import com.example.hearme.models.SessionManager;
import com.example.hearme.models.UnifiedLoginResponse;
import com.example.hearme.models.UserData;
import com.google.gson.Gson;

import com.example.hearme.activities.admin.AdminDashboardActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignupRedirect;
    private ApiInterface api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User is already logged in. Checking role...");
            Intent intent;
            if (sessionManager.isAdmin()) {
                Log.d(TAG, "Role: Admin. Redirecting to AdminDashboardActivity.");
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
            } else {
                Log.d(TAG, "Role: User. Redirecting to MainActivity.");
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "User not logged in. Showing login screen.");
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignupRedirect = findViewById(R.id.tvSignupRedirect);

        api = ApiClient.getClient().create(ApiInterface.class);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        tvSignupRedirect.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void loginUser(String username, String password) {
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

        api.unifiedLogin(username, password).enqueue(new Callback<UnifiedLoginResponse>() {
            @Override
            public void onResponse(Call<UnifiedLoginResponse> call, Response<UnifiedLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UnifiedLoginResponse res = response.body();

                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        String role = res.getRole();
                        String token = res.getToken();
                        Gson gson = new Gson();

                        // ... inside onResponse ...
                        if ("admin".equals(role)) {
                            // --- ADMIN ---
                            Log.d(TAG, "Admin login successful.");
                            // Parse the admin data (which now includes user_data)
                            AdminData adminData = gson.fromJson(res.getData(), AdminData.class);

                            // ⭐️ Create admin session with the full AdminData object
                            sessionManager.createAdminSession(token, adminData);

                            Toast.makeText(LoginActivity.this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();

                        } else if ("user".equals(role)) {
                            Log.d(TAG, "User login successful.");
                            UserData userData = gson.fromJson(res.getData(), UserData.class);
                            sessionManager.createLoginSession(token, userData);

                            Toast.makeText(LoginActivity.this, "Login successful! Welcome " + userData.getUsername(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Log.w(TAG, "Login successful but role is unknown: " + role);
                            Toast.makeText(LoginActivity.this, "Unknown user role.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.w(TAG, "Login failed: " + res.getMessage());
                        Toast.makeText(LoginActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Server error: " + response.code());
                    Toast.makeText(LoginActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UnifiedLoginResponse> call, Throwable t) {
                Log.e(TAG, "Connection error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}