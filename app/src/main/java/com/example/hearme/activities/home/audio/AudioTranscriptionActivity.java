package com.example.hearme.activities.home.audio;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log; // ⭐️ IMPORT
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView; // ⭐️ IMPORT
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hearme.R;
import com.example.hearme.activities.MainActivity; // ⭐️ IMPORT
import com.example.hearme.activities.admin.AdminDashboardActivity; // ⭐️ IMPORT
import com.example.hearme.activities.history.ChatHistoryActivity; // ⭐️ IMPORT
import com.example.hearme.activities.profile.ProfileActivity; // ⭐️ IMPORT
import com.example.hearme.models.SessionManager; // ⭐️ IMPORT

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.*;

public class AudioTranscriptionActivity extends AppCompatActivity {
    private static final String TAG = "AudioTranscription"; // ⭐️ ADDED TAG
    private static final int PICK_AUDIO_REQUEST = 1;
    private Button uploadButton, transcribeButton, copyButton;
    private TextView fileNameTextView, transcriptionTextView;
    private ProgressBar progressBar;
    private Spinner languageSpinner;
    private File selectedFile;
    private String actualFileName;
    private String selectedLanguage = "ms";
    private final OkHttpClient client = new OkHttpClient();

    private SessionManager sessionManager; // ⭐️ ADDED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_transcription);

        // ⭐️ ADDED
        sessionManager = new SessionManager(this);

        // Initialize UI components
        uploadButton = findViewById(R.id.uploadButton);
        transcribeButton = findViewById(R.id.transcribeButton);
        copyButton = findViewById(R.id.copyButton);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        transcriptionTextView = findViewById(R.id.transcriptionTextView);
        progressBar = findViewById(R.id.progressBar);
        languageSpinner = findViewById(R.id.languageSpinner);

        // Initialize language spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] languageCodes = getResources().getStringArray(R.array.language_codes_array);
                selectedLanguage = languageCodes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Keep default language (Malay)
            }
        });

        // Set button listeners
        uploadButton.setOnClickListener(v -> openFilePicker());
        transcribeButton.setOnClickListener(v -> transcribeAudio());
        copyButton.setOnClickListener(v -> copyToClipboard());

        // ⭐️ ADDED
        setupBottomNavigation("home"); // This is a "home" feature
    }

    /**
     * Opens file picker to select audio file
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    /**
     * Gets the actual file name from a URI
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    /**
     * Handles the result from file picker
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    actualFileName = getFileNameFromUri(uri);
                    selectedFile = getFileFromUri(uri);
                    fileNameTextView.setText(actualFileName);
                    transcribeButton.setEnabled(true);
                    copyButton.setVisibility(View.GONE);
                    Toast.makeText(this, "File selected: " + actualFileName,
                            Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Error selecting file: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Converts URI to File object
     */
    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(getCacheDir(), "temp_audio_" + System.currentTimeMillis());
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
        return tempFile;
    }

    /**
     * Sends audio file to backend for transcription
     */
    private void transcribeAudio() {
        if (selectedFile == null) {
            Toast.makeText(this, "Please select an audio file first",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        transcribeButton.setEnabled(false);
        fileNameTextView.setText("Transcribing: " + actualFileName);
        copyButton.setVisibility(View.GONE);

        RequestBody fileBody = RequestBody.create(
                MediaType.parse("audio/*"),
                selectedFile
        );
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file",
                selectedFile.getName(),
                fileBody
        );

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(filePart)
                .addFormDataPart("language", selectedLanguage)
                .build();

        Request request = new Request.Builder()
                .url("https://taufiqzz-whisper-transcription.hf.space/transcribe")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    transcriptionTextView.setText("Error: " + e.getMessage());
                    transcribeButton.setEnabled(true);
                    fileNameTextView.setText(actualFileName);
                    copyButton.setVisibility(View.GONE);
                    Toast.makeText(AudioTranscriptionActivity.this,
                            "Transcription failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        transcriptionTextView.setText(responseData);
                        transcribeButton.setEnabled(true);
                        fileNameTextView.setText(actualFileName);
                        copyButton.setVisibility(View.VISIBLE);
                        Toast.makeText(AudioTranscriptionActivity.this,
                                "Transcription completed!",
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        transcriptionTextView.setText("Error: Server returned " + response.code());
                        transcribeButton.setEnabled(true);
                        fileNameTextView.setText(actualFileName);
                        copyButton.setVisibility(View.GONE);
                        Toast.makeText(AudioTranscriptionActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /**
     * Copies transcription text to clipboard
     */
    private void copyToClipboard() {
        String transcriptionText = transcriptionTextView.getText().toString();
        if (!transcriptionText.isEmpty() && !transcriptionText.equals("Transcription will appear here...")) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Transcription", transcriptionText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Transcription copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No transcription to copy", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ⭐️ IMPLEMENTED: This handles the android:onClick="back" from the XML ⭐️
     */
    public void back(View view) {
        finish();
    }


    // --- ⭐️ ADDED THIS ENTIRE METHOD ⭐️ ---

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

        // Get the inner parts of the dynamic button
        ImageView navGuideAdminIcon = bottomNavView.findViewById(R.id.nav_guide_admin_icon);
        TextView navGuideAdminText = bottomNavView.findViewById(R.id.nav_guide_admin_text);

        // 2. Check the role from SessionManager
        if (sessionManager.isAdmin()) {
            // --- ADMIN ---
            navGuideAdminText.setText("ADMIN");
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin); // (Requires ic_admin.png)

            navGuideAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                startActivity(intent);
                finish(); // Finish current activity
            });

            if ("admin".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107); // Semi-transparent yellow
            }

        } else {
            // --- USER ---
            navGuideAdminText.setText("GUIDE");
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help); // Use built-in icon

            navGuideAdmin.setOnClickListener(v -> {
                // Intent intent = new Intent(this, GuideActivity.class);
                // startActivity(intent);
                Toast.makeText(this, "Guide page coming soon", Toast.LENGTH_SHORT).show();
            });

            if ("guide".equals(activePage)) {
                navGuideAdmin.setBackgroundColor(0x55FFC107);
            }
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
            if (!"history".equals(activePage)) {
                Intent intent = new Intent(this, ChatHistoryActivity.class);
                startActivity(intent);
                finish();
            }
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
            navHistory.setBackgroundColor(0x55FFC107);
        } else if ("profile".equals(activePage)) {
            navProfile.setBackgroundColor(0x55FFC107);
        }
    }
}