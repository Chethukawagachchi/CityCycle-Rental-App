package com.example.citycycle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public class PreferencesActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private Switch darkModeSwitch;
    private Switch notificationSwitch;
    private Switch emailUpdatesSwitch;
    private Switch locationServicesSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Initialize SharedPreferences
        preferences = getSharedPreferences("LaxVistaPrefs", MODE_PRIVATE);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.preferences_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Preferences");
        }

        // Initialize UI elements
        setupSwitches();
        loadPreferences();
    }

    private void setupSwitches() {
        darkModeSwitch = findViewById(R.id.switch_dark_mode);
        notificationSwitch = findViewById(R.id.switch_notifications);
        emailUpdatesSwitch = findViewById(R.id.switch_email_updates);
        locationServicesSwitch = findViewById(R.id.switch_location_services);

        // Set up listeners
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("dark_mode", isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Toast.makeText(this, "Dark mode will be applied when you restart the app",
                    Toast.LENGTH_SHORT).show();
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("notifications", isChecked);
            String message = isChecked ? "Notifications enabled" : "Notifications disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        emailUpdatesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("email_updates", isChecked);
            String message = isChecked ? "Email updates enabled" : "Email updates disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        locationServicesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("location_services", isChecked);
            String message = isChecked ? "Location services enabled" : "Location services disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPreferences() {
        darkModeSwitch.setChecked(preferences.getBoolean("dark_mode", false));
        notificationSwitch.setChecked(preferences.getBoolean("notifications", true));
        emailUpdatesSwitch.setChecked(preferences.getBoolean("email_updates", true));
        locationServicesSwitch.setChecked(preferences.getBoolean("location_services", false));
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}