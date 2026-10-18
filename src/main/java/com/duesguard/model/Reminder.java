package com.duesguard.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Reminder implements Exportable {
    public enum Channel { WHATSAPP, EMAIL, SMS, COPY_TEXT }
    public enum Tone { FRIENDLY, PROFESSIONAL, FIRM }
    public enum Status { SCHEDULED, SENT, CANCELLED }
    public enum EscalationLevel { FIRST, SECOND, FINAL, INTERNAL }

    private String id;
    private String invoiceId;
    private String clientId;
    private Channel channel;
    private Tone tone;
    private String message;
    private LocalDate scheduledDate;
    private LocalDateTime sentDate;
    private Status status;
    private EscalationLevel escalationLevel;
    private String notes;

    public Reminder() {
        this.id = UUID.randomUUID().toString();
        this.channel = Channel.WHATSAPP;
        this.tone = Tone.FRIENDLY;
        this.status = Status.SCHEDULED;
        this.escalationLevel = EscalationLevel.FIRST;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public Tone getTone() { return tone; }
    public void setTone(Tone tone) { this.tone = tone; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public EscalationLevel getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(EscalationLevel escalationLevel) { this.escalationLevel = escalationLevel; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toCSV() {
        return String.join(",", id, invoiceId, clientId, channel.name(), tone.name(), status.name());
    }
}
