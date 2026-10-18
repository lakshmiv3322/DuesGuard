package com.duesguard.model;

import java.time.LocalDate;
import java.util.UUID;

public class Expense implements Exportable {
    public enum Category {
        RENT("Rent"), SALARY("Salary"), SUPPLIES("Supplies"), TAX("Tax"), UTILITIES("Utilities"), OTHER("Other");
        private final String display;
        Category(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    public enum Status { UPCOMING, PAID, OVERDUE }

    private String id;
    private Category category;
    private String description;
    private double amount;
    private LocalDate dueDate;
    private boolean recurring;
    private Status status;

    public Expense() {
        this.id = UUID.randomUUID().toString();
        this.status = Status.UPCOMING;
        this.recurring = false;
    }

    public Expense(Category category, String description, double amount, LocalDate dueDate) {
        this();
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toCSV() {
        return String.join(",", id, category.name(), description, String.valueOf(amount), dueDate.toString(), String.valueOf(recurring), status.name());
    }
}
