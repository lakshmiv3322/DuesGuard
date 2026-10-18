package com.duesguard.model;

public class ProductInvoice extends Invoice {
    private String productCategory;
    private String deliveryAddress;

    public ProductInvoice() { super(); }

    public ProductInvoice(String invoiceNumber, String clientId, String serviceTitle) {
        super();
        setInvoiceNumber(invoiceNumber);
        setClientId(clientId);
        setServiceTitle(serviceTitle);
    }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @Override
    public double calculatePriorityScore(Client client) {
        double balance = getBalance();
        long daysOver = getDaysOverdue();
        double amountScore = Math.min(35.0, (balance / 50000.0) * 35.0);
        double daysScore = Math.min(25.0, (daysOver / 30.0) * 25.0);
        double clientScore = (client != null && client.getTypicalDelayDays() > 7) ? 18 : 8;
        double evidenceScore = (getReadinessScore() < 70) ? 10 : 3;
        double reminderScore = 10;
        return Math.min(100.0, amountScore + daysScore + clientScore + evidenceScore + reminderScore);
    }

    @Override
    public String toCSV() {
        return String.join(",",
            getId(), getInvoiceNumber(), getClientId(), getServiceTitle(),
            String.valueOf(getTotal()), String.valueOf(getAmountPaid()),
            String.valueOf(getBalance()), getStatus().name(),
            getIssueDate() != null ? getIssueDate().toString() : "",
            getDueDate() != null ? getDueDate().toString() : "",
            "PRODUCT"
        );
    }
}
