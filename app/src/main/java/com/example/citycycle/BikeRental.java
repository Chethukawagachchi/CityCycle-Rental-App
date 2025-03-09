package com.example.citycycle;

public class BikeRental {
    private int rentalID;
    private String email;
    private int bikeID;
    private String startTime;
    private String endTime;
    private double totalPrice;
    private String bikeType;
    private String location;
    private String discountCode; // Add this
    private int discountPercentage; // Add this


    public BikeRental() {}

    public BikeRental(int rentalID, String email, int bikeID, String startTime,
                      String endTime, double totalPrice) {
        this.rentalID = rentalID;
        this.email = email;
        this.bikeID = bikeID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public int getRentalID() {
        return rentalID;
    }

    public void setRentalID(int rentalID) {
        this.rentalID = rentalID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getBikeID() {
        return bikeID;
    }

    public void setBikeID(int bikeID) {
        this.bikeID = bikeID;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}