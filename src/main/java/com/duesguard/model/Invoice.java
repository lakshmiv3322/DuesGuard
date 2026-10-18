package com.duesguard.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Invoice implements Prioritizable, Exportable {

    public enum Status {
        DRAFT("Draft", "#9BA8BC"),
        SENT("Sent", "#7C83FF"),
        PENDING("Pending", "#FFB84D"),
        PARTIALLY_PAID("Partially paid", "#49E3B4"),
        PAID("Paid", "#4CAF50"),
        OVERDUE("Overdue", "#FF6B6B"),
        DISPUTED("Disputed", "#FF9800");

        private final String display;
        private final String color;
        Status(String display, String color) { this.display = display; this.color = color; }
        public String getDisplay() { return display; }
        public String getColor() { return color; }
    }

    private String id;
    private String invoiceNumber;
    private String clientId;
    private String serviceTitle;
    private String description;
    private List<LineItem> lineItems;
    private double discount;
    private double taxPercent;
    private double amountPaid;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Status status;
    private int paymentTerms;
    private double priorityScore;
    private double readinessScore;
    private LocalDateTime createdAt;
    private String notes;
    private boolean archived;

    public Invoice() {
        this.id = UUID.randomUUID().toString();
        this.lineItems = new ArrayList<>();
        this.issueDate = LocalDate.now();
        this.status = Status.DRAFT;
        this.paymentTerms = 30;
        this.createdAt = LocalDateTime.now();
        this.amountPaid = 0.0;
        this.discount = 0.0;
        this.taxPercent = 0.0;
        this.archived = false;
    }

    // Computed fields
    public double getSubtotal() {
        return lineItems.stream().mapToDouble(LineItem::getAmount).sum();
    }

    public double getDiscountAmount() {
        return getSubtotal() * (discount / 100.0);
    }

    public double getTaxAmount() {
        return (getSubtotal() - getDiscountAmount()) * (taxPercent / 100.0);
    }

    public double getTotal() {
        return getSubtotal() - getDiscountAmount() + getTaxAmount();
    }

    public double getBalance() {
        return Math.max(0.0, getTotal() - amountPaid);
    }

    public long getDaysOverdue() {
        if (dueDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, today);
        }
        return 0;
    }

    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isBefore(dueDate)) {
            return ChronoUnit.DAYS.between(today, dueDate);
        }
        return 0;
    }

    public void updateStatusAutomatically() {
        if (amountPaid >= getTotal() && getTotal() > 0) {
            status = Status.PAID;
        } else if (amountPaid > 0) {
            status = Status.PARTIALLY_PAID;
        } else if (getDaysOverdue() > 0) {
            status = Status.OVERDUE;
        } else {
            status = Status.PENDING;
        }
    }

    public void recordPayment(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Payment amount must be positive");
        if (amount > getBalance() + 0.01) throw new IllegalArgumentException("Payment exceeds outstanding balance");
        this.amountPaid = Math.min(this.amountPaid + amount, getTotal());
        updateStatusAutomatically();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<LineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<LineItem> lineItems) { this.lineItems = lineItems; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = Math.max(0, Math.min(100, discount)); }

    public double getTaxPercent() { return taxPercent; }
    public void setTaxPercent(double taxPercent) { this.taxPercent = Math.max(0, taxPercent); }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = Math.max(0, amountPaid); }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) {
        if (dueDate != null && issueDate != null && dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Due date cannot be before issue date");
        }
        this.dueDate = dueDate;
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(int paymentTerms) { this.paymentTerms = paymentTerms; }

    public double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(double priorityScore) { this.priorityScore = priorityScore; }

    public double getReadinessScore() { return readinessScore; }
    public void setReadinessScore(double readinessScore) { this.readinessScore = readinessScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    @Override
    public String toString() {
        return invoiceNumber + " - " + serviceTitle + " (" + status.getDisplay() + ")";
    }
}
