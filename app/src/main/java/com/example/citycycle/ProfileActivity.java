package com.example.citycycle;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final String CURRENT_UTC_TIME = "2025-01-24 14:34:50";
    private static final String CURRENT_USER = "Chethukawagachchi";

    // UI Components
    private TextInputLayout usernameLayout, mobileLayout, addressLayout;
    private TextInputEditText editUsername, editMobile, editAddress, editEmail;
    private MaterialButton btnUpdate;
    private View progressOverlay;

    // Data
    private DB_Operations dbOperations;
    private User currentUser;
    private boolean isDataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_profile);
            initializeViews();
            setupToolbar();
            loadUserData();
            setupTextChangeListeners();
            setupUpdateButton();
            logActivityStart();
        } catch (Exception e) {
            logError("Error initializing activity", e);
            showError("Failed to initialize profile view");
            finish();
        }
    }

    private void initializeViews() {
        // Initialize TextInputLayouts
        usernameLayout = findViewById(R.id.usernameLayout);
        mobileLayout = findViewById(R.id.mobileLayout);
        addressLayout = findViewById(R.id.addressLayout);

        // Initialize EditTexts
        editUsername = findViewById(R.id.editUsername);
        editMobile = findViewById(R.id.editMobile);
        editAddress = findViewById(R.id.editAddress);
        editEmail = findViewById(R.id.editEmail);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressOverlay = findViewById(R.id.progressOverlay);

        dbOperations = new DB_Operations(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
    }

    private void setupTextChangeListeners() {
        editUsername.addTextChangedListener(createTextWatcher(usernameLayout));
        editMobile.addTextChangedListener(createTextWatcher(mobileLayout));
        editAddress.addTextChangedListener(createTextWatcher(addressLayout));
    }

    private TextWatcher createTextWatcher(final TextInputLayout textInputLayout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError(null);
                isDataChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateField(textInputLayout, s.toString());
            }
        };
    }

    private void validateField(TextInputLayout layout, String text) {
        if (layout == usernameLayout) {
            if (TextUtils.isEmpty(text)) {
                layout.setError("Username cannot be empty");
            } else if (text.length() < 3) {
                layout.setError("Username must be at least 3 characters");
            } else {
                layout.setError(null);
            }
        } else if (layout == mobileLayout) {
            if (TextUtils.isEmpty(text)) {
                layout.setError("Mobile number cannot be empty");
            } else if (text.length() < 10) {
                layout.setError("Enter a valid mobile number");
            } else {
                layout.setError(null);
            }
        } else if (layout == addressLayout) {
            if (TextUtils.isEmpty(text)) {
                layout.setError("Address cannot be empty");
            } else if (text.length() < 5) {
                layout.setError("Enter a valid address");
            } else {
                layout.setError(null);
            }
        }
    }

    private void setupUpdateButton() {
        btnUpdate.setOnClickListener(v -> {
            if (validateInputs()) {
                confirmUpdate();
            }
        });
    }

    private void loadUserData() {
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (TextUtils.isEmpty(userEmail)) {
            logError("No email provided", null);
            showError("Invalid user email");
            finish();
            return;
        }

        try {
            logOperation("Fetching user data", "email: " + userEmail);
            currentUser = dbOperations.getUserByEmail(userEmail);
            if (currentUser != null) {
                displayUserData();
            } else {
                logError("User not found", null);
                showError("No user details found");
                finish();
            }
        } catch (Exception e) {
            logError("Error loading user data", e);
            showError("Failed to load user data");
            finish();
        }
    }

    private void displayUserData() {
        editUsername.setText(currentUser.getUsername());
        editMobile.setText(String.valueOf(currentUser.getMobile()));
        editAddress.setText(currentUser.getAddress());
        editEmail.setText(currentUser.getEmail());
        editEmail.setEnabled(false);

        logOperation("User data displayed", "user: " + currentUser.getEmail());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Reset all errors
        usernameLayout.setError(null);
        mobileLayout.setError(null);
        addressLayout.setError(null);

        // Username validation
        String username = editUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else if (username.length() < 3) {
            usernameLayout.setError("Username must be at least 3 characters");
            isValid = false;
        }

        // Mobile validation
        String mobile = editMobile.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            mobileLayout.setError("Mobile number cannot be empty");
            isValid = false;
        } else if (mobile.length() < 10) {
            mobileLayout.setError("Enter a valid mobile number");
            isValid = false;
        }

        // Address validation
        String address = editAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            addressLayout.setError("Address cannot be empty");
            isValid = false;
        } else if (address.length() < 5) {
            addressLayout.setError("Enter a valid address");
            isValid = false;
        }

        return isValid;
    }

    private void confirmUpdate() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Update Profile")
                .setMessage("Are you sure you want to update your profile?")
                .setPositiveButton("Update", (dialog, which) -> updateUserDetails())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserDetails() {
        showProgress(true);
        try {
            currentUser.setUsername(editUsername.getText().toString().trim());
            currentUser.setMobile(Integer.parseInt(editMobile.getText().toString().trim()));
            currentUser.setAddress(editAddress.getText().toString().trim());

            boolean isUpdated = dbOperations.updateUsers(currentUser);
            if (isUpdated) {
                logOperation("Profile updated", "user: " + currentUser.getEmail());
                showSuccess("Profile updated successfully");
                isDataChanged = true;
                finish();
            } else {
                logError("Update failed", null);
                showError("Failed to update profile");
            }
        } catch (NumberFormatException e) {
            logError("Invalid mobile number format", e);
            showError("Please enter a valid mobile number");
        } catch (Exception e) {
            logError("Error updating profile", e);
            showError("An error occurred while updating profile");
        } finally {
            showProgress(false);
        }
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Logging methods
    private void logActivityStart() {
        Log.i(TAG, String.format("Activity started - User: %s, Time: %s",
                CURRENT_USER, CURRENT_UTC_TIME));
    }

    private void logOperation(String operation, String details) {
        Log.i(TAG, String.format("%s - %s - Time: %s - User: %s",
                operation, details, CURRENT_UTC_TIME, CURRENT_USER));
    }

    private void logError(String message, Exception e) {
        Log.e(TAG, String.format("Error: %s - Time: %s - User: %s",
                message, CURRENT_UTC_TIME, CURRENT_USER), e);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isDataChanged) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}