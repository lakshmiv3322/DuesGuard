package com.duesguard.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Payment implements Exportable {
    public enum Method {
        UPI("UPI"), BANK_TRANSFER("Bank transfer"), CASH("Cash"), CARD("Card"), OTHER("Other");
        private final String display;
        Method(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    private String id;
    private String invoiceId;
    private String clientId;
    private double amount;
    private LocalDate paymentDate;
    private Method method;
    private String reference;
    private String notes;
    private String recordedBy;
    private LocalDateTime createdAt;

    public Payment() {
        this.id = UUID.randomUUID().toString();
        this.paymentDate = LocalDate.now();
        this.method = Method.UPI;
        this.createdAt = LocalDateTime.now();
    }

    public Payment(String invoiceId, String clientId, double amount, Method method) {
        this();
        this.invoiceId = invoiceId;
        this.clientId = clientId;
        if (amount <= 0) throw new IllegalArgumentException("Payment amount must be positive");
        this.amount = amount;
        this.method = method;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.amount = amount;
    }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toCSV() {
        return String.join(",",
            id, invoiceId, clientId, String.valueOf(amount),
            paymentDate != null ? paymentDate.toString() : "",
            method.name(),
            reference != null ? reference : "",
            notes != null ? notes : ""
        );
    }
}
