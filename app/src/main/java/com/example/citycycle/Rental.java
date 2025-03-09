package com.example.citycycle;

public class Rental {
    private int id;
    private String bikeId;
    private String location;
    private String startTime;
    private String endTime;
    private double cost;
    private String status;

    public Rental(int id, String bikeId, String location, String startTime, String endTime, double cost, String status) {
        this.id = id;
        this.bikeId = bikeId;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cost = cost;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getBikeId() { return bikeId; }
    public String getLocation() { return location; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getCost() { return cost; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setBikeId(String bikeId) { this.bikeId = bikeId; }
    public void setLocation(String location) { this.location = location; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setCost(double cost) { this.cost = cost; }
    public void setStatus(String status) { this.status = status; }
}