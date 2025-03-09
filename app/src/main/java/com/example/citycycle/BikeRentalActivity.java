package com.example.citycycle;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BikeRentalActivity extends AppCompatActivity {

    private TextView txtBikeDetails, txtTotalPrice, textDuration;
    private EditText editTextEmail, editTextStartTime, editTextEndTime, editTextDiscountCode; // Added discount code EditText
    private Button buttonConfirm, buttonApplyDiscount; // Added apply discount button
    private TextView textDiscountApplied; // Added discount applied TextView
    private Discount appliedDiscount; // Added discount object
    private DB_Operations dbOperations;
    private Integer bikeID;
    private double pricePerHour;
    private static final String CHANNEL_ID = "rental_notifications";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final String CURRENT_UTC_TIME = "2025-03-03 03:14:53";
    private static final String CURRENT_USER = "Chethukawagachchi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_rental);

        txtBikeDetails = findViewById(R.id.textBikeDetails);
        txtTotalPrice = findViewById(R.id.textTotalPrice);
        textDuration = findViewById(R.id.textDuration); // Initialize duration TextView
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextStartTime = findViewById(R.id.editTextStartTime);
        editTextEndTime = findViewById(R.id.editTextEndTime);
        buttonConfirm = findViewById(R.id.buttonConfirm);


        bikeID = getIntent().getIntExtra("bikeID", 0);
        pricePerHour = getIntent().getDoubleExtra("price", 0);
        String location = getIntent().getStringExtra("location");
        String bikeType = getIntent().getStringExtra("bikeType");

        txtBikeDetails.setText(String.format("Bike ID: %d\nType: %s\nLocation: %s\nPrice per Hour: Rs.%.2f",
                bikeID, bikeType, location, pricePerHour));

        dbOperations = new DB_Operations(this);

        // Set current user's email
        String userEmail = getIntent().getStringExtra("userEmail");
        if (userEmail != null) {
            editTextEmail.setText(userEmail);
        }

        // Set current time as start time
        String currentDateTime = getIntent().getStringExtra("currentDateTime");
        if (currentDateTime != null) {
            editTextStartTime.setText(currentDateTime);
        }

        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(BikeRentalActivity.this, ViewBikesActivity.class);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        });

        createNotificationChannel();

        editTextStartTime.setOnClickListener(v -> showTimePickerDialog(editTextStartTime));
        editTextEndTime.setOnClickListener(v -> showTimePickerDialog(editTextEndTime));
        buttonConfirm.setOnClickListener(v -> confirmRental());

        checkSmsPermission();

        initializeDiscountViews(); // Initialize Discount views
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }


    private void showTimePickerDialog(EditText editText) {
        Calendar currentTime = Calendar.getInstance();

        // Create DatePickerDialog first
        new DatePickerDialog(this,
                (datePicker, year, month, day) -> {
                    // After date is picked, show TimePickerDialog
                    new TimePickerDialog(this,
                            (timePicker, hour, minute) -> {
                                // Combine date and time
                                Calendar selectedDateTime = Calendar.getInstance();
                                selectedDateTime.set(year, month, day, hour, minute, 0);

                                // Format the date and time
                                String formattedDateTime = dateFormat.format(selectedDateTime.getTime());
                                editText.setText(formattedDateTime);

                                // If both times are set, calculate price
                                if (!editTextStartTime.getText().toString().isEmpty() &&
                                        !editTextEndTime.getText().toString().isEmpty()) {
                                    calculateAndDisplayTotalPrice();
                                }
                            },
                            currentTime.get(Calendar.HOUR_OF_DAY),
                            currentTime.get(Calendar.MINUTE),
                            true // 24-hour format
                    ).show();
                },
                currentTime.get(Calendar.YEAR),
                currentTime.get(Calendar.MONTH),
                currentTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // Update calculateAndDisplayTotalPrice method to also update duration
    private void calculateAndDisplayTotalPrice() {
        try {
            Date startTime = dateFormat.parse(editTextStartTime.getText().toString());
            Date endTime = dateFormat.parse(editTextEndTime.getText().toString());

            if (startTime != null && endTime != null) {
                long differenceInMillis = endTime.getTime() - startTime.getTime();
                double hours = differenceInMillis / (1000.0 * 60 * 60);
                double totalPrice = hours * pricePerHour;

                // Apply discount if available
                if (appliedDiscount != null) {
                    double discountAmount = (totalPrice * appliedDiscount.getPercentage()) / 100.0;
                    totalPrice -= discountAmount;
                }

                // Update duration text
                textDuration.setText(String.format(Locale.getDefault(),
                        "Rental Duration: %.1f hours", hours));

                // Update total price
                txtTotalPrice.setText(String.format(Locale.getDefault(),
                        "Total Price: Rs.%.2f", totalPrice));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error calculating price", Toast.LENGTH_SHORT).show();
        }
    }

    // Add method to check if selected time is valid
    private boolean isValidTime(String startTime, String endTime) {
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);
            Date current = new Date();

            if (start != null && end != null) {
                // Check if start time is in the future
                if (start.before(current)) {
                    Toast.makeText(this,
                            "Start time must be in the future",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                // Check if end time is after start time
                if (end.before(start)) {
                    Toast.makeText(this,
                            "End time must be after start time",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                // Calculate duration in hours
                long differenceInMillis = end.getTime() - start.getTime();
                double hours = differenceInMillis / (1000.0 * 60 * 60);

                // Check minimum rental duration (1 hour)
                if (hours < 1) {
                    Toast.makeText(this,
                            "Minimum rental duration is 1 hour",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                // Check maximum rental duration (24 hours)
                if (hours > 24) {
                    Toast.makeText(this,
                            "Maximum rental duration is 24 hours",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void confirmRental() {
        String email = editTextEmail.getText().toString();
        String startTime = editTextStartTime.getText().toString();
        String endTime = editTextEndTime.getText().toString();

        if (email.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidTime(startTime, endTime)) {
            return;
        }

        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            boolean isAvailable = dbOperations.isBikeAvailable(bikeID, startTime, endTime);
            if (!isAvailable) {
                Toast.makeText(this, "Bike is already booked for the selected time period",
                        Toast.LENGTH_LONG).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Rental")
                    .setMessage(createRentalConfirmationMessage())
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String totalPriceText = txtTotalPrice.getText().toString()
                                .replace("Total Price: Rs.", "").trim();
                        double totalPrice = Double.parseDouble(totalPriceText);

                        BikeRental rental = new BikeRental();
                        rental.setEmail(email);
                        rental.setBikeID(bikeID);
                        rental.setStartTime(startTime);
                        rental.setEndTime(endTime);
                        rental.setTotalPrice(totalPrice);

                        if (appliedDiscount != null) {
                            rental.setDiscountCode(appliedDiscount.getCode());
                            rental.setDiscountPercentage(appliedDiscount.getPercentage());
                        }

                        User user = dbOperations.getUserByEmail(email);
                        if (user != null) {
                            dbOperations.rentBike(rental);
                            dbOperations.updateBikeAvailability(bikeID, "Not Available");

                            Toast.makeText(this, "Rental confirmed! Total Price: Rs." + totalPrice,
                                    Toast.LENGTH_SHORT).show();

                            sendNotification(email, totalPrice);
                            sendSmsNotification(user.getMobile(), rental);

                            finish();
                        } else {
                            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    private String createRentalConfirmationMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Please confirm the following rental details:\n\n");
        message.append(String.format("Bike ID: %d\n", bikeID));
        message.append(String.format("Start Time: %s\n", editTextStartTime.getText()));
        message.append(String.format("End Time: %s\n", editTextEndTime.getText()));

        if (appliedDiscount != null) {
            message.append(String.format("Discount Applied: %d%%\n", appliedDiscount.getPercentage()));
        }

        message.append(String.format("Total Price: %s", txtTotalPrice.getText()));

        return message.toString();
    }


    private void sendSmsNotification(int mobile, BikeRental rental) {
        String message = String.format("Rental confirmed!\nBike ID: %d\nStart Time: %s\n" +
                        "End Time: %s\nTotal Price: Rs.%.2f",
                rental.getBikeID(), rental.getStartTime(), rental.getEndTime(), rental.getTotalPrice());

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(String.valueOf(mobile), null, message, null, null);
        } catch (Exception e) {
            Log.e("BikeRentalActivity", "Failed to send SMS", e);
        }
    }

    private void sendNotification(String email, double totalPrice) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Rental Confirmation")
                .setContentText(String.format("Your rental has been confirmed for bike ID: %d. " +
                        "Total Price: Rs.%.2f. Confirmation sent to: %s", bikeID, totalPrice, email))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Rental Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for rental notifications");
            channel.setSound(soundUri, null);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 1000});

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void initializeDiscountViews() {
        editTextDiscountCode = findViewById(R.id.editTextDiscountCode);
        buttonApplyDiscount = findViewById(R.id.buttonApplyDiscount);
        textDiscountApplied = findViewById(R.id.textDiscountApplied);

        buttonApplyDiscount.setOnClickListener(v -> applyDiscount());
    }

    private void applyDiscount() {
        String discountCode = editTextDiscountCode.getText().toString().trim();

        if (discountCode.isEmpty()) {
            Toast.makeText(this, "Please enter a discount code", Toast.LENGTH_SHORT).show();
            return;
        }

        Discount discount = dbOperations.getDiscountByCode(discountCode);

        if (discount != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date validUntil = sdf.parse(discount.getValidUntil());
                Date currentDate = sdf.parse(CURRENT_UTC_TIME.split(" ")[0]);

                if (validUntil != null && validUntil.after(currentDate)) {
                    appliedDiscount = discount;
                    textDiscountApplied.setVisibility(View.VISIBLE);
                    textDiscountApplied.setText(String.format("Discount Applied: %d%% off", discount.getPercentage()));

                    // Disable discount input after successful application
                    editTextDiscountCode.setEnabled(false);
                    buttonApplyDiscount.setEnabled(false);

                    // Recalculate total price with discount
                    calculateAndDisplayTotalPrice();

                    Toast.makeText(this, "Discount applied successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "This discount code has expired", Toast.LENGTH_SHORT).show();
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Error validating discount", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid discount code", Toast.LENGTH_SHORT).show();
        }
    }
}