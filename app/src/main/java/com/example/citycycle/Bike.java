package com.example.citycycle;

public class Bike {
    private int bikeID;
    private String bikeType;
    private double pricePerHour;
    private byte[] image;
    private String description;
    private String availability;
    private String location;

    public Bike() {}

    public Bike(int bikeID, String bikeType, double pricePerHour, byte[] image,
                String description, String availability, String location) {
        this.bikeID = bikeID;
        this.bikeType = bikeType;
        this.pricePerHour = pricePerHour;
        this.image = image;
        this.description = description;
        this.availability = availability;
        this.location = location;
    }

    public int getBikeID() {
        return bikeID;
    }

    public void setBikeID(int bikeID) {
        this.bikeID = bikeID;
    }

    public String getBikeType() {
        return bikeType != null ? bikeType.toLowerCase() : null;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}