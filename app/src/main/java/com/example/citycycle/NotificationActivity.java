package com.example.citycycle;


import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.SharedPreferences;

public class NotificationActivity extends AppCompatActivity {
    private NotificationHelper notificationHelper;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "NotificationPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialize
        notificationHelper = new NotificationHelper(this);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupToolbar();
        setupNotificationSwitches();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupNotificationSwitches() {
        Switch bookingSwitch = findViewById(R.id.switch_booking_notifications);
        Switch promotionalSwitch = findViewById(R.id.switch_promotional_notifications);
        Switch serviceSwitch = findViewById(R.id.switch_service_notifications);

        // Load saved preferences
        bookingSwitch.setChecked(preferences.getBoolean("booking_notifications", true));
        promotionalSwitch.setChecked(preferences.getBoolean("promotional_notifications", true));
        serviceSwitch.setChecked(preferences.getBoolean("service_notifications", true));

        // Set listeners
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            String prefKey = "";

            int id = buttonView.getId();
            if (id == R.id.switch_booking_notifications) {
                prefKey = "booking_notifications";
            } else if (id == R.id.switch_promotional_notifications) {
                prefKey = "promotional_notifications";
            } else if (id == R.id.switch_service_notifications) {
                prefKey = "service_notifications";
            }

            editor.putBoolean(prefKey, isChecked);
            editor.apply();

            // Show confirmation
            if (isChecked) {
                notificationHelper.sendServiceNotification(
                        "Notifications Enabled",
                        buttonView.getText() + " have been enabled"
                );
            }
        };

        bookingSwitch.setOnCheckedChangeListener(listener);
        promotionalSwitch.setOnCheckedChangeListener(listener);
        serviceSwitch.setOnCheckedChangeListener(listener);
    }
}