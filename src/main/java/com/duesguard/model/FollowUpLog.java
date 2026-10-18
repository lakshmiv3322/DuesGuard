package com.duesguard.model;

import java.time.LocalDate;
import java.util.UUID;

public class FollowUpLog implements Exportable {
    private String id;
    private String invoiceId;
    private LocalDate date;
    private String channel;
    private String action;
    private String outcome;
    private String notes;

    public FollowUpLog() {
        this.id = UUID.randomUUID().toString();
        this.date = LocalDate.now();
    }

    public FollowUpLog(String invoiceId, String channel, String action) {
        this();
        this.invoiceId = invoiceId;
        this.channel = channel;
        this.action = action;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toCSV() {
        return String.join(",", id, invoiceId, date.toString(), channel, action,
            outcome != null ? outcome : "",
            notes != null ? notes : "");
    }
}
