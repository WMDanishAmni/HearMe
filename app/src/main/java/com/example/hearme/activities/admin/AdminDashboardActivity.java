// file: AdminDashboardActivity.java
package com.example.hearme.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ApiInterface;
import com.example.hearme.models.AdminDashboardResponse;
import com.example.hearme.models.GetEmergencyUsersResponse;
import com.example.hearme.models.SessionManager; // <-- IMPORT MERGED SESSION MANAGER
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalUsers;
    private GridLayout containerEmergencyStats;
    private Button btnUserDetails;
    private ImageButton btnRefresh;
    private ApiInterface api;
    private BarChart barChart;
    private Handler handler = new Handler();

    private SessionManager sessionManager; // <-- ADD SESSION MANAGER

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // --- ADD THIS ---
        sessionManager = new SessionManager(this);

        // Initialize views
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        containerEmergencyStats = findViewById(R.id.containerEmergencyStats);
        btnUserDetails = findViewById(R.id.btnUserDetails);
        btnRefresh = findViewById(R.id.btnRefresh);
        barChart = findViewById(R.id.barChartEmergency);

        api = ApiClient.getClient().create(ApiInterface.class);

        setupBarChart();
        loadDashboardData();

        // 🔄 Refresh button click
        btnRefresh.setOnClickListener(v -> {
            loadDashboardData();
        });

        btnUserDetails.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminUserListActivity.class))
        );

        // --- ADD THIS ---
        // Setup the navigation bar, passing "admin" as the active page
        setupBottomNavigation("admin");
    }

    // --------------------------------------------------------------------
    // ALL YOUR ORIGINAL METHODS ARE PERFECT. NO CHANGES NEEDED BELOW HERE.
    // (setupBarChart, updateBarChart, loadDashboardData, showEmergencyUsers)
    // --------------------------------------------------------------------

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(true);
        barChart.setNoDataText("No emergency data available");

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(12f);
        barChart.getAxisRight().setEnabled(false);
    }

    private void updateBarChart(AdminDashboardResponse res) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (AdminDashboardResponse.EmergencyStat stat : res.getEmergencyStats()) {
            entries.add(new BarEntry(index, stat.getTotal()));
            labels.add(stat.getCategoryName());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Emergency Usage Frequency");

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(android.R.color.holo_red_light, null));
        colors.add(getResources().getColor(android.R.color.holo_orange_light, null));
        colors.add(getResources().getColor(android.R.color.holo_green_light, null));
        colors.add(getResources().getColor(android.R.color.holo_blue_light, null));
        colors.add(getResources().getColor(android.R.color.holo_purple, null));
        colors.add(getResources().getColor(android.R.color.holo_blue_dark, null));
        while (colors.size() < entries.size()) {
            colors.addAll(colors);
        }
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(getResources().getColor(android.R.color.black, null));

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void loadDashboardData() {
        api.getAdminDashboardData().enqueue(new Callback<AdminDashboardResponse>() {
            @Override
            public void onResponse(Call<AdminDashboardResponse> call, Response<AdminDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminDashboardResponse res = response.body();
                    tvTotalUsers.setText("Total Users: " + res.getTotalUsers());
                    updateBarChart(res);

                    containerEmergencyStats.removeAllViews();
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    float density = getResources().getDisplayMetrics().density;
                    int totalMarginPaddingPerColumn = (int) (32 * density);
                    int availableWidth = screenWidth;
                    int itemWidth = (availableWidth / containerEmergencyStats.getColumnCount()) - totalMarginPaddingPerColumn;

                    for (AdminDashboardResponse.EmergencyStat stat : res.getEmergencyStats()) {
                        View itemView = getLayoutInflater().inflate(R.layout.item_emergency_stat, containerEmergencyStats, false);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                        int margin = (int) (8 * density);
                        params.setMargins(margin, margin, margin, margin);
                        itemView.setLayoutParams(params);

                        ImageView iconButton = itemView.findViewById(R.id.iconButton);
                        TextView tv = itemView.findViewById(R.id.tvCategoryName);

                        String cat = stat.getCategoryName();
                        String categoryText = (cat != null ? cat : "Unknown") + " (" + stat.getTotal() + ")";
                        tv.setText(categoryText);

                        int iconRes;
                        if (cat != null) {
                            String catLower = cat.toLowerCase();
                            if (catLower.contains("accident")) iconRes = R.drawable.ic_accident;
                            else if (catLower.contains("theft")) iconRes = R.drawable.ic_theft;
                            else if (catLower.contains("health")) iconRes = R.drawable.ic_health;
                            else if (catLower.contains("fire")) iconRes = R.drawable.ic_fire;
                            else if (catLower.contains("wild")) iconRes = R.drawable.ic_wildlife;
                            else if (catLower.contains("injury")) iconRes = R.drawable.ic_injury;
                            else iconRes = android.R.drawable.ic_dialog_alert;
                        } else {
                            iconRes = android.R.drawable.ic_dialog_alert;
                        }
                        iconButton.setImageResource(iconRes);

                        if (cat != null) {
                            itemView.setOnClickListener(v -> showEmergencyUsers(stat.getCategoryName()));
                        }
                        containerEmergencyStats.addView(itemView);
                    }
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminDashboardResponse> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEmergencyUsers(String categoryName) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_emergency_users, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvCategoryTitle);
        ProgressBar progressBar = sheetView.findViewById(R.id.progressBar);
        ListView listUsers = sheetView.findViewById(R.id.listUsers);

        tvTitle.setText(categoryName);

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<GetEmergencyUsersResponse> call = api.getEmergencyUsers(categoryName);

        call.enqueue(new Callback<GetEmergencyUsersResponse>() {
            @Override
            public void onResponse(Call<GetEmergencyUsersResponse> call, Response<GetEmergencyUsersResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GetEmergencyUsersResponse res = response.body();

                    if ("success".equals(res.getStatus())) {
                        List<GetEmergencyUsersResponse.UserData> users = res.getUsers();

                        if (users != null && !users.isEmpty()) {
                            ArrayAdapter<GetEmergencyUsersResponse.UserData> adapter = new ArrayAdapter<GetEmergencyUsersResponse.UserData>(
                                    AdminDashboardActivity.this,
                                    R.layout.item_user_report,
                                    R.id.tvReportUserName,
                                    users
                            ) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    GetEmergencyUsersResponse.UserData user = getItem(position);

                                    TextView tvName = view.findViewById(R.id.tvReportUserName);
                                    TextView tvPhone = view.findViewById(R.id.tvReportUserPhone);
                                    TextView tvLocation = view.findViewById(R.id.tvReportLocation);

                                    if (user != null) {
                                        tvName.setText(user.getFullName());
                                        tvPhone.setText(user.getPhoneNo());
                                        tvLocation.setText(user.getLocation());
                                    }
                                    return view;
                                }
                            };
                            listUsers.setAdapter(adapter);
                        } else {
                            listUsers.setAdapter(new ArrayAdapter<>(
                                    AdminDashboardActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    new String[]{"No users found for this category."}
                            ));
                        }
                    } else {
                        Toast.makeText(AdminDashboardActivity.this, "API Error: " + res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetEmergencyUsersResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        bottomSheetDialog.show();
    }


    // --- ⬇️ NEW METHOD FOR DYNAMIC NAVIGATION ⬇️ ---

    /**
     * Sets up the bottom navigation bar based on the user's role.
     * @param activePage A string ("home", "history", "admin", "profile") to highlight the current page.
     */
    private void setupBottomNavigation(String activePage) {
        // 1. Get references to all 4 nav buttons
        View navHome = findViewById(R.id.nav_home);
        View navHistory = findViewById(R.id.nav_history);
        View navGuideAdmin = findViewById(R.id.nav_guide_admin); // The dynamic button
        View navProfile = findViewById(R.id.nav_profile);

        // Get the inner parts of the dynamic button
        ImageView navGuideAdminIcon = findViewById(R.id.nav_guide_admin_icon);
        TextView navGuideAdminText = findViewById(R.id.nav_guide_admin_text);

        // 2. Check the role from SessionManager
        if (sessionManager.isAdmin()) {
            // --- ADMIN ---
            navGuideAdminText.setText("ADMIN");
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin); // (Requires ic_admin.png)

            navGuideAdmin.setOnClickListener(v -> {
                // Already on this page, do nothing or refresh
                if (!"admin".equals(activePage)) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    startActivity(intent);
                }
            });

            // Highlight if we are on the admin page
            if ("admin".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107); // Semi-transparent yellow
            }

        } else {
            // --- USER ---
            navGuideAdminText.setText("GUIDE");
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help); // (Requires ic_guide.png)

            navGuideAdmin.setOnClickListener(v -> {
                // Intent intent = new Intent(this, GuideActivity.class);
                // startActivity(intent);
                Toast.makeText(this, "Guide page coming soon", Toast.LENGTH_SHORT).show();
            });

            // Highlight if we are on the guide page
            if ("guide".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107);
            }
        }

        // 3. Set click listeners for the other 3 buttons
        navHome.setOnClickListener(v -> {
            if (!"home".equals(activePage)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Go to top
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

        // 4. Set highlight for the active page (if not already set for admin/guide)
        if ("home".equals(activePage)) {
            navHome.setBackgroundColor(0x55FFC107);
        } else if ("history".equals(activePage)) {
            navHistory.setBackgroundColor(0x55FFC107);
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107);
        }
    }
}