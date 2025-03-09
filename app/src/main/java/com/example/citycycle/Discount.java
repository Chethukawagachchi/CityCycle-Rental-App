package com.example.citycycle;

public class Discount {
    private int id;
    private String code;
    private int percentage;
    private String validUntil;
    private String createdAt;
    private String createdBy;

    public Discount(int id, String code, int percentage, String validUntil,
                    String createdAt, String createdBy) {
        this.id = id;
        this.code = code;
        this.percentage = percentage;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    // Getters
    public int getId() { return id; }
    public String getCode() { return code; }
    public int getPercentage() { return percentage; }
    public String getValidUntil() { return validUntil; }
    public String getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }

    // Setters if needed
    public void setCode(String code) { this.code = code; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
    public void setValidUntil(String validUntil) { this.validUntil = validUntil; }
}