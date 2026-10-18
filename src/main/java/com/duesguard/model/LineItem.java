package com.duesguard.model;

public class LineItem {
    private String description;
    private double quantity;
    private double rate;

    public LineItem() {}

    public LineItem(String description, double quantity, double rate) {
        this.description = description;
        this.quantity = quantity;
        this.rate = rate;
    }

    public double getAmount() { return quantity * rate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = Math.max(0, quantity); }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = Math.max(0, rate); }
}
