package com.example.citycycle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdminViewBikesActivity extends AppCompatActivity {
    private ListView listViewBikes;
    private ArrayList<Bike> bikeList;
    private BikeAdapter bikeAdapter;
    private DB_Operations dbOperations;
    private EditText editTextMinPrice, editTextMaxPrice, editTextLocation;
    private CheckBox checkBoxAvailability;
    private LinearLayout filterOptionsContainer;
    private TextView currentTimeText;
    private TextView currentUserText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_bikes);

        initializeViews();
        setupCurrentInfo();
        loadBikes();
        setupSpinner();
        setupListeners();
    }

    private void initializeViews() {
        listViewBikes = findViewById(R.id.listView);
        editTextMinPrice = findViewById(R.id.editTextMinPrice);
        editTextMaxPrice = findViewById(R.id.editTextMaxPrice);
        editTextLocation = findViewById(R.id.editTextLocation);
        checkBoxAvailability = findViewById(R.id.checkBoxAvailability);
        filterOptionsContainer = findViewById(R.id.filterOptionsContainer);
        currentTimeText = findViewById(R.id.currentTimeText);
        currentUserText = findViewById(R.id.currentUserText);
    }

    private void setupCurrentInfo() {
        currentTimeText.setText("Current Time (UTC): 2025-02-27 11:05:00");
        currentUserText.setText("Admin: Chethukawagachchi");
    }

    private void loadBikes() {
        dbOperations = new DB_Operations(this);
        bikeList = dbOperations.getAllBikes();

        if (bikeList == null || bikeList.isEmpty()) {
            bikeList = new ArrayList<>();
            Toast.makeText(this, "No bikes available in the database.", Toast.LENGTH_SHORT).show();
        }

        bikeAdapter = new BikeAdapter(this, bikeList);
        listViewBikes.setAdapter(bikeAdapter);
    }

    private void setupSpinner() {
        Spinner spinnerBikeType = findViewById(R.id.spinnerBikeType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bike_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBikeType.setAdapter(adapter);

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
    }

    private void setupListeners() {
        Button buttonFilter = findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(v -> applyFilters(
                ((Spinner) findViewById(R.id.spinnerBikeType)).getSelectedItem().toString()));

        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminViewBikesActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        ImageView filterToggleIcon = findViewById(R.id.filterToggleIcon);
        filterToggleIcon.setOnClickListener(v -> toggleFilterOptions());
    }

    private void toggleFilterOptions() {
        if (filterOptionsContainer.getVisibility() == View.GONE) {
            filterOptionsContainer.setVisibility(View.VISIBLE);
        } else {
            filterOptionsContainer.setVisibility(View.GONE);
        }
    }

    private void applyFilters(String selectedBikeType) {
        String minPriceStr = editTextMinPrice.getText().toString();
        String maxPriceStr = editTextMaxPrice.getText().toString();
        String location = editTextLocation.getText().toString();

        Double minPrice = minPriceStr.isEmpty() ? null : Double.parseDouble(minPriceStr);
        Double maxPrice = maxPriceStr.isEmpty() ? null : Double.parseDouble(maxPriceStr);
        boolean onlyAvailable = checkBoxAvailability.isChecked();

        bikeAdapter.filterByBikeTypePriceAndAvailability(
                selectedBikeType,
                minPrice,
                maxPrice,
                onlyAvailable,
                location.isEmpty() ? "all" : location
        );
    }
}