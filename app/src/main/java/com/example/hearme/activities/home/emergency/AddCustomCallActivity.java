// file: AddCustomCallActivity.java
package com.example.hearme.activities.home.emergency;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hearme.R;
import com.example.hearme.adapter.CustomCallAdapter;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.EmergencyApiService;
import com.example.hearme.models.BasicResponse;
import com.example.hearme.models.CustomCallModel;
import com.example.hearme.models.CustomCallResponseModel;
import com.example.hearme.models.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCustomCallActivity extends AppCompatActivity implements CustomCallAdapter.OnDeleteClickListener {

    private static final String TAG = "AddCustomCallActivity";
    private static final int MAX_CONTACTS = 5;

    private EditText etContactName, etContactNumber;
    private Button btnSaveContact;
    private CardView cardAddContact;
    private RecyclerView recyclerView;
    private TextView tvNoContacts;

    private SessionManager sessionManager;
    private EmergencyApiService apiService;

    private CustomCallAdapter adapter;
    // ⭐️ This list is final. We will only clear() and addAll() to it.
    private final List<CustomCallModel> contactList = new ArrayList<>();
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom_call);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(EmergencyApiService.class);

        etContactName = findViewById(R.id.et_contact_name);
        etContactNumber = findViewById(R.id.et_contact_number);
        btnSaveContact = findViewById(R.id.btn_save_contact);
        cardAddContact = findViewById(R.id.card_add_contact);
        recyclerView = findViewById(R.id.recycler_custom_calls);
        tvNoContacts = findViewById(R.id.tv_no_contacts);

        setupHeader();
        setupRecyclerView();

        btnSaveContact.setOnClickListener(v -> saveContact());

        fetchContacts();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasChanges) {
                    setResult(RESULT_OK); // Tell EmergencyActivity to refresh
                }
                finish();
            }
        });
    }

    private void setupHeader() {
        View header = findViewById(R.id.header_layout);
        if (header != null) {
            View btnBack = header.findViewById(R.id.btn_back_header);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            }
        }
    }

    private void setupRecyclerView() {
        // The adapter is given the original 'contactList' and holds onto it
        adapter = new CustomCallAdapter(contactList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // ⭐️ --- FIX #1: THIS METHOD IS NOW CORRECT --- ⭐️
    private void fetchContacts() {
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getCustomCalls(token).enqueue(new Callback<CustomCallResponseModel>() {
            @Override
            public void onResponse(Call<CustomCallResponseModel> call, Response<CustomCallResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    List<CustomCallModel> newList = response.body().getData();

                    // 1. Clear the *original* list that the adapter is watching
                    contactList.clear();

                    // 2. Add all new items from the server into the original list
                    if (newList != null) {
                        contactList.addAll(newList);
                    }

                    // 3. Tell the adapter (which is still watching the original list) to refresh
                    adapter.notifyDataSetChanged();

                    checkContactListState();
                } else {
                    Toast.makeText(AddCustomCallActivity.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<CustomCallResponseModel> call, Throwable t) {
                Toast.makeText(AddCustomCallActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(CustomCallModel contact, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete " + contact.getCustom_name() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteContactFromApi(contact, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ⭐️ --- FIX #2: THIS METHOD IS NOW CORRECT --- ⭐️
    private void deleteContactFromApi(CustomCallModel contact, int position) {
        String token = sessionManager.getToken();
        apiService.deleteCustomCall(token, contact.getCustom_id()).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                    Toast.makeText(AddCustomCallActivity.this, "Contact deleted", Toast.LENGTH_SHORT).show();

                    // 1. Modify the original list
                    contactList.remove(position);
                    // 2. Notify the adapter of the specific removal
                    adapter.notifyItemRemoved(position);
                    // 3. Notify the adapter that item positions have changed
                    adapter.notifyItemRangeChanged(position, contactList.size());

                    checkContactListState();
                    hasChanges = true;
                } else {
                    Toast.makeText(AddCustomCallActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(AddCustomCallActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // This method is correct because it calls the fixed fetchContacts()
    private void saveContact() {
        String name = etContactName.getText().toString().trim();
        String number = etContactNumber.getText().toString().trim();
        String token = sessionManager.getToken();

        if (name.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, "Please enter both a name and a number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (token == null) {
            Toast.makeText(this, "Session expired, please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveContact.setEnabled(false);
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        apiService.saveCustomCall(token, name, number).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnSaveContact.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse res = response.body();
                    Toast.makeText(AddCustomCallActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();

                    if ("success".equalsIgnoreCase(res.getStatus())) {
                        hasChanges = true;
                        etContactName.setText("");
                        etContactNumber.setText("");
                        fetchContacts();
                    }
                } else {
                    Toast.makeText(AddCustomCallActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnSaveContact.setEnabled(true);
                Toast.makeText(AddCustomCallActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkContactListState() {
        int count = contactList.size();

        if (count >= MAX_CONTACTS) {
            cardAddContact.setVisibility(View.GONE);
        } else {
            cardAddContact.setVisibility(View.VISIBLE);
        }

        if (count == 0) {
            tvNoContacts.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoContacts.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}