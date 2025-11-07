package com.example.hearme.activities.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.models.SessionManager;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ProfileApiService;
import com.example.hearme.models.UpdateProfileResponse;
import com.example.hearme.models.UserData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername, etFullName, etEmail, etAddress;
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnSave;
    private ImageView btnBack;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        // Find Views
        btnBack = findViewById(R.id.btn_back_edit);
        etUsername = findViewById(R.id.et_edit_username);
        etFullName = findViewById(R.id.et_edit_fullname);
        etEmail = findViewById(R.id.et_edit_email);
        etAddress = findViewById(R.id.et_edit_address);
        btnSave = findViewById(R.id.btn_save_profile);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // Set Back Button
        btnBack.setOnClickListener(v -> finish());

        // Load existing data into fields
        loadUserData();

        // --- UPDATED Save Button Logic ---
        btnSave.setOnClickListener(v -> {
            // Get all data from fields
            String username = etUsername.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            // --- Client-side Validation ---
            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill in all user details (except password)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if user is changing password
            if (!newPass.isEmpty()) {
                if (oldPass.isEmpty()) {
                    Toast.makeText(this, "Please enter your old password to set a new one", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // All checks passed, proceed with API call
            saveProfileChanges(username, fullName, email, address, oldPass, newPass, confirmPass);
        });
    }

    private void loadUserData() {
        etUsername.setText(sessionManager.getUsername());
        etFullName.setText(sessionManager.getFullName());
        etAddress.setText(sessionManager.getAddress());
        etEmail.setText(sessionManager.getEmail());
    }

    // --- NEW API CALL METHOD ---
    private void saveProfileChanges(String username, String fullName, String email, String address, String oldPass, String newPass, String confirmPass) {
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Session expired, please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        ProfileApiService apiService = ApiClient.getClient().create(ProfileApiService.class);
        Call<UpdateProfileResponse> call = apiService.updateProfile(token, username, fullName, email, address, oldPass, newPass, confirmPass);

        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Success!
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    // Update the session with the new data from the server
                    UserData updatedUser = response.body().getData();
                    sessionManager.updateUserDetails(
                            updatedUser.getUsername(),
                            updatedUser.getFullName(),
                            updatedUser.getAddress(),
                            updatedUser.getEmail()
                    );

                    // Go back to the profile page
                    finish();

                } else {
                    // API returned an error (e.g., "Old password incorrect")
                    String error = "Failed to update profile.";
                    if (response.body() != null) {
                        error = response.body().getError();
                    }
                    Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                // Network error
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}