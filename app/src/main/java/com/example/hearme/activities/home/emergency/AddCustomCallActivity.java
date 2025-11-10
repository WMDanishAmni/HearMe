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
import com.example.hearme.activities.BaseActivity;
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

public class AddCustomCallActivity extends BaseActivity implements CustomCallAdapter.OnDeleteClickListener {

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
        adapter = new CustomCallAdapter(contactList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void fetchContacts() {
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, getString(R.string.toast_session_expired), Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getCustomCalls(token).enqueue(new Callback<CustomCallResponseModel>() {
            @Override
            public void onResponse(Call<CustomCallResponseModel> call, Response<CustomCallResponseModel> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CustomCallModel> newList = response.body().getData();
                    contactList.clear();
                    if (newList != null) {
                        contactList.addAll(newList);
                    }
                    adapter.notifyDataSetChanged();
                    checkContactListState();
                } else {
                    Toast.makeText(AddCustomCallActivity.this, getString(R.string.toast_load_contacts_failed), Toast.LENGTH_SHORT).show();
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
                .setTitle(getString(R.string.add_contact_delete_title))
                .setMessage(getString(R.string.add_contact_delete_confirm, contact.getCustom_name()))
                .setPositiveButton(getString(R.string.add_contact_delete_button), (dialog, which) -> {
                    deleteContactFromApi(contact, position);
                })
                .setNegativeButton(getString(R.string.dialog_phrases_cancel), null) // Re-use cancel string
                .show();
    }

    private void deleteContactFromApi(CustomCallModel contact, int position) {
        String token = sessionManager.getToken();
        apiService.deleteCustomCall(token, contact.getCustom_id()).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                    Toast.makeText(AddCustomCallActivity.this, getString(R.string.toast_contact_deleted), Toast.LENGTH_SHORT).show();
                    contactList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, contactList.size());
                    checkContactListState();
                    hasChanges = true;
                } else {
                    Toast.makeText(AddCustomCallActivity.this, getString(R.string.toast_delete_failed), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(AddCustomCallActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveContact() {
        String name = etContactName.getText().toString().trim();
        String number = etContactNumber.getText().toString().trim();
        String token = sessionManager.getToken();

        if (name.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_contact_fill_both), Toast.LENGTH_SHORT).show();
            return;
        }
        if (token == null) {
            Toast.makeText(this, getString(R.string.toast_session_expired), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveContact.setEnabled(false);
        Toast.makeText(this, getString(R.string.toast_contact_saving), Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(AddCustomCallActivity.this, getString(R.string.toast_server_error, String.valueOf(response.code())), Toast.LENGTH_SHORT).show();
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