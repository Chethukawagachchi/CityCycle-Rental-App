package com.example.citycycle;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RentalsActivity extends AppCompatActivity {
    private static final String CURRENT_USER = "Chethukawagachchi";
    private static final String CURRENT_TIME = "2025-02-27 13:07:13";

    private RecyclerView rentalsRecyclerView;
    private RentalsAdapter rentalsAdapter;
    private ArrayList<BikeRental> allRentals, filteredRentals;
    private String selectedStatus = "All";
    private Date selectedDate = null;
    private TextView currentTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rentals);

        setupViews();
        loadRentals();
        setupFilters();
        notifyUpcomingRentals();
    }

    private void setupViews() {
        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Rentals");
        }

        // Setup current time display
        currentTimeText = findViewById(R.id.currentTimeText);
        currentTimeText.setText(String.format("Current Time (UTC): %s\nUser: %s",
                CURRENT_TIME, CURRENT_USER));

        // Setup RecyclerView
        rentalsRecyclerView = findViewById(R.id.rentals_recycler_view);
        rentalsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadRentals() {
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) userEmail = CURRENT_USER;

        DB_Operations dbOperations = new DB_Operations(this);
        allRentals = dbOperations.getRentalsByEmail(userEmail);
        filteredRentals = new ArrayList<>(allRentals);
        rentalsAdapter = new RentalsAdapter(filteredRentals);
        rentalsRecyclerView.setAdapter(rentalsAdapter);
    }

    private void setupFilters() {
        setupStatusFilter();
        setupDateFilter();
    }

    private void setupStatusFilter() {
        Spinner statusFilter = findViewById(R.id.status_filter);
        statusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = parent.getItemAtPosition(position).toString();
                filterRentals();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDateFilter() {
        Button dateFilterButton = findViewById(R.id.date_filter_button);
        dateFilterButton.setOnClickListener(v -> showDatePicker(dateFilterButton));
    }

    private void showDatePicker(Button dateButton) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate = calendar.getTime();
                    dateButton.setText(new SimpleDateFormat("yyyy-MM-dd",
                            Locale.getDefault()).format(selectedDate));
                    filterRentals();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void filterRentals() {
        filteredRentals.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (BikeRental rental : allRentals) {
            boolean matchesStatus = true;
            boolean matchesDate = selectedDate == null;

            try {
                Date endDate = dateFormat.parse(rental.getEndTime());
                Date currentDate = new Date();
                String status;

                if (endDate != null && endDate.before(currentDate)) {
                    status = "Completed";
                } else {
                    status = "Active";
                }

                matchesStatus = selectedStatus.equals("All") || status.equals(selectedStatus);

                if (selectedDate != null) {
                    Date rentalStartDate = dateFormat.parse(rental.getStartTime());
                    Calendar selectedCal = Calendar.getInstance();
                    Calendar rentalCal = Calendar.getInstance();
                    selectedCal.setTime(selectedDate);
                    rentalCal.setTime(rentalStartDate);

                    matchesDate = (selectedCal.get(Calendar.YEAR) == rentalCal.get(Calendar.YEAR)) &&
                            (selectedCal.get(Calendar.MONTH) == rentalCal.get(Calendar.MONTH)) &&
                            (selectedCal.get(Calendar.DAY_OF_MONTH) == rentalCal.get(Calendar.DAY_OF_MONTH));
                }

                if (matchesStatus && matchesDate) {
                    filteredRentals.add(rental);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        rentalsAdapter.notifyDataSetChanged();
    }

    private void notifyUpcomingRentals() {
        for (BikeRental rental : allRentals) {
            if (isUpcoming(rental)) {
                sendNotification(rental);
            }
        }
    }

    private boolean isUpcoming(BikeRental rental) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date startTime = dateFormat.parse(rental.getStartTime());
            Date currentTime = new Date();

            if (startTime != null) {
                long diffInHours = (startTime.getTime() - currentTime.getTime()) / (60 * 60 * 1000);
                return diffInHours > 0 && diffInHours <= 24; // Notify if rental is within next 24 hours
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendNotification(BikeRental rental) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "rental_notifications";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Rental Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, RentalsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new android.app.Notification.Builder(this, channelId)
                    .setContentTitle("Upcoming Bike Rental")
                    .setContentText("Your bike rental starts on " + rental.getStartTime())
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        if (builder != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
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