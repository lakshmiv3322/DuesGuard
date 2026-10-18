package com.duesguard.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EvidenceDocument implements Exportable {
    public enum DocType {
        QUOTATION("Quotation"),
        INVOICE("Invoice"),
        WORK_COMPLETION_PROOF("Work completion proof"),
        DELIVERY_PROOF("Delivery proof"),
        CLIENT_ACKNOWLEDGEMENT("Client acknowledgement"),
        PURCHASE_ORDER("Purchase order"),
        CONTRACT("Contract"),
        OTHER("Other");

        private final String display;
        DocType(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    private String id;
    private String invoiceId;
    private DocType type;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadedAt;
    private boolean verified;

    public EvidenceDocument() {
        this.id = UUID.randomUUID().toString();
        this.uploadedAt = LocalDateTime.now();
        this.verified = false;
    }

    public EvidenceDocument(String invoiceId, DocType type, String fileName, String filePath) {
        this();
        this.invoiceId = invoiceId;
        this.type = type;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public DocType getType() { return type; }
    public void setType(DocType type) { this.type = type; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    @Override
    public String toCSV() {
        return String.join(",", id, invoiceId, type.name(), fileName, filePath, uploadedAt.toString(), String.valueOf(verified));
    }
}
