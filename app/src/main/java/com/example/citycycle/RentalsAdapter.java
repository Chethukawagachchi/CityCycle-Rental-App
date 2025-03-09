package com.example.citycycle;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RentalsAdapter extends RecyclerView.Adapter<RentalsAdapter.RentalViewHolder> {
    private ArrayList<BikeRental> rentals;
    private SimpleDateFormat displayFormat;
    private SimpleDateFormat parseFormat;

    public RentalsAdapter(ArrayList<BikeRental> rentals) {
        this.rentals = rentals;
        this.displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        this.parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    @Override
    public RentalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rental, parent, false);
        return new RentalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentalViewHolder holder, int position) {
        BikeRental rental = rentals.get(position);
        try {
            // Format dates for display
            Date startDate = parseFormat.parse(rental.getStartTime());
            Date endDate = parseFormat.parse(rental.getEndTime());
            String formattedStart = startDate != null ? displayFormat.format(startDate) : rental.getStartTime();
            String formattedEnd = endDate != null ? displayFormat.format(endDate) : rental.getEndTime();

            // Set rental details
            holder.bikeIdText.setText(String.format("Bike ID: %d", rental.getBikeID()));
            holder.timeRangeText.setText(String.format("From: %s\nTo: %s", formattedStart, formattedEnd));
            holder.priceText.setText(String.format("Total Price: Rs.%.2f", rental.getTotalPrice()));

            // Determine and set status
            String status = determineStatus(endDate);
            holder.statusText.setText(status);

            // Set status color directly
            if (status.equals("Active")) {
                holder.statusText.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else {
                holder.statusText.setTextColor(Color.parseColor("#757575")); // Gray
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String determineStatus(Date endDate) {
        if (endDate == null) return "Unknown";
        return endDate.before(new Date()) ? "Completed" : "Active";
    }

    @Override
    public int getItemCount() {
        return rentals.size();
    }

    static class RentalViewHolder extends RecyclerView.ViewHolder {
        TextView bikeIdText;
        TextView timeRangeText;
        TextView priceText;
        TextView statusText;

        RentalViewHolder(View itemView) {
            super(itemView);
            bikeIdText = itemView.findViewById(R.id.bikeIdText);
            timeRangeText = itemView.findViewById(R.id.timeRangeText);
            priceText = itemView.findViewById(R.id.priceText);
            statusText = itemView.findViewById(R.id.statusText);
        }
    }
}