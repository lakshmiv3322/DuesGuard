package com.duesguard.service;

import com.duesguard.model.EvidenceDocument;
import com.duesguard.model.Invoice;

import java.util.List;

public class ReadinessScoreService {

    public record ReadinessResult(
        double score,
        boolean hasQuotation,
        boolean invoiceSent,
        boolean hasCompletionProof,
        boolean hasDeliveryProof,
        boolean hasClientAck
    ) {}

    public ReadinessResult calculate(Invoice invoice, List<EvidenceDocument> docs) {
        boolean hasQuotation = false;
        boolean invoiceSent = (invoice.getStatus() != Invoice.Status.DRAFT);
        boolean hasCompletionProof = false;
        boolean hasDeliveryProof = false;
        boolean hasClientAck = false;

        for (EvidenceDocument doc : docs) {
            if (!doc.getInvoiceId().equals(invoice.getId())) continue;
            switch (doc.getType()) {
                case QUOTATION -> hasQuotation = true;
                case WORK_COMPLETION_PROOF -> hasCompletionProof = true;
                case DELIVERY_PROOF -> hasDeliveryProof = true;
                case CLIENT_ACKNOWLEDGEMENT -> hasClientAck = true;
                default -> {}
            }
        }

        // Each criterion = 20 points
        double score = 0;
        if (hasQuotation) score += 20;
        if (invoiceSent) score += 20;
        if (hasCompletionProof) score += 20;
        if (hasDeliveryProof) score += 20;
        if (hasClientAck) score += 20;

        return new ReadinessResult(score, hasQuotation, invoiceSent, hasCompletionProof, hasDeliveryProof, hasClientAck);
    }

    public String getReadinessLabel(double score) {
        if (score >= 100) return "Payment-ready";
        if (score >= 71) return "Almost ready";
        if (score >= 41) return "Needs documents";
        return "Not ready";
    }

    public String getReadinessColor(double score) {
        if (score >= 100) return "#4CAF50";
        if (score >= 71) return "#49E3B4";
        if (score >= 41) return "#FFB84D";
        return "#FF6B6B";
    }
}
