// file: AdminUserListActivity.java
package com.example.hearme.activities.admin;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.activities.BaseActivity;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ApiInterface;
import com.example.hearme.models.AdminUserListResponse;
import com.example.hearme.models.SessionManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserListActivity extends BaseActivity {

    private EditText etSearch;
    private Button btnSearch;
    private ListView listUsers;
    private Spinner spinnerSort;
    private ProgressBar progressBar;
    private ApiInterface api;
    private List<AdminUserListResponse.UserInfo> userList;
    private TextView tvEmpty;
    private ImageView btnRefresh;
    private ImageView btn_back;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        sessionManager = new SessionManager(this);

        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        listUsers = findViewById(R.id.listUsers);
        spinnerSort = findViewById(R.id.spinnerSort);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressBar = findViewById(R.id.progressBar);
        btn_back = findViewById(R.id.btn_back);

        tvEmpty = new TextView(this);
        tvEmpty.setText(getString(R.string.admin_user_list_empty)); // ⭐️ USE STRING
        tvEmpty.setTextSize(16f);

        // --- ⭐️ Get theme-aware text color ⭐️ ---
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
        tvEmpty.setTextColor(typedValue.data); // ⭐️ USE THEME COLOR
        // --- ⭐️ End theme-aware color ⭐️ ---

        tvEmpty.setPadding(10, 30, 10, 30);
        ((ViewGroup)listUsers.getParent()).addView(tvEmpty);
        listUsers.setEmptyView(tvEmpty);


        btn_back.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish(); // This correctly goes back to AdminDashboard
        });

        api = ApiClient.getClient().create(ApiInterface.class);

        String[] sortOptions = {"Sort by: A–Z", "Sort by: Z–A", "Newest First", "Oldest First"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortOptions);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortUserList(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            loadUserList(query);
        });

        btnRefresh.setOnClickListener(v -> loadUserList(""));

        loadUserList("");

        listUsers.setOnItemClickListener((parent, view, position, id) -> {
            AdminUserListResponse.UserInfo selectedUser = userList.get(position);
            showUserDialog(selectedUser);
        });

        // Setup nav bar, highlight "admin"
        setupBottomNavigation("admin");
    }

    private void loadUserList(String search) {
        progressBar.setVisibility(View.VISIBLE);
        listUsers.setVisibility(View.GONE);

        api.getAdminUserList(search).enqueue(new Callback<AdminUserListResponse>() {
            @Override
            public void onResponse(Call<AdminUserListResponse> call, Response<AdminUserListResponse> response) {
                progressBar.setVisibility(View.GONE);
                listUsers.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    AdminUserListResponse res = response.body();
                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        userList = res.getUsers();
                        updateUserListView();
                    } else {
                        userList = null;
                        updateUserListView();
                    }
                } else {
                    Toast.makeText(AdminUserListActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AdminUserListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminUserListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sortUserList(int sortOption) {
        if (userList == null || userList.isEmpty()) return;

        switch (sortOption) {
            case 0: Collections.sort(userList, Comparator.comparing(AdminUserListResponse.UserInfo::getUsername, String.CASE_INSENSITIVE_ORDER)); break;
            case 1: Collections.sort(userList, Comparator.comparing(AdminUserListResponse.UserInfo::getUsername, String.CASE_INSENSITIVE_ORDER).reversed()); break;
            case 2: Collections.sort(userList, (u1, u2) -> u2.getCreated_at().compareTo(u1.getCreated_at())); break;
            case 3: Collections.sort(userList, (u1, u2) -> u1.getCreated_at().compareTo(u2.getCreated_at())); break;
        }
        updateUserListView();
    }

    private void updateUserListView() {
        if (userList == null || userList.isEmpty()) {
            listUsers.setAdapter(null);
            return;
        }

        ArrayAdapter<AdminUserListResponse.UserInfo> adapter = new ArrayAdapter<>(this, R.layout.item_user, R.id.tvUsername, userList) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tvUsername = view.findViewById(R.id.tvUsername);
                TextView tvEmail = view.findViewById(R.id.tvEmail);
                TextView tvPhone = view.findViewById(R.id.tvPhone);

                AdminUserListResponse.UserInfo user = userList.get(position);
                tvUsername.setText(user.getUsername());
                // ⭐️ USE STRING RESOURCES ⭐️
                tvEmail.setText(getString(R.string.admin_user_list_email, user.getEmail()));
                tvPhone.setText(getString(R.string.admin_user_list_phone, user.getPhone_no()));
                return view;
            }
        };

        listUsers.setAdapter(adapter);
    }

    private void showUserDialog(AdminUserListResponse.UserInfo user) {
        // ⭐️ USE STRING RESOURCE ⭐️
        String info = getString(R.string.admin_user_dialog_info,
                user.getUsername(),
                user.getEmail(),
                user.getFull_name(),
                user.getAddress(),
                user.getPhone_no(),
                user.getCreated_at()
        );

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.admin_user_dialog_title)) // ⭐️ USE STRING
                .setMessage(info)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Sets up the bottom navigation bar based on the user's role.
     * (This method is already using string resources, so it's perfect)
     */
    private void setupBottomNavigation(String activePage) {
        View navHome = findViewById(R.id.nav_home);
        View navHistory = findViewById(R.id.nav_history);
        View navGuideAdmin = findViewById(R.id.nav_guide_admin);
        View navProfile = findViewById(R.id.nav_profile);

        ImageView navGuideAdminIcon = findViewById(R.id.nav_guide_admin_icon);
        TextView navGuideAdminText = findViewById(R.id.nav_guide_admin_text);

        if (sessionManager.isAdmin()) {
            // --- ADMIN ---
            navGuideAdminText.setText(getString(R.string.nav_admin));
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin);

            navGuideAdmin.setOnClickListener(v -> {
                if (!"admin".equals(activePage)) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    startActivity(intent);
                }
            });

            if ("admin".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107);
            }

        } else {
            // --- USER ---
            navGuideAdminText.setText(getString(R.string.nav_guide));
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help);

            navGuideAdmin.setOnClickListener(v -> {
                Toast.makeText(this, "Guide page coming soon", Toast.LENGTH_SHORT).show();
            });

            if ("guide".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107);
            }
        }

        // --- Set click listeners for the other buttons ---
        navHome.setOnClickListener(v -> {
            if (!"home".equals(activePage)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        navHistory.setOnClickListener(v -> {
            if (!"history".equals(activePage)) {
                Intent intent = new Intent(this, ChatHistoryActivity.class);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!"profile".equals(activePage)) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // --- Set highlights for the active page ---
        if ("home".equals(activePage)) {
            navHome.setBackgroundColor(0x55FFC107);
        } else if ("history".equals(activePage)) {
            navHistory.setBackgroundColor(0x55FFC107);
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107);
        }
    }
}