package com.duesguard.model;

public class ServiceInvoice extends Invoice {
    private String projectCode;
    private String deliverable;

    public ServiceInvoice() { super(); }

    public ServiceInvoice(String invoiceNumber, String clientId, String serviceTitle) {
        super();
        setInvoiceNumber(invoiceNumber);
        setClientId(clientId);
        setServiceTitle(serviceTitle);
    }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getDeliverable() { return deliverable; }
    public void setDeliverable(String deliverable) { this.deliverable = deliverable; }

    @Override
    public double calculatePriorityScore(Client client) {
        double score = 0;
        double balance = getBalance();
        long daysOver = getDaysOverdue();

        // Amount weight: 35 points
        double amountScore = Math.min(35.0, (balance / 50000.0) * 35.0);

        // Days overdue weight: 25 points
        double daysScore = Math.min(25.0, (daysOver / 30.0) * 25.0);

        // Client behaviour weight: 20 points
        double clientScore = 0;
        if (client != null) {
            int delay = client.getTypicalDelayDays();
            if (delay > 10) clientScore = 20;
            else if (delay > 5) clientScore = 14;
            else if (delay > 2) clientScore = 8;
            else clientScore = 4;
        }

        // Evidence gap: 10 points (will be set by service)
        double evidenceScore = (getReadinessScore() < 70) ? 10 : 3;

        // Previous reminder response: 10 points (default - no response = 10)
        double reminderScore = 10;

        score = amountScore + daysScore + clientScore + evidenceScore + reminderScore;
        return Math.min(100.0, Math.max(0.0, score));
    }

    @Override
    public String toCSV() {
        return String.join(",",
            getId(), getInvoiceNumber(), getClientId(), getServiceTitle(),
            String.valueOf(getTotal()), String.valueOf(getAmountPaid()),
            String.valueOf(getBalance()), getStatus().name(),
            getIssueDate() != null ? getIssueDate().toString() : "",
            getDueDate() != null ? getDueDate().toString() : "",
            "SERVICE"
        );
    }
}
