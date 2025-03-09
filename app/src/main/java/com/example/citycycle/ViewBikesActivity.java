package com.example.citycycle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class ViewBikesActivity extends AppCompatActivity {

    ListView listViewBikes;
    ArrayList<Bike> bikeList;
    BikeAdapter bikeAdapter;
    DB_Operations dbOperations;
    EditText editTextMinPrice, editTextMaxPrice;
    CheckBox checkBoxAvailability;
    LinearLayout filterOptionsContainer;
    private AutoCompleteTextView spinnerLocation;
    private TextInputLayout customLocationLayout;
    private EditText editTextCustomLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bikes);

        dbOperations = new DB_Operations(this);
        bikeList = dbOperations.getAllBikes();

        if (bikeList == null || bikeList.isEmpty()) {
            bikeList = new ArrayList<>();
            Toast.makeText(this, "No bikes available in the database.", Toast.LENGTH_SHORT).show();
        }

        bikeAdapter = new BikeAdapter(this, bikeList);
        listViewBikes = findViewById(R.id.listView);
        listViewBikes.setAdapter(bikeAdapter);

        Spinner spinnerBikeType = findViewById(R.id.spinnerBikeType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bike_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBikeType.setAdapter(adapter);

        editTextMinPrice = findViewById(R.id.editTextMinPrice);
        editTextMaxPrice = findViewById(R.id.editTextMaxPrice);

        checkBoxAvailability = findViewById(R.id.checkBoxAvailability);
        filterOptionsContainer = findViewById(R.id.filterOptionsContainer);

        // Initialize location views
        setupLocationSelection();

        Button buttonFilter = findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(v -> {
            if (validateLocation()) {
                applyFilters(spinnerBikeType.getSelectedItem().toString());
            }
        });

        spinnerBikeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilters("All");
            }
        });

        Button buttonHome = findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(v -> {
            Intent intent = new Intent(ViewBikesActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageView filterToggleIcon = findViewById(R.id.filterToggleIcon);
        filterToggleIcon.setOnClickListener(v -> {
            if (filterOptionsContainer.getVisibility() == View.GONE) {
                filterOptionsContainer.setVisibility(View.VISIBLE);
            } else {
                filterOptionsContainer.setVisibility(View.GONE);
            }
        });
    }

    private void setupLocationSelection() {
        spinnerLocation = findViewById(R.id.spinnerLocation);
        customLocationLayout = findViewById(R.id.customLocationLayout);
        editTextCustomLocation = findViewById(R.id.editTextCustomLocation);

        // Setup location adapter
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.predefined_locations,
                android.R.layout.simple_dropdown_item_1line
        );
        spinnerLocation.setAdapter(locationAdapter);

        // Handle location selection
        spinnerLocation.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLocation = parent.getItemAtPosition(position).toString();
            if (selectedLocation.equals("Custom Location")) {
                customLocationLayout.setVisibility(View.VISIBLE);
                editTextCustomLocation.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextCustomLocation, InputMethodManager.SHOW_IMPLICIT);
            } else {
                customLocationLayout.setVisibility(View.GONE);
                editTextCustomLocation.setText(""); // Clear the text

                // Apply filter with selected location ONLY if location is valid
                if (validateLocation()) {
                    applyFilters(
                            ((Spinner) findViewById(R.id.spinnerBikeType)).getSelectedItem().toString()
                    );
                }
            }
        });

        // Set initial selection
        spinnerLocation.setText(locationAdapter.getItem(0).toString(), false);
    }

    private void applyFilters(String selectedBikeType) {
        String minPriceStr = editTextMinPrice.getText().toString();
        String maxPriceStr = editTextMaxPrice.getText().toString();

        // Get location based on whether custom location is visible
        String location;
        if (customLocationLayout.getVisibility() == View.VISIBLE) {
            location = editTextCustomLocation.getText().toString().trim();
            if (location.isEmpty()) {
                location = "all";
            }
        } else {
            location = spinnerLocation.getText().toString();
            if (location.equals("Select Location")) {
                location = "all";
            }
        }

        Double minPrice = minPriceStr.isEmpty() ? null : Double.parseDouble(minPriceStr);
        Double maxPrice = maxPriceStr.isEmpty() ? null : Double.parseDouble(maxPriceStr);
        boolean onlyAvailable = checkBoxAvailability.isChecked();

        bikeAdapter.filterByBikeTypePriceAndAvailability(
                selectedBikeType,
                minPrice,
                maxPrice,
                onlyAvailable,
                location
        );
    }

    // Add this method to validate location input
    private boolean validateLocation() {
        if (customLocationLayout.getVisibility() == View.VISIBLE) {
            String customLocation = editTextCustomLocation.getText().toString().trim();
            if (customLocation.isEmpty()) {
                editTextCustomLocation.setError("Please enter a location");
                return false;
            }
        } else {
            String selectedLocation = spinnerLocation.getText().toString();
            if (selectedLocation.equals("Select Location")) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}