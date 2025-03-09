package com.example.citycycle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ManageLocationsActivity extends AppCompatActivity {

    private TextView timeDisplayText;
    private RecyclerView locationsRecyclerView;
    private TextView emptyView;
    private FloatingActionButton fabAddLocation;
    private Handler timeHandler;
    private SimpleDateFormat dateFormat;
    private static final String CURRENT_USER = "Chethukawagachchi";
    private DB_Operations dbOperations;
    private ManageLocationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_locations);

        dbOperations = new DB_Operations(this);
        initializeViews();
        setupToolbar();
        setupTimeDisplay();
        loadLocations();
        setupAddButton();
    }

    private void initializeViews() {
        timeDisplayText = findViewById(R.id.timeDisplayText);
        locationsRecyclerView = findViewById(R.id.locationsRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        fabAddLocation = findViewById(R.id.fabAddLocation);

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
            getSupportActionBar().setTitle("Manage Locations");
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
                "Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): %s\n" +
                        "Current User's Login: %s",
                currentTime, CURRENT_USER);
        timeDisplayText.setText(displayText);
    }

    private void loadLocations() {
        ArrayList<Location> locations = dbOperations.getAllLocations();

        if (locations.isEmpty()) {
            locationsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            locationsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter = new ManageLocationsAdapter(locations,
                    new ManageLocationsAdapter.LocationActionListener() {
                        @Override
                        public void onEditClick(Location location) {
                            showEditLocationDialog(location);
                        }

                        @Override
                        public void onDeleteClick(Location location) {
                            showDeleteConfirmationDialog(location);
                        }
                    });
            locationsRecyclerView.setAdapter(adapter);
        }
    }

    private void setupAddButton() {
        fabAddLocation.setOnClickListener(v -> showAddLocationDialog());
    }

    private void showAddLocationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_location, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Add New Location");

        EditText nameInput = dialogView.findViewById(R.id.locationNameInput);
        EditText addressInput = dialogView.findViewById(R.id.locationAddressInput);
        EditText latitudeInput = dialogView.findViewById(R.id.locationLatitudeInput);
        EditText longitudeInput = dialogView.findViewById(R.id.locationLongitudeInput);
        EditText descriptionInput = dialogView.findViewById(R.id.locationDescriptionInput);
        EditText bikesInput = dialogView.findViewById(R.id.availableBikesInput);

        builder.setPositiveButton("Add", null); // Set to null - we'll override this below
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to prevent automatic dismissal
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String latitudeStr = latitudeInput.getText().toString().trim();
            String longitudeStr = longitudeInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String bikesStr = bikesInput.getText().toString().trim();

            if (validateInputs(name, address, latitudeStr, longitudeStr, description, bikesStr)) {
                try {
                    double latitude = Double.parseDouble(latitudeStr);
                    double longitude = Double.parseDouble(longitudeStr);
                    int bikes = Integer.parseInt(bikesStr);

                    Location newLocation = new Location(0, name, address, latitude,
                            longitude, description, bikes);

                    dbOperations.addLocation(newLocation);
                    loadLocations(); // Refresh the list
                    dialog.dismiss();
                    Toast.makeText(this, "Location added successfully",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error adding location: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showEditLocationDialog(Location location) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_location, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Edit Location");

        EditText nameInput = dialogView.findViewById(R.id.locationNameInput);
        EditText addressInput = dialogView.findViewById(R.id.locationAddressInput);
        EditText latitudeInput = dialogView.findViewById(R.id.locationLatitudeInput);
        EditText longitudeInput = dialogView.findViewById(R.id.locationLongitudeInput);
        EditText descriptionInput = dialogView.findViewById(R.id.locationDescriptionInput);
        EditText bikesInput = dialogView.findViewById(R.id.availableBikesInput);

        // Pre-fill existing data
        nameInput.setText(location.getName());
        addressInput.setText(location.getAddress());
        latitudeInput.setText(String.valueOf(location.getLatitude()));
        longitudeInput.setText(String.valueOf(location.getLongitude()));
        descriptionInput.setText(location.getDescription());
        bikesInput.setText(String.valueOf(location.getAvailableBikes()));

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String latitudeStr = latitudeInput.getText().toString().trim();
            String longitudeStr = longitudeInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String bikesStr = bikesInput.getText().toString().trim();

            if (validateInputs(name, address, latitudeStr, longitudeStr, description, bikesStr)) {
                try {
                    double latitude = Double.parseDouble(latitudeStr);
                    double longitude = Double.parseDouble(longitudeStr);
                    int bikes = Integer.parseInt(bikesStr);

                    Location updatedLocation = new Location(location.getId(), name,
                            address, latitude, longitude, description, bikes);

                    if (dbOperations.updateLocation(updatedLocation)) {
                        loadLocations(); // Refresh the list
                        dialog.dismiss();
                        Toast.makeText(this, "Location updated successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error updating location",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error updating location: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteConfirmationDialog(Location location) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Location")
                .setMessage("Are you sure you want to delete this location?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbOperations.deleteLocation(location.getId())) {
                        loadLocations(); // Refresh the list
                        Toast.makeText(this, "Location deleted successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error deleting location",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validateInputs(String name, String address, String latitude,
                                   String longitude, String description, String bikes) {
        if (name.isEmpty() || address.isEmpty() || latitude.isEmpty() ||
                longitude.isEmpty() || description.isEmpty() || bikes.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(latitude);
            Double.parseDouble(longitude);
            Integer.parseInt(bikes);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
}