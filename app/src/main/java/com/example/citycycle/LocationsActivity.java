package com.example.citycycle;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocationsActivity extends AppCompatActivity {

    private TextView timeDisplayText;
    private RecyclerView locationsRecyclerView;
    private TextView emptyView;
    private Handler timeHandler;
    private SimpleDateFormat dateFormat;
    private static final String CURRENT_USER = "chethukdenuwan"; // Updated to match current user
    private LocationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        initializeViews();
        setupToolbar();
        setupTimeDisplay();
        loadLocations();

        // Insert sample locations (only needed once, you might want to conditionally run this)
        DB_Operations dbOperations = new DB_Operations(this);
        dbOperations.insertSampleLocations();
    }

    private void initializeViews() {
        timeDisplayText = findViewById(R.id.timeDisplayText);
        locationsRecyclerView = findViewById(R.id.locationsRecyclerView);
        emptyView = findViewById(R.id.emptyView);

        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        timeHandler = new Handler(Looper.getMainLooper());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Rental Locations");
        }
    }

    private void setupTimeDisplay() {
        updateTimeDisplay();
        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                timeHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateTimeDisplay() {
        String currentTime = dateFormat.format(new Date());
        String displayText = String.format(Locale.US,
                "Current Date and Time (UTC - YYYY-MM-DD HH:mm:ss formatted): %s\n" +
                        "Current User's Login: %s",
                currentTime, CURRENT_USER);
        timeDisplayText.setText(displayText);
    }

    private void loadLocations() {
        DB_Operations dbOperations = new DB_Operations(this);
        ArrayList<Location> locations = dbOperations.getAllLocations();

        if (locations.isEmpty()) {
            locationsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            locationsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            // Updated adapter initialization to pass the context
            adapter = new LocationsAdapter(this, locations);
            locationsRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null) {
            timeHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            int locationId = data.getIntExtra("selected_location_id", -1);
            if (locationId != -1) {
                DB_Operations dbOperations = new DB_Operations(this);
                Location selectedLocation = dbOperations.getLocationById(locationId);
                if (selectedLocation != null) {
                    processBooking(selectedLocation);
                }
            }
        }
    }

    private void processBooking(Location location) {
        String currentTime = dateFormat.format(new Date());

        new AlertDialog.Builder(this)
                .setTitle("Confirm Booking")
                .setMessage(String.format(Locale.US,
                        "Book a bike from:\n\n" +
                                "Location: %s\n" +
                                "Address: %s\n" +
                                "Available Bikes: %d\n" +
                                "Booking Time: %s",
                        location.getName(),
                        location.getAddress(),
                        location.getAvailableBikes(),
                        currentTime))
                .setPositiveButton("Confirm", (dialog, which) -> {
                    completeBooking(location);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void completeBooking(Location location) {
        if (location.getAvailableBikes() > 0) {
            location.setAvailableBikes(location.getAvailableBikes() - 1);

            DB_Operations dbOperations = new DB_Operations(this);
            if (dbOperations.updateLocation(location)) {
                String currentTime = dateFormat.format(new Date());
                showBookingConfirmation(location, currentTime);
                loadLocations(); // Refresh the list
            } else {
                Toast.makeText(this,
                        "Failed to book bike. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showBookingConfirmation(Location location, String bookingTime) {
        new AlertDialog.Builder(this)
                .setTitle("Booking Successful!")
                .setMessage(String.format(Locale.US,
                        "Booking Details:\n\n" +
                                "Location: %s\n" +
                                "Address: %s\n" +
                                "Booking Time: %s\n\n" +
                                "Please pick up your bike within 30 minutes.",
                        location.getName(),
                        location.getAddress(),
                        bookingTime))
                .setPositiveButton("OK", null)
                .show();
    }
}