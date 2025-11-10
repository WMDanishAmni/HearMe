// file: GuideActivity.java
package com.example.hearme.activities.guide; // ⭐️ CHANGED PACKAGE

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// ⭐️ CHANGED TO BASEACTIVITY
import com.example.hearme.activities.BaseActivity;
import com.example.hearme.R;
import com.example.hearme.activities.MainActivity;
import com.example.hearme.activities.admin.AdminDashboardActivity;
import com.example.hearme.activities.history.ChatHistoryActivity;
import com.example.hearme.activities.profile.ProfileActivity;
import com.example.hearme.models.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class GuideActivity extends BaseActivity { // ⭐️ CHANGED

    private static final String TAG = "GuideActivity";

    // --- PDF Renderer Objects ---
    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private int currentPageIndex = 0;

    // --- UI Views ---
    private ImageView pdfImageView;
    private Button btnEmergency, btnCommunication, btnAudio;
    private Button btnPrevious, btnNext;
    private TextView tvPageCount;

    // --- ⭐️ ADDED ⭐️ ---
    private SessionManager sessionManager;

    // --- ⭐️ Theme-aware colors ⭐️ ---
    private int colorSelected;
    private int colorUnselected;
    private int textSelected;
    private int textUnselected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        // ⭐️ REMOVED AppCompatDelegate line

        sessionManager = new SessionManager(this); // ⭐️ ADDED

        // Initialize Views
        pdfImageView = findViewById(R.id.pdf_image_view);
        btnEmergency = findViewById(R.id.btn_guide_emergency);
        btnCommunication = findViewById(R.id.btn_guide_communication);
        btnAudio = findViewById(R.id.btn_guide_audio);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        tvPageCount = findViewById(R.id.tv_page_count);

        loadThemeColors(); // ⭐️ ADDED
        setupHeader();
        setupBottomNavigation("guide"); // ⭐️ CHANGED to new method

        // --- Set Button Listeners ---
        btnEmergency.setOnClickListener(v -> loadEmergencyGuide());
        btnCommunication.setOnClickListener(v -> loadCommunicationGuide());
        btnAudio.setOnClickListener(v -> loadAudioGuide());

        btnPrevious.setOnClickListener(v -> showPage(currentPageIndex - 1));
        btnNext.setOnClickListener(v -> showPage(currentPageIndex + 1));

        // Load default guide (Emergency)
        loadCommunicationGuide();
    }

    // ⭐️ --- NEW METHOD --- ⭐️
    private void loadThemeColors() {
        TypedValue typedValue = new TypedValue();

        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        colorSelected = typedValue.data;

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        textSelected = typedValue.data;

        getTheme().resolveAttribute(R.attr.toolbarColor, typedValue, true); // Using toolbarColor for unselected
        colorUnselected = typedValue.data;

        getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
        textUnselected = typedValue.data;
    }
    // ⭐️ --- END NEW METHOD --- ⭐️

    // --- Methods to load PDFs and update button styles ---

    private void loadEmergencyGuide() {
        updateButtonStyles(btnEmergency, btnCommunication, btnAudio);
        openPdf("guide_emergency.pdf");
    }

    private void loadCommunicationGuide() {
        updateButtonStyles(btnCommunication, btnEmergency, btnAudio);
        openPdf("guide_communication.pdf");
    }

    private void loadAudioGuide() {
        updateButtonStyles(btnAudio, btnEmergency, btnCommunication);
        openPdf("guide_audio.pdf");
    }

    // --- This is the main PDF rendering logic ---

    private void openPdf(String pdfName) {
        // First, close any existing PDF
        closePdfRenderer();

        try {
            // Find the resource ID from the name
            int resourceId = 0;
            if (pdfName.equals("guide_communication.pdf")) {
                resourceId = R.raw.guide_communication;
            } else if (pdfName.equals("guide_emergency.pdf")) {
                resourceId = R.raw.guide_emergency;
            } else if (pdfName.equals("guide_audio.pdf")) {
                resourceId = R.raw.guide_audio;
            }

            if (resourceId == 0) {
                // ⭐️ USE STRING ⭐️
                Toast.makeText(this, getString(R.string.toast_pdf_not_found), Toast.LENGTH_SHORT).show();
                return;
            }

            // We need to copy the file from res/raw to a place the PdfRenderer can read
            InputStream is = getResources().openRawResource(resourceId);
            File file = new File(getCacheDir(), pdfName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            is.close();

            // Open the file as a ParcelFileDescriptor
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            // Create the PdfRenderer
            pdfRenderer = new PdfRenderer(fileDescriptor);

            // Show the first page (index 0)
            currentPageIndex = 0;
            showPage(currentPageIndex);

        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF", e);
            // ⭐️ USE STRING ⭐️
            Toast.makeText(this, getString(R.string.toast_pdf_error, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPage(int index) {
        if (pdfRenderer == null) return;

        // Check if index is valid
        if (index < 0 || index >= pdfRenderer.getPageCount()) {
            return; // Out of bounds
        }

        // Close the old page
        if (currentPage != null) {
            currentPage.close();
        }

        // Open the new page
        currentPage = pdfRenderer.openPage(index);
        currentPageIndex = index;

        // Create a bitmap to render the page onto
        // ⭐️ Use window width for bitmap size to fit screen
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (width * (float)currentPage.getHeight() / (float)currentPage.getWidth());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Render the page
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // Set the bitmap to the ImageView
        pdfImageView.setImageBitmap(bitmap);

        // Update UI
        updateNavButtons();
    }

    // file: GuideActivity.java

    // file: GuideActivity.java

    private void updateNavButtons() {
        if (pdfRenderer == null) return;

        int pageCount = pdfRenderer.getPageCount();
        tvPageCount.setText(getString(R.string.guide_page_count, (currentPageIndex + 1), pageCount));

        // Enable/disable buttons
        btnPrevious.setEnabled(currentPageIndex > 0);
        btnNext.setEnabled(currentPageIndex + 1 < pageCount);

        // ⭐️ The setAlpha() lines are no longer needed ⭐️
    }

    private void closePdfRenderer() {
        try {
            if (currentPage != null) {
                currentPage.close();
                currentPage = null;
            }
            if (pdfRenderer != null) {
                pdfRenderer.close();
                pdfRenderer = null;
            }
            if (fileDescriptor != null) {
                fileDescriptor.close();
                fileDescriptor = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing PDF renderer", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePdfRenderer(); // Clean up the PDF
    }

    // --- End of new PDF logic ---


    private void updateButtonStyles(Button active, Button inactive1, Button inactive2) {
        // ⭐️ This method now uses the theme-aware colors
        active.setBackgroundTintList(ColorStateList.valueOf(colorSelected));
        active.setTextColor(textSelected);

        inactive1.setBackgroundTintList(ColorStateList.valueOf(colorUnselected));
        inactive1.setTextColor(textUnselected);

        inactive2.setBackgroundTintList(ColorStateList.valueOf(colorUnselected));
        inactive2.setTextColor(textUnselected);
    }

    // --- Standard Navigation Methods ---

    private void setupHeader() {
        View header = findViewById(R.id.header_layout);
        if (header != null) {
            View btnBack = header.findViewById(R.id.btn_back_header);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        }
    }

    // ⭐️ --- REPLACED ENTIRE METHOD --- ⭐️
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
            // This page isn't for admins, but we handle it just in case
            navGuideAdminText.setText(getString(R.string.nav_admin));
            navGuideAdminIcon.setImageResource(R.drawable.ic_admin);

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
                // Already on this page
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
    }
}