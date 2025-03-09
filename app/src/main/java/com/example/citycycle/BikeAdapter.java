package com.example.citycycle;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BikeAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Bike> bikeList;
    private ArrayList<Bike> originalBikeList;

    public BikeAdapter(Context context, ArrayList<Bike> bikeList) {
        this.context = context;
        this.bikeList = new ArrayList<>(bikeList);
        this.originalBikeList = new ArrayList<>(bikeList);
    }

    @Override
    public int getCount() {
        return bikeList.size();
    }

    @Override
    public Object getItem(int position) {
        return bikeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_bike, parent, false);
        }

        TextView txtBikeNumber = rowView.findViewById(R.id.txtBikeNumber);
        TextView txtBikeType = rowView.findViewById(R.id.txtBikeType);
        TextView txtPrice = rowView.findViewById(R.id.txtPrice);
        TextView txtDescription = rowView.findViewById(R.id.txtDescription);
        TextView txtLocation = rowView.findViewById(R.id.txtLocation);
        TextView txtAvailability = rowView.findViewById(R.id.txtAvailability);
        ImageView imgBike = rowView.findViewById(R.id.imgBike);
        Button btnRent = rowView.findViewById(R.id.buttonRent);

        Bike bike = bikeList.get(position);
        txtBikeNumber.setText("Bike Number: " + bike.getBikeID());
        txtBikeType.setText("Type: " + bike.getBikeType());
        txtPrice.setText("Price Per Hour: Rs." + String.format("%.2f", bike.getPricePerHour()));
        txtDescription.setText("Description: " + bike.getDescription());
        txtLocation.setText("Location: " + bike.getLocation());

        if (bike.getAvailability().equalsIgnoreCase("Available")) {
            txtAvailability.setText("Availability: Available");
            txtAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            btnRent.setEnabled(true);
        } else {
            txtAvailability.setText("Availability: Not Available");
            txtAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            btnRent.setEnabled(false);
        }

        if (bike.getImage() != null && bike.getImage().length > 0) {
            imgBike.setImageBitmap(BitmapFactory.decodeByteArray(bike.getImage(), 0, bike.getImage().length));
        } else {
            imgBike.setImageResource(R.drawable.default_bike);
        }

        btnRent.setOnClickListener(v -> rentBike(bike));

        return rowView;
    }

    public void filterByBikeTypePriceAndAvailability(String bikeType, Double minPrice, Double maxPrice,
                                                     boolean onlyAvailable, String location) {
        ArrayList<Bike> filteredBikes = new ArrayList<>();
        String bikeTypeLower = bikeType.toLowerCase();

        for (Bike bike : originalBikeList) {
            String bikeTypeFromList = bike.getBikeType() != null ? bike.getBikeType().toLowerCase() : "";

            boolean matchesBikeType = bikeTypeLower.equals("all") || bikeTypeFromList.equals(bikeTypeLower);
            String bikeLocation = bike.getLocation() != null ? bike.getLocation().toLowerCase().trim() : ""; // Handle null location
            boolean matchesLocation = location.equals("all") || bikeLocation.contains(location.toLowerCase().trim());

            boolean matchesPrice = true;
            if (minPrice != null && bike.getPricePerHour() < minPrice) {
                matchesPrice = false;
            }
            if (maxPrice != null && bike.getPricePerHour() > maxPrice) {
                matchesPrice = false;
            }

            boolean matchesAvailability = !onlyAvailable || bike.getAvailability().equals("Available");

            if (matchesBikeType && matchesPrice && matchesAvailability && matchesLocation) {
                filteredBikes.add(bike);
            }
        }

        bikeList.clear();
        bikeList.addAll(filteredBikes);
        notifyDataSetChanged();
    }

    public void resetFilter() {
        bikeList.clear();
        bikeList.addAll(originalBikeList);
        notifyDataSetChanged();
    }

    private void rentBike(Bike bike) {
        if (bike.getAvailability().equalsIgnoreCase("Available")) {
            Intent intent = new Intent(context, BikeRentalActivity.class);
            intent.putExtra("bikeID", bike.getBikeID());
            intent.putExtra("price", bike.getPricePerHour());
            intent.putExtra("location", bike.getLocation());
            intent.putExtra("bikeType", bike.getBikeType());
            intent.putExtra("userEmail", "Chethukawagachchi"); // Using the current user's login
            intent.putExtra("currentDateTime", "2025-02-27 08:44:12"); // Using the provided current datetime
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Sorry, this bike is not available for rental.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void updateBike(Bike updatedBike) {
        for (int i = 0; i < bikeList.size(); i++) {
            if (bikeList.get(i).getBikeID() == updatedBike.getBikeID()) {
                bikeList.set(i, updatedBike);
                break;
            }
        }

        for (int i = 0; i < originalBikeList.size(); i++) {
            if (originalBikeList.get(i).getBikeID() == updatedBike.getBikeID()) {
                originalBikeList.set(i, updatedBike);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void removeBike(int bikeID) {
        Bike bikeToRemove = null;
        for (Bike bike : bikeList) {
            if (bike.getBikeID() == bikeID) {
                bikeToRemove = bike;
                break;
            }
        }
        if (bikeToRemove != null) {
            bikeList.remove(bikeToRemove);
        }

        bikeToRemove = null;
        for (Bike bike : originalBikeList) {
            if (bike.getBikeID() == bikeID) {
                bikeToRemove = bike;
                break;
            }
        }
        if (bikeToRemove != null) {
            originalBikeList.remove(bikeToRemove);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Bike> getBikeList() {
        return bikeList;
    }

    public ArrayList<Bike> getOriginalBikeList() {
        return originalBikeList;
    }

    public void updateBikeAvailability(int bikeID, String availability) {
        for (Bike bike : bikeList) {
            if (bike.getBikeID() == bikeID) {
                bike.setAvailability(availability);
                break;
            }
        }
        for (Bike bike : originalBikeList) {
            if (bike.getBikeID() == bikeID) {
                bike.setAvailability(availability);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public boolean containsBike(int bikeID) {
        for (Bike bike : bikeList) {
            if (bike.getBikeID() == bikeID) {
                return true;
            }
        }
        return false;
    }

    public Bike getBikeById(int bikeID) {
        for (Bike bike : bikeList) {
            if (bike.getBikeID() == bikeID) {
                return bike;
            }
        }
        return null;
    }

    public void sortByPrice(boolean ascending) {
        ArrayList<Bike> sortedList = new ArrayList<>(bikeList);
        sortedList.sort((bike1, bike2) -> {
            if (ascending) {
                return Double.compare(bike1.getPricePerHour(), bike2.getPricePerHour());
            } else {
                return Double.compare(bike2.getPricePerHour(), bike1.getPricePerHour());
            }
        });
        bikeList.clear();
        bikeList.addAll(sortedList);
        notifyDataSetChanged();
    }

    public void sortByAvailability() {
        ArrayList<Bike> sortedList = new ArrayList<>(bikeList);
        sortedList.sort((bike1, bike2) -> {
            if (bike1.getAvailability().equals(bike2.getAvailability())) {
                return 0;
            }
            return bike1.getAvailability().equals("Available") ? -1 : 1;
        });
        bikeList.clear();
        bikeList.addAll(sortedList);
        notifyDataSetChanged();
    }
}