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

import com.google.android.material.floatingactionbutton.FloatingActionButton;  // Import FAB

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
    private static final String CURRENT_USER = "chethukdenuwan";
    private LocationsAdapter adapter;
    private static final int SELECT_LOCATIONS_REQUEST = 1001;  // Request code for the SelectLocationActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        initializeViews();
        setupToolbar();
        setupTimeDisplay();
        loadLocations();
        setupFloatingActionButton(); // Initialize the FAB

        // (Optional) Insert sample locations - consider doing this only once on app install
        // DB_Operations dbOperations = new DB_Operations(this);
        // dbOperations.insertSampleLocations();
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

    private void setupFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(LocationsActivity.this, SelectLocationActivity.class);
            startActivityForResult(intent, SELECT_LOCATIONS_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_LOCATIONS_REQUEST && resultCode == RESULT_OK && data != null) {

            int pickupLocationId = data.getIntExtra("pickup_location_id", -1);
            int destinationLocationId = data.getIntExtra("destination_location_id", -1);

            double distance = data.getDoubleExtra("distance", 0);
            double price = data.getDoubleExtra("price", 0);

            DB_Operations dbOperations = new DB_Operations(this);
            Location pickupLocation = dbOperations.getLocationById(pickupLocationId);
            Location destinationLocation = dbOperations.getLocationById(destinationLocationId);

            if (pickupLocation != null && destinationLocation != null) {
                processJourneyBooking(pickupLocation, destinationLocation, distance, price);
            } else {
                Toast.makeText(this, "Error: Could not retrieve location data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processJourneyBooking(Location pickup, Location destination, double distance, double price) {
        String currentTime = dateFormat.format(new Date());

        new AlertDialog.Builder(this)
                .setTitle("Confirm Journey Booking")
                .setMessage(String.format(Locale.US,
                        "Journey Details:\n\n" +
                                "From: %s\n" +
                                "To: %s\n" +
                                "Distance: %.2f km\n" +
                                "Price: Rs. %.0f\n" +
                                "Time: %s\n\n" +
                                "Available Bikes at Pickup: %d",
                        pickup.getName(),
                        destination.getName(),
                        distance,
                        price,
                        currentTime,
                        pickup.getAvailableBikes()))
                .setPositiveButton("Confirm", (dialog, which) -> {
                    completeJourneyBooking(pickup, destination, distance, price);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void completeJourneyBooking(Location pickup, Location destination, double distance, double price) {
        if (pickup.getAvailableBikes() > 0) {
            // Optimistic lock: Read number of bikes, reduce and write
            int currentBikes = pickup.getAvailableBikes();
            pickup.setAvailableBikes(currentBikes - 1);

            DB_Operations dbOperations = new DB_Operations(this);
            if (dbOperations.updateLocation(pickup)) {
                String currentTime = dateFormat.format(new Date());
                showJourneyConfirmation(pickup, destination, distance, price, currentTime);
                loadLocations(); // Refresh the list
            } else {
                Toast.makeText(this,
                        "Failed to book journey. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No available bikes at the pickup location.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showJourneyConfirmation(Location pickup, Location destination,
                                         double distance, double price, String bookingTime) {
        new AlertDialog.Builder(this)
                .setTitle("Journey Booking Successful!")
                .setMessage(String.format(Locale.US,
                        "Booking Details:\n\n" +
                                "From: %s\n" +
                                "To: %s\n" +
                                "Distance: %.2f km\n" +
                                "Price: Rs. %.0f\n" +
                                "Booking Time: %s\n\n" +
                                "Please pick up your bike within 30 minutes.",
                        pickup.getName(),
                        destination.getName(),
                        distance,
                        price,
                        bookingTime))
                .setPositiveButton("OK", null)
                .show();
    }
}