package com.example.citycycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ManageLocationsAdapter extends RecyclerView.Adapter<ManageLocationsAdapter.ViewHolder> {

    private final ArrayList<Location> locations;
    private final LocationActionListener actionListener;

    public interface LocationActionListener {
        void onEditClick(Location location);
        void onDeleteClick(Location location);
    }

    public ManageLocationsAdapter(ArrayList<Location> locations, LocationActionListener listener) {
        this.locations = locations;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_location, parent, false);
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

        holder.editButton.setOnClickListener(v -> actionListener.onEditClick(location));
        holder.deleteButton.setOnClickListener(v -> actionListener.onDeleteClick(location));
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
        ImageButton editButton;
        ImageButton deleteButton;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.locationNameText);
            addressText = view.findViewById(R.id.locationAddressText);
            descriptionText = view.findViewById(R.id.locationDescriptionText);
            bikesText = view.findViewById(R.id.availableBikesText);
            coordinatesText = view.findViewById(R.id.coordinatesText);
            editButton = view.findViewById(R.id.editButton);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}