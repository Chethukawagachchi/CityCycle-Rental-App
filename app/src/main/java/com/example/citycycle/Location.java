package com.example.citycycle;

public class Location {
    private int id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String description;
    private int availableBikes;

    public Location(int id, String name, String address, double latitude,
                    double longitude, String description, int availableBikes) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.availableBikes = availableBikes;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public int getAvailableBikes() { return availableBikes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setDescription(String description) { this.description = description; }
    public void setAvailableBikes(int availableBikes) { this.availableBikes = availableBikes; }
}