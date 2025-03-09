package com.example.citycycle;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RentalHistoryAdapter extends RecyclerView.Adapter<RentalHistoryAdapter.ViewHolder> {
    private final ArrayList<BikeRental> rentals;
    private final OnRentalClickListener listener;
    private final SimpleDateFormat displayFormat;
    private final SimpleDateFormat parseFormat;

    public interface OnRentalClickListener {
        void onRentalClick(BikeRental rental);
    }

    public RentalHistoryAdapter(ArrayList<BikeRental> rentals, OnRentalClickListener listener) {
        this.rentals = rentals;
        this.listener = listener;
        this.displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        this.parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rental_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BikeRental rental = rentals.get(position);
        try {
            // Format dates
            Date startDate = parseFormat.parse(rental.getStartTime());
            Date endDate = parseFormat.parse(rental.getEndTime());

            String formattedStart = startDate != null ? displayFormat.format(startDate) : rental.getStartTime();
            String formattedEnd = endDate != null ? displayFormat.format(endDate) : rental.getEndTime();

            // Calculate duration
            long durationMillis = endDate != null && startDate != null ?
                    endDate.getTime() - startDate.getTime() : 0;
            long hours = durationMillis / (60 * 60 * 1000);

            // Set views
            holder.bikeIdText.setText(String.format(Locale.getDefault(), "Bike ID: %d", rental.getBikeID()));
            holder.bikeTypeText.setText(rental.getBikeType());
            holder.dateRangeText.setText(String.format("From: %s\nTo: %s", formattedStart, formattedEnd));
            holder.durationText.setText(String.format(Locale.getDefault(), "%d hours", hours));
            holder.priceText.setText(String.format(Locale.getDefault(), "Rs.%.2f", rental.getTotalPrice()));

            // Set status badge
            boolean isCompleted = endDate != null && endDate.before(new Date());
            holder.statusBadge.setText(isCompleted ? "Completed" : "Active");

            // Set status badge color programmatically
            if (isCompleted) {
                holder.statusBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            } else {
                holder.statusBadge.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
            }

            // Set rounded corners for status badge
            holder.statusBadge.setBackground(createRoundedBackground(
                    isCompleted ? Color.parseColor("#4CAF50") : Color.parseColor("#FF9800")
            ));

            // Set location if available
            if (rental.getLocation() != null) {
                holder.locationText.setText(rental.getLocation());
                holder.locationText.setVisibility(View.VISIBLE);
            } else {
                holder.locationText.setVisibility(View.GONE);
            }

            // Set click listener
            holder.itemView.setOnClickListener(v -> listener.onRentalClick(rental));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private android.graphics.drawable.GradientDrawable createRoundedBackground(int color) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16); // 16dp rounded corners
        shape.setColor(color);
        return shape;
    }

    @Override
    public int getItemCount() {
        return rentals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bikeIdText;
        TextView bikeTypeText;
        TextView dateRangeText;
        TextView durationText;
        TextView priceText;
        TextView statusBadge;
        TextView locationText;

        ViewHolder(View itemView) {
            super(itemView);
            bikeIdText = itemView.findViewById(R.id.bikeIdText);
            bikeTypeText = itemView.findViewById(R.id.bikeTypeText);
            dateRangeText = itemView.findViewById(R.id.dateRangeText);
            durationText = itemView.findViewById(R.id.durationText);
            priceText = itemView.findViewById(R.id.priceText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            locationText = itemView.findViewById(R.id.locationText);
        }
    }
}
