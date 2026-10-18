package com.duesguard.model;

import java.time.LocalDateTime;

public class Business {
    private String id;
    private String name;
    private String ownerName;
    private String type;
    private String email;
    private String phone;
    private String city;
    private String currency;
    private int defaultPaymentTerms;
    private LocalDateTime createdAt;

    public Business() {
        this.id = java.util.UUID.randomUUID().toString();
        this.currency = "INR";
        this.defaultPaymentTerms = 30;
        this.createdAt = LocalDateTime.now();
    }

    public Business(String name, String ownerName, String type, String email, String phone, String city) {
        this();
        this.name = name;
        this.ownerName = ownerName;
        this.type = type;
        this.email = email;
        this.phone = phone;
        this.city = city;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Business name cannot be empty");
        this.name = name.trim();
    }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getDefaultPaymentTerms() { return defaultPaymentTerms; }
    public void setDefaultPaymentTerms(int days) {
        if (days < 0) throw new IllegalArgumentException("Payment terms cannot be negative");
        this.defaultPaymentTerms = days;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Business{name='" + name + "', ownerName='" + ownerName + "', city='" + city + "'}";
    }
}
