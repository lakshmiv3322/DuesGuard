package com.duesguard.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Client {
    public enum ReliabilityLabel {
        USUALLY_ON_TIME("Usually on time"),
        OCCASIONALLY_DELAYED("Occasionally delayed"),
        FREQUENTLY_DELAYED("Frequently delayed"),
        NOT_ENOUGH_HISTORY("Not enough history");

        private final String display;
        ReliabilityLabel(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    public enum PreferredChannel {
        WHATSAPP("WhatsApp"), EMAIL("Email"), SMS("SMS"), PHONE("Phone");
        private final String display;
        PreferredChannel(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    private String id;
    private String name;
    private String company;
    private String email;
    private String phone;
    private PreferredChannel preferredChannel;
    private int typicalDelayDays;
    private String notes;
    private LocalDateTime createdAt;
    private ReliabilityLabel reliabilityLabel;

    public Client() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.preferredChannel = PreferredChannel.WHATSAPP;
        this.reliabilityLabel = ReliabilityLabel.NOT_ENOUGH_HISTORY;
        this.typicalDelayDays = 0;
    }

    public Client(String name, String company, String email, String phone) {
        this();
        this.name = name;
        this.company = company;
        this.email = email;
        this.phone = phone;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Client name cannot be empty");
        this.name = name.trim();
    }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public PreferredChannel getPreferredChannel() { return preferredChannel; }
    public void setPreferredChannel(PreferredChannel preferredChannel) { this.preferredChannel = preferredChannel; }

    public int getTypicalDelayDays() { return typicalDelayDays; }
    public void setTypicalDelayDays(int days) { this.typicalDelayDays = Math.max(0, days); }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ReliabilityLabel getReliabilityLabel() { return reliabilityLabel; }
    public void setReliabilityLabel(ReliabilityLabel reliabilityLabel) { this.reliabilityLabel = reliabilityLabel; }

    public String getDisplayName() {
        return (company != null && !company.isEmpty()) ? name + " (" + company + ")" : name;
    }

    @Override
    public String toString() { return name; }
}
