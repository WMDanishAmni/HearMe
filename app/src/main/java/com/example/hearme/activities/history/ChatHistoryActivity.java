// file: ChatHistoryActivity.java
package com.example.hearme.activities.history;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hearme.R;
import com.example.hearme.activities.BaseActivity;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.activities.guide.GuideActivity;
import com.example.hearme.adapter.ChatHistoryAdapter;
import com.example.hearme.adapter.EmergencyHistoryAdapter;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ConversationApiService;
import com.example.hearme.api.EmergencyApiService;
import com.example.hearme.models.ChatHistoryModel;
import com.example.hearme.models.ChatHistoryResponseModel;
import com.example.hearme.models.ConversationResponseModel;
import com.example.hearme.models.EmergencyHistoryModel;
import com.example.hearme.models.EmergencyHistoryResponseModel;
import com.example.hearme.models.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatHistoryActivity extends BaseActivity {

    private static final String TAG = "ChatHistoryActivity";

    // --- UI Components ---
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyStateTitle, tvEmptyStateMessage;
    private TextView btnChatHistory, btnEmergencyHistory;

    // --- Adapters and Data Lists ---
    private ChatHistoryAdapter chatAdapter;
    private List<ChatHistoryModel> chatHistoryList = new ArrayList<>();
    private EmergencyHistoryAdapter emergencyAdapter;
    private List<EmergencyHistoryModel> emergencyHistoryList = new ArrayList<>();

    // --- State ---
    private SessionManager sessionManager;
    private boolean isChatMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        sessionManager = new SessionManager(this);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_chat_history);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        tvEmptyStateTitle = findViewById(R.id.tv_empty_state_title);
        tvEmptyStateMessage = findViewById(R.id.tv_empty_state_message);
        btnChatHistory = findViewById(R.id.btn_chat_history);
        btnEmergencyHistory = findViewById(R.id.btn_emergency_history);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ⭐️ LINE REMOVED ⭐️
        // findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_delete_all).setOnClickListener(v -> {
            if (isChatMode) {
                showDeleteAllConfirmationDialog();
            } else {
                Toast.makeText(this, "Delete all for emergency not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        btnChatHistory.setOnClickListener(v -> selectChatMode());
        btnEmergencyHistory.setOnClickListener(v -> selectEmergencyMode());

        setupBottomNavigation("history"); // Highlight the "history" tab

        updateUI();
    }

    private void selectChatMode() {
        if (isChatMode) return;
        isChatMode = true;
        updateUI();
    }

    private void selectEmergencyMode() {
        if (!isChatMode) return;
        isChatMode = false;
        updateUI();
    }

    private void updateUI() {
        updateToggleButtons();
        if (isChatMode) {
            findViewById(R.id.btn_delete_all).setVisibility(View.VISIBLE);
            fetchChatHistory();
        } else {
            findViewById(R.id.btn_delete_all).setVisibility(View.GONE);
            fetchEmergencyHistory();
        }
    }

    private void updateToggleButtons() {
        // Get colors from the theme
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        int colorSelectedText = typedValue.data;

        getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
        int colorUnselectedText = typedValue.data;

        if (isChatMode) {
            btnChatHistory.setBackgroundResource(R.drawable.toggle_selected);
            btnChatHistory.setTextColor(colorSelectedText); // Use theme color
            btnEmergencyHistory.setBackgroundResource(R.drawable.toggle_unselected);
            btnEmergencyHistory.setTextColor(colorUnselectedText); // Use theme color
        } else {
            btnChatHistory.setBackgroundResource(R.drawable.toggle_unselected);
            btnChatHistory.setTextColor(colorUnselectedText); // Use theme color
            btnEmergencyHistory.setBackgroundResource(R.drawable.toggle_selected);
            btnEmergencyHistory.setTextColor(colorSelectedText); // Use theme color
        }
    }

    // --- CHAT HISTORY METHODS ---

    private void fetchChatHistory() {
        if (!sessionManager.isLoggedIn()) {
            showEmptyState();
            return;
        }
        int userId = sessionManager.getUserId();

        ConversationApiService apiService = ApiClient.getClient().create(ConversationApiService.class);
        Call<ChatHistoryResponseModel> call = apiService.getChatHistory(userId);

        call.enqueue(new Callback<ChatHistoryResponseModel>() {
            @Override
            public void onResponse(Call<ChatHistoryResponseModel> call, Response<ChatHistoryResponseModel> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        chatHistoryList = response.body().getData();
                        if (chatHistoryList == null || chatHistoryList.isEmpty()) {
                            showEmptyState();
                        } else {
                            displayChatHistory();
                        }
                    } else {
                        showEmptyState();
                        String msg = (response.body() != null) ? response.body().getError() : "No chat history found";
                        Toast.makeText(ChatHistoryActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Chat response processing error", e);
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ChatHistoryResponseModel> call, Throwable t) {
                Log.e(TAG, "Chat network error", t);
                Toast.makeText(ChatHistoryActivity.this, "Network error", Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }

    private void displayChatHistory() {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        chatAdapter = new ChatHistoryAdapter(this, chatHistoryList, new ChatHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ChatHistoryModel chatItem) {
                Intent intent = new Intent(ChatHistoryActivity.this, ChatDetailActivity.class);
                intent.putExtra("chat_id", chatItem.getChat_id());
                intent.putExtra("hear", chatItem.getHear());
                intent.putExtra("speak", chatItem.getSpeak());
                startActivityForResult(intent, 1001); // Use forResult to refresh on delete
            }

            @Override
            public void onItemLongClick(ChatHistoryModel chatItem, int position) {
                showDeleteConfirmationDialog(chatItem, position);
            }
        });
        recyclerView.setAdapter(chatAdapter);
    }

    // --- EMERGENCY HISTORY METHODS ---

    private void fetchEmergencyHistory() {
        if (!sessionManager.isLoggedIn()) {
            showEmptyState();
            return;
        }
        String token = sessionManager.getToken();

        EmergencyApiService apiService = ApiClient.getClient().create(EmergencyApiService.class);
        Call<EmergencyHistoryResponseModel> call = apiService.getEmergencyHistory(token);

        call.enqueue(new Callback<EmergencyHistoryResponseModel>() {
            @Override
            public void onResponse(Call<EmergencyHistoryResponseModel> call, Response<EmergencyHistoryResponseModel> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        emergencyHistoryList = response.body().getData();
                        if (emergencyHistoryList == null || emergencyHistoryList.isEmpty()) {
                            showEmptyState();
                        } else {
                            displayEmergencyHistory();
                        }
                    } else {
                        showEmptyState();
                        String msg = (response.body() != null) ? response.body().getError() : "No emergency history found";
                        Toast.makeText(ChatHistoryActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Emergency response processing error", e);
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<EmergencyHistoryResponseModel> call, Throwable t) {
                Log.e(TAG, "Emergency network error", t);
                Toast.makeText(ChatHistoryActivity.this, "Network error", Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }

    private void displayEmergencyHistory() {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        emergencyAdapter = new EmergencyHistoryAdapter(this, emergencyHistoryList);
        recyclerView.setAdapter(emergencyAdapter);
    }


    // --- COMMON METHODS ---

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        if (isChatMode) {
            tvEmptyStateTitle.setText(getString(R.string.history_empty_chat_title));
            tvEmptyStateMessage.setText(getString(R.string.history_empty_chat_message));
        } else {
            tvEmptyStateTitle.setText(getString(R.string.history_empty_emergency_title));
            tvEmptyStateMessage.setText(getString(R.string.history_empty_emergency_message));
        }
    }

    private void showDeleteConfirmationDialog(ChatHistoryModel chatItem, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteChat(chatItem.getChat_id(), position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAllConfirmationDialog() {
        // This is a placeholder. A real "delete all" would need a new API endpoint.
        new AlertDialog.Builder(this)
                .setTitle("Delete All Conversations")
                .setMessage("This will only clear the list locally. Are you sure?")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    deleteAllChatsLocal();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void deleteChat(int chatId, int position) {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ConversationApiService apiService = ApiClient.getClient().create(ConversationApiService.class);
        Call<ConversationResponseModel> call = apiService.deleteConversation(chatId, token);

        call.enqueue(new Callback<ConversationResponseModel>() {
            @Override
            public void onResponse(Call<ConversationResponseModel> call, Response<ConversationResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ChatHistoryActivity.this, "Conversation deleted", Toast.LENGTH_SHORT).show();
                    chatHistoryList.remove(position);
                    chatAdapter.notifyItemRemoved(position);
                    chatAdapter.notifyItemRangeChanged(position, chatHistoryList.size());
                    if (chatHistoryList.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    String errorMsg = (response.body() != null) ? response.body().getMessage() : "Failed to delete";
                    Toast.makeText(ChatHistoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConversationResponseModel> call, Throwable t) {
                Toast.makeText(ChatHistoryActivity.this, "Network error: Could not delete", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Delete chat network error", t);
            }
        });
    }

    private void deleteAllChatsLocal() {
        if (chatHistoryList != null && chatAdapter != null) {
            chatHistoryList.clear();
            chatAdapter.notifyDataSetChanged();
        }
        showEmptyState();
        Toast.makeText(this, "All conversations cleared from list", Toast.LENGTH_SHORT).show();
    }


    /**
     * Sets up the bottom navigation bar based on the user's role.
     * @param activePage A string ("home", "history", "admin", "profile") to highlight the current page.
     */
    private void setupBottomNavigation(String activePage) {
        View bottomNavView = findViewById(R.id.bottom_navigation);
        if (bottomNavView == null) {
            Log.e(TAG, "FATAL: bottom_navigation view not found.");
            return;
        }

        // 1. Get references to all 4 nav buttons
        View navHome = bottomNavView.findViewById(R.id.nav_home);
        View navHistory = bottomNavView.findViewById(R.id.nav_history);
        View navGuideAdmin = bottomNavView.findViewById(R.id.nav_guide_admin); // The dynamic button
        View navProfile = bottomNavView.findViewById(R.id.nav_profile);

        if (navHome == null || navHistory == null || navGuideAdmin == null || navProfile == null) {
            Log.e(TAG, "FATAL: A navigation button was not found.");
            return;
        }

        ImageView navGuideAdminIcon = bottomNavView.findViewById(R.id.nav_guide_admin_icon);
        TextView navGuideAdminText = bottomNavView.findViewById(R.id.nav_guide_admin_text);

        // 2. Check the role from SessionManager
        if (sessionManager.isAdmin()) {
            // --- ADMIN ---
            navGuideAdminText.setText(getString(R.string.nav_admin)); // ⭐️ Use string res
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin); // (Requires ic_admin.png)

            navGuideAdmin.setOnClickListener(v -> {
                if (!"admin".equals(activePage)) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            // --- USER ---
            navGuideAdminText.setText(getString(R.string.nav_guide));
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help);

            navGuideAdmin.setOnClickListener(v -> {
                if (!"guide".equals(activePage)) {
                    Intent intent = new Intent(this, GuideActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                }
            });
        }

        // 3. Set click listeners for the other 3 buttons
        navHome.setOnClickListener(v -> {
            if (!"home".equals(activePage)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        navHistory.setOnClickListener(v -> {
            // Already on this page
        });

        navProfile.setOnClickListener(v -> {
            if (!"profile".equals(activePage)) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 4. Set highlight for the active page
        if ("home".equals(activePage)) {
            navHome.setBackgroundColor(0x55FFC107);
        } else if ("history".equals(activePage)) {
            navHistory.setBackgroundColor(0x55FFC107); // Highlight History
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107);
        } else if ("admin".equals(activePage) && sessionManager.isAdmin()) {
            navGuideAdmin.setBackgroundColor(0x55FFC107);
        } else if ("guide".equals(activePage) && !sessionManager.isAdmin()) {
            navGuideAdmin.setBackgroundColor(0x55FFC107);
        }
    }


    // This is used to refresh the list if a chat is deleted in ChatDetailActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (isChatMode) {
                fetchChatHistory();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the UI in case we are coming back to it
        updateUI();
        // Also refresh the nav bar
        setupBottomNavigation("history");
    }
}