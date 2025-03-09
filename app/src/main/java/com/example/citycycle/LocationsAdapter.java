package com.example.citycycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

    private final ArrayList<Location> locations;

    public LocationsAdapter(ArrayList<Location> locations) {
        this.locations = locations;
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
        holder.nameText.setText(location.getName());
        holder.addressText.setText(location.getAddress());
        holder.descriptionText.setText(location.getDescription());
        holder.bikesText.setText(String.format("Available Bikes: %d", location.getAvailableBikes()));
        holder.coordinatesText.setText(String.format("Coordinates: %.4f, %.4f",
                location.getLatitude(), location.getLongitude()));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView addressText;
        TextView descriptionText;
        TextView bikesText;
        TextView coordinatesText;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            addressText = view.findViewById(R.id.addressText);
            descriptionText = view.findViewById(R.id.descriptionText);
            bikesText = view.findViewById(R.id.bikesText);
            coordinatesText = view.findViewById(R.id.coordinatesText);
        }
    }
}