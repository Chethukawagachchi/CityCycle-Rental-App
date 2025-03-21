package com.example.citycycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class SelectLocationActivity extends AppCompatActivity {
    private MapView mapView;
    private TextView pickupLocationText;
    private TextView destinationLocationText;
    private Button confirmButton;
    private Button switchModeButton;
    private Location pickupLocation;
    private Location destinationLocation;
    private ArrayList<Location> locations;
    private MyLocationNewOverlay myLocationOverlay;
    private boolean isSelectingDestination = false;
    private GeoPoint selectedCustomLocation; // For custom location
    private Marker customMarker; // To show custom selected point

    // Add these variables to your class
    private TextView distanceText;
    private TextView priceText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_select_location);

        setupToolbar();
        initializeViews();
        setupMap();
        loadLocations();
    }

    // Add this method to calculate price based on distance
    private double calculatePrice(Location pickup, Location destination) {
        // Calculate distance between pickup and destination
        double distance = calculateDistance(
                pickup.getLatitude(), pickup.getLongitude(),
                destination.getLatitude(), destination.getLongitude()
        );

        // Convert distance to kilometers
        double distanceKm = distance / 1000.0;

        // Base price Rs.50
        double basePrice = 50.0;

        // Price per kilometer Rs.40
        double pricePerKm = 40.0;

        // Calculate total price
        double totalPrice = basePrice + (distanceKm * pricePerKm);

        // Round to nearest 10 rupees
        return Math.ceil(totalPrice / 10.0) * 10;
    }


    // Add this method to your SelectLocationActivity class
    private void loadLocations() {
        DB_Operations dbOperations = new DB_Operations(this);
        locations = dbOperations.getAllLocations();
        if (locations != null && !locations.isEmpty()) {
            addLocationMarkers();
        } else {
            Toast.makeText(this, "No rental locations available", Toast.LENGTH_SHORT).show();
        }
    }

    // Also add this method if not already present
    private void addLocationMarkers() {
        if (locations != null) {
            for (Location location : locations) {
                addMarkerForLocation(location);
            }
        }
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0); // Increased zoom level

        // Set initial position to Colombo
        GeoPoint colombo = new GeoPoint(6.9271, 79.8612);
        mapView.getController().setCenter(colombo);

        setupMyLocation();
        setupMapClickListener();
    }

    private void setupMapClickListener() {
        mapView.getOverlays().add(new org.osmdroid.views.overlay.Overlay(this) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());

                if (isSelectingDestination) {
                    handleDestinationSelection(loc);
                } else {
                    handlePickupSelection(loc);
                }
                return true;
            }
        });
    }

    private void handlePickupSelection(GeoPoint loc) {
        Location nearestLocation = findNearestLocation(loc);
        if (nearestLocation != null && nearestLocation.getAvailableBikes() > 0) {
            pickupLocation = nearestLocation;
            updatePickupLocationUI();
        } else {
            // Create custom location for pickup
            pickupLocation = createCustomLocation(loc, "Custom Pickup Point");
            updatePickupLocationUI();
            Toast.makeText(this, "Custom pickup location selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDestinationSelection(GeoPoint loc) {
        Location nearestLocation = findNearestLocation(loc);
        if (nearestLocation != null) {
            destinationLocation = nearestLocation;
        } else {
            destinationLocation = createCustomLocation(loc, "Custom Destination Point");
        }
        updateDestinationLocationUI();
        Toast.makeText(this, "Destination location selected", Toast.LENGTH_SHORT).show();
    }

    private Location findNearestLocation(GeoPoint tap) {
        if (locations == null || locations.isEmpty()) return null;

        Location nearest = null;
        double minDistance = 500; // 500 meters radius

        for (Location loc : locations) {
            double distance = calculateDistance(
                    tap.getLatitude(), tap.getLongitude(),
                    loc.getLatitude(), loc.getLongitude()
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearest = loc;
            }
        }
        return nearest;
    }

    private Location createCustomLocation(GeoPoint point, String name) {
        return new Location(
                -1,  // id (custom location)
                name, // name
                "Custom Selected Location", // address
                point.getLatitude(),  // latitude
                point.getLongitude(), // longitude
                "Custom location selected by user", // description (instead of availability)
                1  // availableBikes (1 for pickup points)
        );
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void updatePickupLocationUI() {
        if (pickupLocation != null) {
            String locationInfo;
            if (pickupLocation.getId() == -1) {
                // Custom location
                locationInfo = String.format("Custom Pickup Location\nLatitude: %.4f\nLongitude: %.4f",
                        pickupLocation.getLatitude(),
                        pickupLocation.getLongitude());
            } else {
                locationInfo = String.format("Pickup: %s\nAvailable Bikes: %d",
                        pickupLocation.getName(),
                        pickupLocation.getAvailableBikes());
            }
            pickupLocationText.setText(locationInfo);
            updateConfirmButton();
            animateToLocation(pickupLocation);
            updateCustomMarker(pickupLocation, "Pickup");
        }
    }

    private void updateDestinationLocationUI() {
        if (destinationLocation != null) {
            String locationInfo;
            if (destinationLocation.getId() == -1) {
                // Custom location
                locationInfo = String.format("Custom Destination\nLatitude: %.4f\nLongitude: %.4f",
                        destinationLocation.getLatitude(),
                        destinationLocation.getLongitude());
            } else {
                locationInfo = String.format("Destination: %s",
                        destinationLocation.getName());
            }
            destinationLocationText.setText(locationInfo);
            updateConfirmButton();
            animateToLocation(destinationLocation);
            updateCustomMarker(destinationLocation, "Destination");
        }
    }

    private void animateToLocation(Location location) {
        mapView.getController().animateTo(new GeoPoint(
                location.getLatitude(),
                location.getLongitude()
        ));
    }

    private void updateCustomMarker(Location location, String type) {
        if (customMarker != null) {
            mapView.getOverlays().remove(customMarker);
        }

        customMarker = new Marker(mapView);
        customMarker.setPosition(new GeoPoint(location.getLatitude(), location.getLongitude()));
        customMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        customMarker.setTitle(type + " Location");

        int markerColor = type.equals("Pickup") ? R.drawable.marker_green : R.drawable.marker_blue;
        customMarker.setIcon(getResources().getDrawable(markerColor, getTheme()));

        mapView.getOverlays().add(customMarker);
        mapView.invalidate();
    }

    private void updateConfirmButton() {
        boolean hasValidPickup = pickupLocation != null; //Custom location can be null
        boolean hasValidDestination = destinationLocation != null;
        confirmButton.setEnabled(hasValidPickup && hasValidDestination);


        // Update price and distance if both locations are selected
        if (hasValidPickup && hasValidDestination) {
            double distance = calculateDistance(
                    pickupLocation.getLatitude(), pickupLocation.getLongitude(),
                    destinationLocation.getLatitude(), destinationLocation.getLongitude()
            ) / 1000.0; // Convert to km

            double price = calculatePrice(pickupLocation, destinationLocation);

            distanceText.setText(String.format("%.2f km", distance));
            priceText.setText(String.format("Rs. %.0f", price));
        } else {
            distanceText.setText("");
            priceText.setText("");
        }
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Use onBackPressed()
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView != null) {
            mapView.onResume();
        }
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView != null) {
            mapView.onPause();
        }
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }

    private void setupMyLocation() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }


    private void initializeViews() {
        mapView = findViewById(R.id.map);
        pickupLocationText = findViewById(R.id.pickupLocationText);
        destinationLocationText = findViewById(R.id.destinationLocationText);
        confirmButton = findViewById(R.id.confirmButton);
        switchModeButton = findViewById(R.id.switchModeButton);

        confirmButton.setEnabled(false);
        confirmButton.setOnClickListener(v -> confirmLocationSelection());

        switchModeButton = findViewById(R.id.switchModeButton);
        switchModeButton.setOnClickListener(v -> toggleSelectionMode());
        updateSwitchButtonText();

        // ... existing code ...
        distanceText = findViewById(R.id.distanceText);
        priceText = findViewById(R.id.priceText);
    }

    private void toggleSelectionMode() {
        isSelectingDestination = !isSelectingDestination;
        updateToolbarTitle();
        updateSwitchButtonText();
        Toast.makeText(this, isSelectingDestination ?
                        "Now selecting destination location" : "Now selecting pickup location",
                Toast.LENGTH_SHORT).show();
    }

    private void updateToolbarTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isSelectingDestination ?
                    "Select Destination Location" : "Select Pickup Location");
        }
    }

    private void updateSwitchButtonText() {
        switchModeButton.setText(isSelectingDestination ?
                "Switch to Pickup Selection" : "Switch to Destination Selection");
    }


    private void addMarkerForLocation(Location location) {
        Marker marker = new Marker(mapView);
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(location.getName());
        marker.setSnippet("Available Bikes: " + location.getAvailableBikes());

        int markerIcon = location.getAvailableBikes() > 0 ?
                R.drawable.marker_green : R.drawable.marker_red;
        marker.setIcon(getResources().getDrawable(markerIcon, getTheme()));

        marker.setOnMarkerClickListener((marker1, mapView) -> {
            if (isSelectingDestination) {
                if (pickupLocation != null && pickupLocation.getId() == location.getId()) {
                    Toast.makeText(this,
                            "Cannot select same location for pickup and destination",
                            Toast.LENGTH_SHORT).show();
                } else {
                    destinationLocation = location;
                    updateDestinationLocationUI();
                }
            } else {
                if (location.getAvailableBikes() > 0) {
                    if (destinationLocation != null &&
                            destinationLocation.getId() == location.getId()) {
                        Toast.makeText(this,
                                "Cannot select same location for pickup and destination",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        pickupLocation = location;
                        updatePickupLocationUI();
                    }
                } else {
                    Toast.makeText(this,
                            "No bikes available at this location for pickup",
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        mapView.getOverlays().add(marker);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Location");
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void confirmLocationSelection() {
        if (pickupLocation != null && destinationLocation != null) {
            // Check if we can access the data
            if (!isDataAccessible()) {
                showErrorDialog("Network Error",
                        "Could not retrieve data from the cloud. Please check your internet connection and try again.");
                return;
            }

            try {
                // Calculate price and distance
                double price = calculatePrice(pickupLocation, destinationLocation);
                double distance = calculateDistance(
                        pickupLocation.getLatitude(), pickupLocation.getLongitude(),
                        destinationLocation.getLatitude(), destinationLocation.getLongitude()
                ) / 1000.0;

                // First confirmation dialog
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Journey Details")
                        .setMessage(String.format(
                                "Please verify your journey details:\n\n" +
                                        "From: %s\n" +
                                        "To: %s\n" +
                                        "Distance: %.2f km\n" +
                                        "Price: Rs. %.0f\n\n" +
                                        "Would you like to proceed with this booking?",
                                pickupLocation.getName(),
                                destinationLocation.getName(),
                                distance,
                                price
                        ))
                        .setPositiveButton("Yes, Proceed", (dialog, which) -> {
                            // Verify data again before second confirmation
                            if (!isDataAccessible()) {
                                showErrorDialog("Network Error",
                                        "Could not retrieve data from the cloud. Please try again.");
                                return;
                            }

                            // Second confirmation dialog
                            showFinalConfirmationDialog(price, distance);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } catch (Exception e) {
                showErrorDialog("Error",
                        "An error occurred while processing your request. Please try again.");
            }
        }
    }

    private boolean isDataAccessible() {
        try {
            // Check if DB is accessible
            DB_Operations dbOperations = new DB_Operations(this);
            return dbOperations.isConnected(); // You'll need to add this method to DB_Operations
        } catch (Exception e) {
            return false;
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showFinalConfirmationDialog(double price, double distance) {
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage(String.format(
                        "By confirming, you agree to:\n\n" +
                                "• Pay Rs. %.0f upon completion\n" +
                                "• Pick up the bike within 30 minutes\n" +
                                "• Return the bike at the selected destination\n\n" +
                                "Do you confirm this booking?",
                        price
                ))
                .setPositiveButton("Confirm Booking", (dialog2, which2) -> {
                    // Final data verification
                    if (!isDataAccessible()) {
                        showErrorDialog("Network Error",
                                "Could not complete booking. Please try again.");
                        return;
                    }
                    processConfirmedBooking(price, distance);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processConfirmedBooking(double price, double distance) {
        try {
            // Show success message
            Toast.makeText(this, "Journey Booking Confirmed!", Toast.LENGTH_LONG).show();

            Intent resultIntent = new Intent();

            // Pickup Location Data
            resultIntent.putExtra("pickup_location_id", pickupLocation.getId());
            resultIntent.putExtra("pickup_lat", pickupLocation.getLatitude());
            resultIntent.putExtra("pickup_lng", pickupLocation.getLongitude());

            // Destination Location Data
            resultIntent.putExtra("destination_location_id", destinationLocation.getId());
            resultIntent.putExtra("destination_lat", destinationLocation.getLatitude());
            resultIntent.putExtra("destination_lng", destinationLocation.getLongitude());

            // Add price and distance
            resultIntent.putExtra("distance", distance);
            resultIntent.putExtra("price", price);

            // Update time to match the current time
            resultIntent.putExtra("current_time", "2025-03-21 12:56:07");
            resultIntent.putExtra("user_email", "chethukdenuwan");

            setResult(RESULT_OK, resultIntent);

            // Show final success message
            showSuccessDialog(price);

        } catch (Exception e) {
            showErrorDialog("Error",
                    "An error occurred while completing your booking. Please try again.");
        }
    }

    private void showSuccessDialog(double price) {
        new AlertDialog.Builder(this)
                .setTitle("Booking Success!")
                .setMessage(String.format(
                        "Your journey has been booked successfully!\n\n" +
                                "From: %s\n" +
                                "To: %s\n" +
                                "Total Price: Rs. %.0f\n\n" +
                                "Please pick up your bike within 30 minutes.",
                        pickupLocation.getName(),
                        destinationLocation.getName(),
                        price
                ))
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}