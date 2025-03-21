package com.example.citycycle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

    private final ArrayList<Location> locations;
    private final Context context;
    private final SimpleDateFormat dateFormat;

    public LocationsAdapter(Context context, ArrayList<Location> locations) {
        this.context = context;
        this.locations = locations;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location location = locations.get(position);

        // Set location details
        holder.nameText.setText(location.getName());
        holder.addressText.setText(location.getAddress());
        holder.descriptionText.setText(location.getDescription());
        updateBikeCount(holder, location);
        holder.coordinatesText.setText(String.format(Locale.US,
                "Location: %.4f, %.4f",
                location.getLatitude(),
                location.getLongitude()));

        // Configure book button
        updateBookButtonState(holder.bookButton, location);
        holder.bookButton.setOnClickListener(v -> showBookingDialog(location, holder));
    }

    private void updateBikeCount(ViewHolder holder, Location location) {
        String bikeText;
        int bikes = location.getAvailableBikes();

        if (bikes > 0) {
            bikeText = String.format(Locale.US, "Available Bikes: %d", bikes);
            holder.bikesText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            bikeText = "No bikes available";
            holder.bikesText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        holder.bikesText.setText(bikeText);
    }

    private void updateBookButtonState(Button button, Location location) {
        boolean hasAvailableBikes = location.getAvailableBikes() > 0;
        button.setEnabled(hasAvailableBikes);
        button.setText(hasAvailableBikes ? "Book a Bike" : "No Bikes Available");

        // Update button appearance based on availability
        if (hasAvailableBikes) {
            button.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            button.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void showBookingDialog(Location location, ViewHolder holder) {
        Intent intent = new Intent(context, SelectLocationActivity.class);
        ((AppCompatActivity) context).startActivityForResult(intent, 1001);
    }

    private void processBooking(Location location, ViewHolder holder) {
        if (location.getAvailableBikes() > 0) {
            // Update the bike count
            location.setAvailableBikes(location.getAvailableBikes() - 1);

            // Update database
            DB_Operations dbOperations = new DB_Operations(context);
            boolean success = dbOperations.updateLocation(location);

            if (success) {
                // Update UI
                updateBikeCount(holder, location);
                updateBookButtonState(holder.bookButton, location);

                // Show success message with booking details
                String currentTime = dateFormat.format(new Date());
                showBookingConfirmation(location, currentTime);
            } else {
                // Show error message
                Toast.makeText(context,
                        "Failed to book bike. Please try again.",
                        Toast.LENGTH_LONG).show();

                // Revert the bike count
                location.setAvailableBikes(location.getAvailableBikes() + 1);
            }
        } else {
            Toast.makeText(context,
                    "Sorry, no bikes available at this location.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showBookingConfirmation(Location location, String bookingTime) {
        new AlertDialog.Builder(context)
                .setTitle("Booking Successful!")
                .setMessage(String.format(Locale.US,
                        "Booking Details:\n\n" +
                                "Location: %s\n" +
                                "Address: %s\n" +
                                "Booking Time: %s\n\n" +
                                "Please pick up your bike within 30 minutes.",
                        location.getName(),
                        location.getAddress(),
                        bookingTime))
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView addressText;
        final TextView descriptionText;
        final TextView bikesText;
        final TextView coordinatesText;
        final Button bookButton;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            addressText = view.findViewById(R.id.addressText);
            descriptionText = view.findViewById(R.id.descriptionText);
            bikesText = view.findViewById(R.id.bikesText);
            coordinatesText = view.findViewById(R.id.coordinatesText);
            bookButton = view.findViewById(R.id.bookButton);
        }
    }
}