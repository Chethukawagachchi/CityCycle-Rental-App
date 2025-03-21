package com.example.citycycle;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class SelectLocationActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView selectedLocationText;
    private Button confirmButton;
    private Location selectedLocation;
    private ArrayList<Location> locations;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMdroid
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_select_location);

        setupToolbar();
        initializeViews();
        loadLocations();
        setupMap();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Rental Location");
        }
    }

    private void initializeViews() {
        mapView = findViewById(R.id.map);
        selectedLocationText = findViewById(R.id.selectedLocationText);
        confirmButton = findViewById(R.id.confirmButton);

        confirmButton.setEnabled(false);
        confirmButton.setOnClickListener(v -> confirmLocationSelection());
    }

    private void loadLocations() {
        DB_Operations dbOperations = new DB_Operations(this);
        locations = dbOperations.getAllLocations();
    }

    private void setupMap() {
        // Basic map setup
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(7.0);

        // Set initial position to Sri Lanka
        GeoPoint sriLanka = new GeoPoint(7.8731, 80.7718);
        mapView.getController().setCenter(sriLanka);

        // Add my location overlay
        setupMyLocation();

        // Add location markers
        addLocationMarkers();
    }

    private void setupMyLocation() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }

    private void addLocationMarkers() {
        if (locations != null && !locations.isEmpty()) {
            for (Location location : locations) {
                addMarkerForLocation(location);
            }
        } else {
            Toast.makeText(this, "No rental locations available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMarkerForLocation(Location location) {
        Marker marker = new Marker(mapView);
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(location.getName());
        marker.setSnippet("Available Bikes: " + location.getAvailableBikes());

        // Set marker color based on bike availability
        int markerIcon = location.getAvailableBikes() > 0 ?
                R.drawable.marker_green : R.drawable.marker_red;
        marker.setIcon(getResources().getDrawable(markerIcon, getTheme()));

        // Handle marker click
        marker.setOnMarkerClickListener((marker1, mapView) -> {
            selectedLocation = location;
            updateSelectedLocationUI();
            return true;
        });

        mapView.getOverlays().add(marker);
    }

    private void updateSelectedLocationUI() {
        if (selectedLocation != null) {
            String locationInfo = String.format("%s\n%s\nAvailable Bikes: %d",
                    selectedLocation.getName(),
                    selectedLocation.getAddress(),
                    selectedLocation.getAvailableBikes());

            selectedLocationText.setText(locationInfo);
            confirmButton.setEnabled(selectedLocation.getAvailableBikes() > 0);

            if (selectedLocation.getAvailableBikes() == 0) {
                Toast.makeText(this,
                        "No bikes available at this location",
                        Toast.LENGTH_SHORT).show();
            }

            // Center map on selected location
            mapView.getController().animateTo(new GeoPoint(
                    selectedLocation.getLatitude(),
                    selectedLocation.getLongitude()
            ));
        }
    }

    private void confirmLocationSelection() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_location_id", selectedLocation.getId());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}