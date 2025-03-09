package com.example.citycycle;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;  // Use this import instead of android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RentalHistoryActivity extends AppCompatActivity {
    private static final String CURRENT_USER = "Chethukawagachchi";
    private static final String CURRENT_TIME = "2025-02-28 04:12:36";

    private RecyclerView rentalHistoryRecyclerView;
    private RentalHistoryAdapter historyAdapter;
    private ArrayList<BikeRental> allRentals, filteredRentals;
    private TextView currentTimeText, totalSpentText, totalRentalsText;
    private MaterialCardView statsCard;
    private ChipGroup filterChipGroup;
    private DB_Operations dbOperations;
    private SimpleDateFormat dateFormat;

    // Filter constants
    private static final int FILTER_ALL = 0;
    private static final int FILTER_RECENT = 1;
    private static final int FILTER_PAST_MONTH = 2;
    private static final int FILTER_OLDER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_history);

        initializeViews();
        setupToolbar();
        setupDateTime();
        loadRentalHistory();
        setupFilters();
        calculateStats();
    }

    private void initializeViews() {
        rentalHistoryRecyclerView = findViewById(R.id.rental_history_recycler_view);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalSpentText = findViewById(R.id.totalAmountText);
        totalRentalsText = findViewById(R.id.totalRentalsText);
        statsCard = findViewById(R.id.statsCard);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        rentalHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dbOperations = new DB_Operations(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Rental History");
        }
    }

    private void setupDateTime() {
        currentTimeText.setText(String.format("Current Time (UTC): %s\nUser: %s",
                CURRENT_TIME, CURRENT_USER));
    }

    private void loadRentalHistory() {
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) userEmail = CURRENT_USER;

        allRentals = dbOperations.getRentalsByEmail(userEmail);
        filteredRentals = new ArrayList<>(allRentals);

        historyAdapter = new RentalHistoryAdapter(filteredRentals, this::showRentalDetailsDialog);
        rentalHistoryRecyclerView.setAdapter(historyAdapter);
    }

    private void setupFilters() {
        // Initialize chips
        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipRecent = findViewById(R.id.chipRecent);
        Chip chipPastMonth = findViewById(R.id.chipPastMonth);
        Chip chipOlder = findViewById(R.id.chipOlder);

        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAll.setChecked(true);
                return;
            }

            int filterId;
            if (chipRecent.isChecked()) filterId = FILTER_RECENT;
            else if (chipPastMonth.isChecked()) filterId = FILTER_PAST_MONTH;
            else if (chipOlder.isChecked()) filterId = FILTER_OLDER;
            else filterId = FILTER_ALL;

            applyFilter(filterId);
        });

        // Set default selection
        chipAll.setChecked(true);
    }

    private void applyFilter(int filterId) {
        filteredRentals.clear();
        Date currentDate = new Date();

        for (BikeRental rental : allRentals) {
            try {
                Date endDate = dateFormat.parse(rental.getEndTime());
                if (endDate != null) {
                    boolean shouldInclude = false;
                    long diffInDays = (currentDate.getTime() - endDate.getTime()) / (24 * 60 * 60 * 1000);

                    switch (filterId) {
                        case FILTER_ALL:
                            shouldInclude = true;
                            break;
                        case FILTER_RECENT:
                            shouldInclude = diffInDays <= 30;
                            break;
                        case FILTER_PAST_MONTH:
                            shouldInclude = diffInDays > 30 && diffInDays <= 60;
                            break;
                        case FILTER_OLDER:
                            shouldInclude = diffInDays > 60;
                            break;
                    }

                    if (shouldInclude) {
                        filteredRentals.add(rental);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        historyAdapter.notifyDataSetChanged();
        calculateStats();
        updateEmptyState();
    }

    private void calculateStats() {
        double totalSpent = 0;
        for (BikeRental rental : filteredRentals) {
            totalSpent += rental.getTotalPrice();
        }

        totalSpentText.setText(String.format(Locale.getDefault(),
                "Total: Rs.%.2f", totalSpent));
        totalRentalsText.setText(String.format(Locale.getDefault(),
                "Total Rentals: %d", filteredRentals.size()));
    }

    private void updateEmptyState() {
        View emptyState = findViewById(R.id.emptyStateContainer);
        if (filteredRentals.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rentalHistoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rentalHistoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showRentalDetailsDialog(BikeRental rental) {
        // Implementation for showing rental details will be added later
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}