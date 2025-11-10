// file: EditProfileActivity.java
package com.example.hearme.activities.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.activities.BaseActivity;
import com.example.hearme.models.SessionManager;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ProfileApiService;
import com.example.hearme.models.UpdateProfileResponse;
import com.example.hearme.models.UserData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends BaseActivity {

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

        btnSave.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) { // Address can be empty
                // ⭐️ USE STRING ⭐️
                Toast.makeText(this, getString(R.string.toast_edit_profile_fill_required), Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if password fields are valid (if user is trying to change it)
            if (!newPass.isEmpty() || !oldPass.isEmpty() || !confirmPass.isEmpty()) {
                if (oldPass.isEmpty()) {
                    // ⭐️ USE STRING ⭐️
                    Toast.makeText(this, getString(R.string.toast_edit_profile_old_pass_required), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPass.isEmpty()) {
                    // ⭐️ USE STRING ⭐️
                    Toast.makeText(this, getString(R.string.toast_edit_profile_new_pass_required), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    // ⭐️ USE STRING ⭐️
                    Toast.makeText(this, getString(R.string.toast_edit_profile_pass_mismatch), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            saveProfileChanges(username, fullName, email, address, oldPass, newPass, confirmPass);
        });
    }

    private void loadUserData() {
        etUsername.setText(sessionManager.getUsername());
        etFullName.setText(sessionManager.getFullName());
        etAddress.setText(sessionManager.getAddress());
        etEmail.setText(sessionManager.getEmail());
    }

    private void saveProfileChanges(String username, String fullName, String email, String address, String oldPass, String newPass, String confirmPass) {
        String token = sessionManager.getToken();
        if (token == null) {
            // ⭐️ USE STRING ⭐️
            Toast.makeText(this, getString(R.string.toast_edit_profile_session_expired), Toast.LENGTH_SHORT).show();
            return;
        }

        ProfileApiService apiService = ApiClient.getClient().create(ProfileApiService.class);
        Call<UpdateProfileResponse> call = apiService.updateProfile(token, username, fullName, email, address, oldPass, newPass, confirmPass);

        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateProfileResponse res = response.body();

                    if (res.isSuccess()) {
                        // Success!
                        // ⭐️ USE STRING ⭐️
                        Toast.makeText(EditProfileActivity.this, getString(R.string.toast_edit_profile_success), Toast.LENGTH_SHORT).show();

                        // This part is already correct from our previous fix
                        UserData updatedUser = res.getData();
                        sessionManager.updateUserDetails(updatedUser);

                        // Go back to the profile page
                        finish();

                    } else {
                        // API returned an error
                        Toast.makeText(EditProfileActivity.this, res.getError(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Server error
                    Toast.makeText(EditProfileActivity.this, "Server error: " + response.code(), Toast.LENGTH_LONG).show();
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