package com.example.citycycle;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AdminReportsActivity extends AppCompatActivity {

    private TextView timeDisplayText;
    private TextView totalRentalsText;
    private TextView totalRevenueText;
    private TextView averageRentalDurationText;
    private TextView mostPopularLocationText;
    private TextView activeRentalsText;
    private TextView completedRentalsText;
    private Handler timeHandler;
    private SimpleDateFormat dateFormat;
    private static final String CURRENT_USER = "Chethukawagachchi";
    private DB_Operations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        dbOperations = new DB_Operations(this);
        initializeViews();
        setupToolbar();
        setupTimeDisplay();
        loadReports();
    }

    private void initializeViews() {
        timeDisplayText = findViewById(R.id.timeDisplayText);
        totalRentalsText = findViewById(R.id.totalRentalsText);
        totalRevenueText = findViewById(R.id.totalRevenueText);
        averageRentalDurationText = findViewById(R.id.averageRentalDurationText);
        mostPopularLocationText = findViewById(R.id.mostPopularLocationText);
        activeRentalsText = findViewById(R.id.activeRentalsText);
        completedRentalsText = findViewById(R.id.completedRentalsText);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        timeHandler = new Handler(Looper.getMainLooper());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Reports");
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

    private void loadReports() {
        ArrayList<BikeRental> allRentals = getAllRentals();
        updateStatistics(allRentals);
    }

    private ArrayList<BikeRental> getAllRentals() {
        // Get all rentals from database
        return dbOperations.getAllRentals();
    }

    private void updateStatistics(ArrayList<BikeRental> rentals) {
        int totalRentals = rentals.size();
        double totalRevenue = 0;
        long totalDuration = 0;
        int activeCount = 0;
        int completedCount = 0;
        Map<String, Integer> locationCounts = new HashMap<>();
        Date currentTime = new Date();

        for (BikeRental rental : rentals) {
            try {
                // Calculate revenue
                totalRevenue += rental.getTotalPrice();

                // Calculate duration
                Date startTime = dateFormat.parse(rental.getStartTime());
                Date endTime = dateFormat.parse(rental.getEndTime());

                if (startTime != null && endTime != null) {
                    totalDuration += (endTime.getTime() - startTime.getTime()) / (60 * 60 * 1000); // in hours
                }

                // Count status
                if (endTime != null && endTime.before(currentTime)) {
                    completedCount++;
                } else {
                    activeCount++;
                }

                // Track location popularity
                Bike bike = dbOperations.getBikeById(rental.getBikeID());
                if (bike != null) {
                    String location = bike.getLocation();
                    locationCounts.put(location, locationCounts.getOrDefault(location, 0) + 1);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Find most popular location
        String mostPopularLocation = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : locationCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostPopularLocation = entry.getKey();
            }
        }

        // Update UI
        totalRentalsText.setText(String.format(Locale.US, "Total Rentals: %d", totalRentals));
        totalRevenueText.setText(String.format(Locale.US, "Total Revenue: $%.2f", totalRevenue));

        double avgDuration = totalRentals > 0 ? (double) totalDuration / totalRentals : 0;
        averageRentalDurationText.setText(String.format(Locale.US,
                "Average Rental Duration: %.1f hours", avgDuration));

        mostPopularLocationText.setText(String.format(Locale.US,
                "Most Popular Location: %s\n(%d rentals)",
                mostPopularLocation.isEmpty() ? "N/A" : mostPopularLocation, maxCount));

        activeRentalsText.setText(String.format(Locale.US, "Active Rentals: %d", activeCount));
        completedRentalsText.setText(String.format(Locale.US, "Completed Rentals: %d", completedCount));

        // Update card colors based on statistics
        updateCardColors(activeCount, completedCount);
    }

    private void updateCardColors(int activeCount, int completedCount) {
        CardView activeCard = findViewById(R.id.activeRentalsCard);
        CardView completedCard = findViewById(R.id.completedRentalsCard);

        // Set colors based on counts
        if (activeCount > completedCount) {
            activeCard.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
            completedCard.setCardBackgroundColor(Color.WHITE);
        } else {
            activeCard.setCardBackgroundColor(Color.WHITE);
            completedCard.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
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
}