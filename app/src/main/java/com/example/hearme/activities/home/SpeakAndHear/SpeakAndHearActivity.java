// file: SpeakAndHearActivity.java
package com.example.hearme.activities.home.SpeakAndHear; // Make sure this package is correct

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.hearme.R;
import com.example.hearme.activities.BaseActivity;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity; // ⭐️ IMPORT
import com.example.hearme.activities.guide.GuideActivity;
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.adapter.PhraseAdapter;
import com.example.hearme.api.ApiClient;
import com.example.hearme.api.ConversationApiService;
import com.example.hearme.api.CustomPhraseApiService;
import com.example.hearme.models.ConversationResponseModel;
import com.example.hearme.models.CustomPhraseListResponseModel;
import com.example.hearme.models.CustomPhraseModel;
import com.example.hearme.models.CustomPhraseResponseModel;
import com.example.hearme.models.PhraseItem;
import com.example.hearme.models.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeakAndHearActivity extends BaseActivity implements TextToSpeech.OnInitListener {
    private static final String TAG = "SpeakAndHearActivity";

    // Session manager
    private SessionManager sessionManager;

    // UI Components
    private ImageView btnBack;
    private CardView toggleHearFrom, toggleSpeakTo, cardReply, btnSpeak, btnClear, btnReplyToHear;
    private LinearLayout hearModeLayout;
    private ScrollView speakModeLayout;
    private TextView tvTranscription, btnEnglish, btnMalay;
    private EditText etMessage;
    private ImageButton btnMicrophone;
    private TextView tvMessageLabel, tvClearButton, tvSpeakButton;

    // Mode and Language settings
    private boolean isHearMode = false;
    private String currentLanguage = "en-US";
    private boolean isMalay = false;
    private boolean isListening = false;

    // TTS
    private TextToSpeech textToSpeech;
    private boolean isTTSReady = false;

    // Custom phrases (fetched from DB)
    private List<CustomPhraseModel> customPhrases = new ArrayList<>();

    // Variable to store transcription
    private String transcriptionHistory = "";

    // Temporary list for TTS messages
    private List<String> temporarySpeakMessages = new ArrayList<>();

    // Flag to prevent multiple saves
    private boolean isSaving = false;

    // --- ⭐️ Theme-aware colors --- ⭐️
    private int colorOnPrimary;
    private int colorTextSecondary;
    private int colorTextPrimary;
    private int colorHint;
    private int colorSurface;
    private int colorHoloBlue;
    // ⭐️ --- End theme-aware colors --- ⭐️

    // Activity result launchers
    private final ActivityResultLauncher<Intent> speechRecognizerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Speech recognition result received");
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> arrayList = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (arrayList != null && !arrayList.isEmpty()) {
                        String transcribedText = arrayList.get(0);
                        Log.d(TAG, "Transcribed text: " + transcribedText);

                        if (transcriptionHistory.isEmpty()) {
                            transcriptionHistory = transcribedText;
                        } else {
                            transcriptionHistory = transcriptionHistory + "\n\n" + transcribedText;
                        }

                        if (tvTranscription != null) {
                            tvTranscription.setText(transcriptionHistory);
                            tvTranscription.setTextColor(colorTextPrimary); // ⭐️ USE THEME COLOR
                        }
                    }
                }
                isListening = false;
                updateMicrophoneButton();
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                Log.d(TAG, "Permission result: " + isGranted);
                if (isGranted) {
                    startSpeechRecognition();
                } else {
                    // ⭐️ USE STRING ⭐️
                    String message = isMalay ?
                            getString(R.string.toast_mic_permission_required) :
                            getString(R.string.toast_mic_permission_required);
                    Toast.makeText(SpeakAndHearActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        // ⭐️ REMOVED: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // BaseActivity now handles this
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak_to_nondeaf);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            String u = sessionManager.getUsername();
            Toast.makeText(this, "Session active: " + (u != null ? u : "user"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
        }

        String initialMode = "SPEAK";
        if (getIntent().hasExtra("INITIAL_MODE")) {
            initialMode = getIntent().getStringExtra("INITIAL_MODE");
        }

        isHearMode = "HEAR".equals(initialMode);
        Log.d(TAG, "Initial mode: " + initialMode);

        loadThemeColors(); // ⭐️ ADDED
        initializeViews();
        setupLanguageToggle();
        setupClickListeners();
        initializeTextToSpeech();
        updateModeDisplay();
        updateLanguageToggle();
        updateUITexts();

        fetchCustomPhrases();

        setupBottomNavigation("home");
    }

    // ⭐️ --- ADDED THIS NEW METHOD --- ⭐️
    private void loadThemeColors() {
        android.util.TypedValue typedValue = new android.util.TypedValue();

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        colorOnPrimary = typedValue.data;

        getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
        colorTextSecondary = typedValue.data;

        getTheme().resolveAttribute(android.R.attr.textColor, typedValue, true);
        colorTextPrimary = typedValue.data;

        getTheme().resolveAttribute(android.R.attr.textColorHint, typedValue, true);
        colorHint = typedValue.data;

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        colorSurface = typedValue.data;

        // This color is not in the theme, so we load it from resources
        colorHoloBlue = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
    }
    // ⭐️ --- END NEW METHOD --- ⭐️

    @SuppressLint("WrongViewCast")
    private void initializeViews() {
        Log.d(TAG, "initializeViews called");
        btnBack = findViewById(R.id.btn_back);
        toggleHearFrom = findViewById(R.id.toggle_hear_from);
        toggleSpeakTo = findViewById(R.id.toggle_speak_to);
        hearModeLayout = findViewById(R.id.hear_mode_layout);
        speakModeLayout = findViewById(R.id.speak_mode_layout);
        tvTranscription = findViewById(R.id.tv_transcription);
        btnMicrophone = findViewById(R.id.btn_microphone);
        cardReply = findViewById(R.id.card_reply);
        etMessage = findViewById(R.id.et_message);
        btnSpeak = findViewById(R.id.btn_speak);
        btnClear = findViewById(R.id.btn_clear);
        btnReplyToHear = findViewById(R.id.btn_reply_to_hear);
        tvMessageLabel = findViewById(R.id.tv_message_label);
        tvClearButton = findViewById(R.id.tv_clear_button);
        tvSpeakButton = findViewById(R.id.tv_speak_button);
        btnEnglish = findViewById(R.id.btn_english);
        btnMalay = findViewById(R.id.btn_malay);
    }

    private void setupLanguageToggle() {
        Log.d(TAG, "setupLanguageToggle called");
        btnEnglish.setOnClickListener(v -> selectLanguage("en-US"));
        btnMalay.setOnClickListener(v -> selectLanguage("ms-MY"));
    }

    private void selectLanguage(String languageCode) {
        Log.d(TAG, "selectLanguage called: " + languageCode);
        currentLanguage = languageCode;
        isMalay = languageCode.equals("ms-MY");
        updateLanguageToggle();
        updateUITexts(); // Your original method

        if (textToSpeech != null && isTTSReady) {
            Locale locale = isMalay ? new Locale("ms", "MY") : Locale.US;
            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // ⭐️ USE STRING ⭐️
                    String message = isMalay ?
                            getString(R.string.toast_language_not_supported) :
                            getString(R.string.toast_language_not_supported);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }
        }
        fetchCustomPhrases();
        // ⭐️ USE STRING ⭐️
        String message = isMalay ? getString(R.string.toast_language_malay) : getString(R.string.toast_language_english);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateLanguageToggle() {
        Log.d(TAG, "updateLanguageToggle called");
        if (currentLanguage.equals("en-US")) {
            btnEnglish.setBackgroundResource(R.drawable.language_selected);
            btnEnglish.setTextColor(colorOnPrimary); // ⭐️ USE THEME COLOR
            btnMalay.setBackgroundResource(R.drawable.language_unselected);
            btnMalay.setTextColor(colorTextSecondary); // ⭐️ USE THEME COLOR
        } else {
            btnMalay.setBackgroundResource(R.drawable.language_selected);
            btnMalay.setTextColor(colorOnPrimary); // ⭐️ USE THEME COLOR
            btnEnglish.setBackgroundResource(R.drawable.language_unselected);
            btnEnglish.setTextColor(colorTextSecondary); // ⭐️ USE THEME COLOR
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners called");
        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // Toggle buttons
        toggleHearFrom.setOnClickListener(v -> {
            Log.d(TAG, "Hear mode toggle clicked");
            switchToHearMode();
        });
        toggleSpeakTo.setOnClickListener(v -> {
            Log.d(TAG, "Speak mode toggle clicked");
            switchToSpeakMode();
        });

        // Hear mode buttons
        btnMicrophone.setOnClickListener(v -> {
            Log.d(TAG, "Microphone button clicked");
            if (!isListening) {
                checkPermissionAndStartSpeechRecognition();
            }
        });

        cardReply.setOnClickListener(v -> {
            Log.d(TAG, "Reply button clicked");
            switchToSpeakMode();
        });

        // Speak mode buttons
        btnSpeak.setOnClickListener(v -> {
            Log.d(TAG, "Speak button clicked");
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                temporarySpeakMessages.add(text + "\n");
                Log.d(TAG, "Added to temporary messages: " + text);
                speakText(text);
            } else {
                // ⭐️ USE STRING ⭐️
                String message = isMalay ?
                        getString(R.string.toast_speak_enter_text) :
                        getString(R.string.toast_speak_enter_text);
                Toast.makeText(SpeakAndHearActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        btnClear.setOnClickListener(v -> {
            Log.d(TAG, "Clear button clicked");
            etMessage.setText("");
            etMessage.requestFocus();
        });

        btnReplyToHear.setOnClickListener(v -> {
            Log.d(TAG, "Reply to hear button clicked");
            switchToHearMode();
        });

        etMessage.setOnLongClickListener(v -> {
            Log.d(TAG, "EditText long pressed");
            showPhrasesDialog();
            return true;
        });
    }

    private void showPhrasesDialog() {
        // (This method is unchanged from your original)
        Log.d(TAG, "showPhrasesDialog called");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isMalay ? "Frasa Biasa" : "Common Phrases"); // ⭐️ YOUR LOGIC
        List<PhraseItem> phrasesList = preparePhrasesList();
        PhraseAdapter adapter = new PhraseAdapter(this, phrasesList);
        builder.setAdapter(adapter, (dialog, which) -> {
            PhraseItem selectedItem = phrasesList.get(which);
            if (selectedItem.getType() == PhraseItem.TYPE_PHRASE) {
                String selectedPhrase = selectedItem.getText();
                String currentText = etMessage.getText().toString();
                if (currentText.isEmpty()) {
                    etMessage.setText(selectedPhrase);
                } else {
                    etMessage.setText(currentText + " " + selectedPhrase);
                }
                etMessage.setSelection(etMessage.getText().length());
            }
        });
        builder.setNeutralButton(isMalay ? "Tambah Frasa" : "Add Phrases", (dialog, which) -> { // ⭐️ YOUR LOGIC
            showAddPhraseDialog();
        });
        builder.setNegativeButton(isMalay ? "Batal" : "Cancel", null); // ⭐️ YOUR LOGIC
        builder.show();
    }


    private List<PhraseItem> preparePhrasesList() {
        // (This method is unchanged from your original)
        Log.d(TAG, "preparePhrasesList called");
        List<PhraseItem> items = new ArrayList<>();
        Map<String, List<CustomPhraseModel>> customCategoryMap = new HashMap<>();
        if (customPhrases != null) {
            for (CustomPhraseModel customPhrase : customPhrases) {
                String phraseLang = customPhrase.getLanguage();
                boolean langMatch = (isMalay && "ms".equals(phraseLang)) || (!isMalay && "en".equals(phraseLang));

                if (langMatch) {
                    String category = customPhrase.getCategory();
                    if (!customCategoryMap.containsKey(category)) {
                        customCategoryMap.put(category, new ArrayList<>());
                    }
                    customCategoryMap.get(category).add(customPhrase);
                }
            }
        }
        if (isMalay) {
            items.add(new PhraseItem("Ucapan", "UCAPAN", PhraseItem.TYPE_HEADER));
            items.add(new PhraseItem("Ucapan", "Assalamualaikum", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Ucapan", "Apa khabar?", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Ucapan", "Awak sihat?", PhraseItem.TYPE_PHRASE));
            if (customCategoryMap.containsKey("Ucapan")) {
                for (CustomPhraseModel custom : customCategoryMap.get("Ucapan")) {
                    items.add(new PhraseItem(custom.getCategory(), custom.getPhrase(), PhraseItem.TYPE_PHRASE));
                }
                customCategoryMap.remove("Ucapan");
            }
            items.add(new PhraseItem("Penghargaan", "PENGHARGAAN", PhraseItem.TYPE_HEADER));
            items.add(new PhraseItem("Penghargaan", "Terima kasih", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Penghargaan", "Sama-sama", PhraseItem.TYPE_PHRASE));
            if (customCategoryMap.containsKey("Penghargaan")) {
                for (CustomPhraseModel custom : customCategoryMap.get("Penghargaan")) {
                    items.add(new PhraseItem(custom.getCategory(), custom.getPhrase(), PhraseItem.TYPE_PHRASE));
                }
                customCategoryMap.remove("Penghargaan");
            }
            items.add(new PhraseItem("Pertanyaan/Pertolongan", "PERTANYAAN/PERTOLONGAN", PhraseItem.TYPE_HEADER));
            items.add(new PhraseItem("Pertanyaan/Pertolongan", "Maaf tumpang tanya", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Pertanyaan/Pertolongan", "Tandas di mana ye", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Pertanyaan/Pertolongan", "Surau di mana ye", PhraseItem.TYPE_PHRASE));
            List<CustomPhraseModel> list = new ArrayList<>();
            if (customCategoryMap.containsKey("Pertanyaan")) {
                list.addAll(customCategoryMap.get("Pertanyaan"));
                customCategoryMap.remove("Pertanyaan");
            }
            if (customCategoryMap.containsKey("Pertanyaan/Pertolongan")) {
                list.addAll(customCategoryMap.get("Pertanyaan/Pertolongan"));
                customCategoryMap.remove("Pertanyaan/Pertolongan");
            }
            for (CustomPhraseModel custom : list) {
                items.add(new PhraseItem(custom.getCategory(), custom.getPhrase(), PhraseItem.TYPE_PHRASE));
            }
        } else {
            items.add(new PhraseItem("Greetings", "GREETINGS", PhraseItem.TYPE_HEADER));
            items.add(new PhraseItem("Greetings", "Hello", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Greetings", "How are you?", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Greetings", "Are you well?", PhraseItem.TYPE_PHRASE));
            if (customCategoryMap.containsKey("Greetings")) {
                for (CustomPhraseModel custom : customCategoryMap.get("Greetings")) {
                    items.add(new PhraseItem(custom.getCategory(), custom.getPhrase(), PhraseItem.TYPE_PHRASE));
                }
                customCategoryMap.remove("Greetings");
            }
            items.add(new PhraseItem("Thanks", "THANKS", PhraseItem.TYPE_HEADER));
            items.add(new PhraseItem("Thanks", "Thank you", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Thanks", "You're welcome", PhraseItem.TYPE_PHRASE));
            if (customCategoryMap.containsKey("Thanks")) {
                for (CustomPhraseModel custom : customCategoryMap.get("Thanks")) {
                    items.add(new PhraseItem(custom.getCategory(), custom.getPhrase(), PhraseItem.TYPE_PHRASE));
                }
                customCategoryMap.remove("Thanks");
            }
            items.add(new PhraseItem("Questions/Help", "QUESTIONS/HELP", PhraseItem.TYPE_HEADER));
            items.add(new PhraseItem("Questions/Help", "Excuse me", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Questions/Help", "Where is the toilet?", PhraseItem.TYPE_PHRASE));
            items.add(new PhraseItem("Questions/Help", "Where is the surau?", PhraseItem.TYPE_PHRASE));
            List<CustomPhraseModel> list = new ArrayList<>();
            if (customCategoryMap.containsKey("Questions")) {
                list.addAll(customCategoryMap.get("Questions"));
                customCategoryMap.remove("Questions");
            }
            if (customCategoryMap.containsKey("Questions/Help")) {
                list.addAll(customCategoryMap.get("Questions/Help"));
                customCategoryMap.remove("Questions/Help");
            }
            for (CustomPhraseModel custom : list) {
                items.add(new PhraseItem(custom.getCategory(), custom.getPhrase(), PhraseItem.TYPE_PHRASE));
            }
        }
        for (Map.Entry<String, List<CustomPhraseModel>> entry : customCategoryMap.entrySet()) {
            String categoryName = entry.getKey();
            List<CustomPhraseModel> phrases = entry.getValue();
            items.add(new PhraseItem(categoryName, categoryName.toUpperCase(), PhraseItem.TYPE_HEADER));
            for (CustomPhraseModel custom : phrases) {
                items.add(new PhraseItem(custom.getCategory(), custom.getPhrase(), PhraseItem.TYPE_PHRASE));
            }
        }
        return items;
    }


    private void showAddPhraseDialog() {
        // (This method is unchanged from your original)
        Log.d(TAG, "showAddPhraseDialog called");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isMalay ? "Tambah Frasa Baharu" : "Add New Phrase"); // ⭐️ YOUR LOGIC
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        isMalay ? "Ucapan" : "Greetings",
                        isMalay ? "Penghargaan" : "Thanks",
                        isMalay ? "Pertanyaan" : "Questions"
                }); // ⭐️ YOUR LOGIC
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        EditText input = new EditText(this);
        input.setHint(isMalay ? "Masukkan frasa anda" : "Enter your phrase"); // ⭐️ YOUR LOGIC
        layout.addView(spinner);
        layout.addView(input);
        builder.setView(layout);
        builder.setPositiveButton(isMalay ? "Simpan" : "Save", (dialog, which) -> { // ⭐️ YOUR LOGIC
            String category = spinner.getSelectedItem().toString();
            String phrase = input.getText().toString().trim();
            if (!phrase.isEmpty()) saveCustomPhrase(category, phrase);
            else
                Toast.makeText(this, isMalay ? "Sila masukkan frasa" : "Please enter a phrase", Toast.LENGTH_SHORT).show(); // ⭐️ YOUR LOGIC
        });
        builder.setNegativeButton(isMalay ? "Batal" : "Cancel", null); // ⭐️ YOUR LOGIC
        builder.show();
    }

    private void saveCustomPhrase(String category, String phrase) {
        // ⭐️ UPDATED TO USE STRING RESOURCES FOR TOASTS ⭐️
        Log.d(TAG, "saveCustomPhrase called: " + category + ", " + phrase);
        if (sessionManager == null) sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, getString(R.string.toast_add_phrase_login_required), Toast.LENGTH_SHORT).show();
            return;
        }
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, getString(R.string.toast_add_phrase_invalid_id), Toast.LENGTH_SHORT).show();
            return;
        }
        CustomPhraseApiService apiService = ApiClient.getClient().create(CustomPhraseApiService.class);
        Call<CustomPhraseResponseModel> call = apiService.saveCustomPhrase(userId, category, phrase, isMalay ? "ms" : "en");
        call.enqueue(new Callback<CustomPhraseResponseModel>() {
            @Override
            public void onResponse(Call<CustomPhraseResponseModel> call, Response<CustomPhraseResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SpeakAndHearActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    fetchCustomPhrases();
                } else {
                    Toast.makeText(SpeakAndHearActivity.this, getString(R.string.toast_add_phrase_fail), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<CustomPhraseResponseModel> call, Throwable t) {
                Toast.makeText(SpeakAndHearActivity.this, getString(R.string.toast_add_phrase_fail_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCustomPhrases() {
        // (This method is unchanged from your original)
        Log.d(TAG, "fetchCustomPhrases called");
        if (sessionManager == null) sessionManager = new SessionManager(this);
        int userId = sessionManager.isLoggedIn() ? sessionManager.getUserId() : 1;
        CustomPhraseApiService apiService = ApiClient.getClient().create(CustomPhraseApiService.class);
        Call<CustomPhraseListResponseModel> call = apiService.getCustomPhrases(userId, isMalay ? "ms" : "en");
        call.enqueue(new Callback<CustomPhraseListResponseModel>() {
            @Override
            public void onResponse(Call<CustomPhraseListResponseModel> call, Response<CustomPhraseListResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    customPhrases.clear();
                    if (response.body().getPhrases() != null)
                        customPhrases.addAll(response.body().getPhrases());
                } else {
                    Log.e(TAG, "fetchCustomPhrases server error");
                }
            }
            @Override
            public void onFailure(Call<CustomPhraseListResponseModel> call, Throwable t) {
                Log.e(TAG, "fetchCustomPhrases failure", t);
            }
        });
    }

    private void switchToHearMode() {
        // ⭐️ UPDATED TO USE THEME COLOR ⭐️
        Log.d(TAG, "switchToHearMode called");
        isHearMode = true;
        updateModeDisplay();
        updateUITexts();
        if (!transcriptionHistory.isEmpty() && tvTranscription != null) {
            tvTranscription.setText(transcriptionHistory);
            tvTranscription.setTextColor(colorTextPrimary); // ⭐️ USE THEME COLOR
        }
    }

    private void switchToSpeakMode() {
        // (This method is unchanged from your original)
        Log.d(TAG, "switchToSpeakMode called");
        isHearMode = false;
        updateModeDisplay();
        updateUITexts();
    }

    private void updateModeDisplay() {
        // ⭐️ UPDATED TO USE THEME COLOR ⭐️
        Log.d(TAG, "updateModeDisplay called, isHearMode: " + isHearMode);
        if (isHearMode) {
            hearModeLayout.setVisibility(View.VISIBLE);
            speakModeLayout.setVisibility(View.GONE);
            toggleHearFrom.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            toggleHearFrom.setCardElevation(8f);
            toggleSpeakTo.setCardBackgroundColor(colorSurface); // ⭐️ USE THEME COLOR
            toggleSpeakTo.setCardElevation(3f);
        } else {
            hearModeLayout.setVisibility(View.GONE);
            speakModeLayout.setVisibility(View.VISIBLE);
            toggleSpeakTo.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            toggleSpeakTo.setCardElevation(8f);
            toggleHearFrom.setCardBackgroundColor(colorSurface); // ⭐️ USE THEME COLOR
            toggleHearFrom.setCardElevation(3f);
        }
    }

    private void updateUITexts() {
        // (This method is unchanged from your original)
        Log.d(TAG, "updateUITexts called, isMalay: " + isMalay);
        if (isMalay) {
            if (tvMessageLabel != null) tvMessageLabel.setText("Masukkan mesej anda:");
            if (tvClearButton != null) tvClearButton.setText("PADAM");
            if (tvSpeakButton != null) tvSpeakButton.setText("CAKAP");
            if (tvTranscription != null && transcriptionHistory.isEmpty()) {
                tvTranscription.setText(isListening ? "Mendengar..." : "Mula bercakap....");
            }
            if (etMessage != null)
                etMessage.setHint("Taip mesej anda di sini... \n(Tekan lama untuk frasa biasa)");
        } else {
            if (tvMessageLabel != null) tvMessageLabel.setText("Enter your message:");
            if (tvClearButton != null) tvClearButton.setText("CLEAR");
            if (tvSpeakButton != null) tvSpeakButton.setText("SPEAK");
            if (tvTranscription != null && transcriptionHistory.isEmpty()) {
                tvTranscription.setText(isListening ? "Listening..." : "Start speaking....");
            }
            if (etMessage != null)
                etMessage.setHint("Type your message here... \n(Long press for common phrases)");
        }
    }

    private void initializeTextToSpeech() {
        // (This method is unchanged from your original)
        Log.d(TAG, "initializeTextToSpeech called");
        try {
            textToSpeech = new TextToSpeech(this, this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TextToSpeech", e);
            isTTSReady = false;
        }
    }

    @Override
    public void onInit(int status) {
        // ⭐️ UPDATED TO USE STRING RESOURCES FOR TOASTS ⭐️
        Log.d(TAG, "onInit called with status: " + status);
        try {
            if (textToSpeech == null) {
                Log.e(TAG, "TextToSpeech is null");
                return;
            }
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = isMalay ? new Locale("ms", "MY") : Locale.US;
                int result = textToSpeech.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        String message = isMalay ?
                                getString(R.string.toast_language_not_supported) :
                                getString(R.string.toast_language_not_supported);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        isTTSReady = false;
                    } else {
                        isTTSReady = true;
                        textToSpeech.setSpeechRate(1.0f);
                        textToSpeech.setPitch(1.0f);
                    }
                } else {
                    isTTSReady = true;
                    textToSpeech.setSpeechRate(1.0f);
                    textToSpeech.setPitch(1.0f);
                }
            } else {
                String message = isMalay ?
                        getString(R.string.toast_tts_init_failed) :
                        getString(R.string.toast_tts_init_failed);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                isTTSReady = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onInit", e);
            isTTSReady = false;
        }
    }

    private void speakText(String text) {
        // ⭐️ UPDATED TO USE STRING RESOURCES FOR TOASTS ⭐️
        Log.d(TAG, "speakText called: " + text);
        try {
            if (textToSpeech == null || !isTTSReady) {
                String message = isMalay ?
                        getString(R.string.toast_tts_not_ready) :
                        getString(R.string.toast_tts_not_ready);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                return;
            }
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error speaking text", e);
        }
    }

    private void checkPermissionAndStartSpeechRecognition() {
        // (This method is unchanged from your original)
        Log.d(TAG, "checkPermissionAndStartSpeechRecognition called");
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting microphone permission");
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            } else {
                Log.d(TAG, "Microphone permission already granted");
                startSpeechRecognition();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking permission", e);
        }
    }

    private void startSpeechRecognition() {
        // ⭐️ UPDATED TO USE THEME COLOR AND STRING RESOURCES ⭐️
        Log.d(TAG, "startSpeechRecognition called");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage);
        if (isMalay) {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Mula bercakap..."); // ⭐️ YOUR LOGIC
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start speaking..."); // ⭐️ YOUR LOGIC
        }
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try {
            isListening = true;
            updateMicrophoneButton();
            if (isMalay) {
                tvTranscription.setText("Mendengar..."); // ⭐️ YOUR LOGIC
            } else {
                tvTranscription.setText("Listening..."); // ⭐️ YOUR LOGIC
            }
            tvTranscription.setTextColor(colorHoloBlue); // ⭐️ USE THEME COLOR
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            String errorMessage = isMalay ?
                    getString(R.string.toast_speech_recognition_not_available) :
                    getString(R.string.toast_speech_recognition_not_available);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            isListening = false;
            updateMicrophoneButton();
        }
    }

    private void updateMicrophoneButton() {
        // (This method is unchanged from your original)
        try {
            if (btnMicrophone != null) {
                btnMicrophone.setAlpha(isListening ? 0.7f : 1.0f);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating microphone button", e);
        }
    }

    /**
     * Sets up the bottom navigation bar based on the user's role.
     * @param activePage A string ("home", "history", "admin", "profile") to highlight the current page.
     */
    private void setupBottomNavigation(String activePage) {
        // ⭐️ UPDATED TO USE STRING RESOURCES ⭐️
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
            navGuideAdminText.setText(getString(R.string.nav_admin)); // ⭐️ USE STRING
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
            navGuideAdminText.setText(getString(R.string.nav_guide));
            navGuideAdminIcon.setImageResource(android.R.drawable.ic_menu_help);

            navGuideAdmin.setOnClickListener(v -> {
                if (!"guide".equals(activePage)) {
                    Intent intent = new Intent(this, GuideActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                }
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
                // Save conversation before switching
                saveConversationToDatabase();
                Intent intent = new Intent(this, ChatHistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!"profile".equals(activePage)) {
                // Save conversation before switching
                saveConversationToDatabase();
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

    private void saveConversationToDatabase() {
        // (This method is unchanged from your original)
        if (isSaving) return;
        if (sessionManager == null) sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            return; // Don't show toast, just don't save if not logged in
        }
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            return; // Don't save if no user ID
        }
        String allHearText = (tvTranscription != null && tvTranscription.getText() != null)
                ? tvTranscription.getText().toString().trim()
                : "";
        String allSpeakText = temporarySpeakMessages.isEmpty() ? "" : String.join("\n", temporarySpeakMessages).trim();
        if (allHearText.isEmpty() && allSpeakText.isEmpty()) {
            return;
        }
        isSaving = true;
        ConversationApiService apiService = ApiClient.getClient().create(ConversationApiService.class);
        Call<ConversationResponseModel> call = apiService.saveConversation(userId, allHearText, allSpeakText);
        call.enqueue(new Callback<ConversationResponseModel>() {
            @Override
            public void onResponse(Call<ConversationResponseModel> call, Response<ConversationResponseModel> response) {
                isSaving = false;
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Conversation saved: " + response.body().getMessage());
                    temporarySpeakMessages.clear();
                    transcriptionHistory = "";
                    if (tvTranscription != null) tvTranscription.setText("");
                    updateUITexts();
                } else {
                    Log.e(TAG, "saveConversation server error. code=" + response.code());
                }
            }
            @Override
            public void onFailure(Call<ConversationResponseModel> call, Throwable t) {
                isSaving = false;
                Log.e(TAG, "saveConversation failure", t);
            }
        });
    }

    @Override
    protected void onPause() {
        // (This method is unchanged from your original)
        super.onPause();
        try {
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onPause", e);
        }
    }

    @Override
    protected void onDestroy() {
        // (This method is unchanged from your original)
        Log.d(TAG, "onDestroy called");
        saveConversationToDatabase(); // Save one last time
        try {
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
                textToSpeech = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
        super.onDestroy();
    }
}