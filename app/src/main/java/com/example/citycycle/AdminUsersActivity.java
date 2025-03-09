package com.example.citycycle;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdminUsersActivity extends AppCompatActivity {
    private static final String TAG = "AdminUsersActivity";
    private static final String CURRENT_USER = "Chethukawagachchi";
    private static final String CURRENT_UTC_TIME = "2025-01-24 13:25:23";

    private RecyclerView usersRecyclerView;
    private TextInputEditText searchEditText;
    private DB_Operations dbOperations;
    private List<User> allUsers;
    private UserAdapter adapter;
    private MaterialButton searchButton;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_users);
            initializeViews();
            setupToolbar();
            setupRecyclerView();
            setupSearchFunctionality();
            loadUsers();
            logActivityStart();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error initializing admin users view", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        if (toolbar == null || usersRecyclerView == null ||
                searchEditText == null || searchButton == null) {
            throw new IllegalStateException("Required views not found in layout");
        }

        dbOperations = new DB_Operations(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registered Users");
        }

        toolbar.setNavigationOnClickListener(view -> {
            logNavigationEvent("back_pressed");
            onBackPressed();
        });
    }

    private void setupRecyclerView() {
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(new UserAdapter.UserClickListener() {
            @Override
            public void onUserClick(User user) {
                logUserInteraction("user_selected", user.getEmail());
                // Handle user selection
            }

            @Override
            public void onUserLongClick(User user) {
                logUserInteraction("user_long_pressed", user.getEmail());
                // Handle long press
            }
        });
        usersRecyclerView.setAdapter(adapter);
    }

    private void setupSearchFunctionality() {
        searchButton.setOnClickListener(v -> {
            logUserInteraction("search_button_clicked",
                    searchEditText.getText().toString());
            performSearch();
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    logUserInteraction("search_text_changed", s.toString());
                }
            }
        });
    }

    private void loadUsers() {
        try {
            logDatabaseOperation("fetching_all_users");
            allUsers = dbOperations.getAllUsers();
            if (allUsers != null && !allUsers.isEmpty()) {
                adapter.setUsers(allUsers);
                logDatabaseOperation("users_loaded_successfully",
                        String.valueOf(allUsers.size()));
            } else {
                logDatabaseOperation("no_users_found");
                showMessage("No users found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading users: ", e);
            logDatabaseOperation("error_loading_users", e.getMessage());
            showMessage("Error loading users");
        }
    }

    private void performSearch() {
        try {
            String searchTerm = searchEditText.getText().toString().toLowerCase().trim();
            logSearchOperation(searchTerm);

            if (TextUtils.isEmpty(searchTerm)) {
                adapter.setUsers(allUsers);
                return;
            }

            List<User> filteredUsers = new ArrayList<>();
            for (User user : allUsers) {
                if (user.getEmail().toLowerCase().contains(searchTerm)) {
                    filteredUsers.add(user);
                }
            }

            adapter.setUsers(filteredUsers);
            logSearchResults(searchTerm, filteredUsers.size());
        } catch (Exception e) {
            Log.e(TAG, "Error performing search: ", e);
            logSearchOperation("error", e.getMessage());
            showMessage("Error performing search");
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Logging methods
    private void logActivityStart() {
        Log.i(TAG, String.format("Activity started - User: %s, Time: %s",
                CURRENT_USER, getCurrentTimestamp()));
    }

    private void logNavigationEvent(String action) {
        Log.i(TAG, String.format("Navigation event - Action: %s, Time: %s",
                action, getCurrentTimestamp()));
    }

    private void logUserInteraction(String action, String details) {
        Log.i(TAG, String.format("User interaction - Action: %s, Details: %s, Time: %s",
                action, details, getCurrentTimestamp()));
    }

    private void logDatabaseOperation(String operation) {
        logDatabaseOperation(operation, "");
    }

    private void logDatabaseOperation(String operation, String details) {
        Log.i(TAG, String.format("Database operation - Operation: %s, Details: %s, Time: %s",
                operation, details, getCurrentTimestamp()));
    }

    private void logSearchOperation(String searchTerm) {
        logSearchOperation("search_performed", searchTerm);
    }

    private void logSearchOperation(String action, String details) {
        Log.i(TAG, String.format("Search operation - Action: %s, Details: %s, Time: %s",
                action, details, getCurrentTimestamp()));
    }

    private void logSearchResults(String searchTerm, int resultCount) {
        Log.i(TAG, String.format("Search results - Term: %s, Count: %d, Time: %s",
                searchTerm, resultCount, getCurrentTimestamp()));
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, String.format("Activity destroyed - User: %s, Time: %s",
                CURRENT_USER, getCurrentTimestamp()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu if needed
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            logNavigationEvent("up_button_pressed");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}