package com.duesguard.service;

import com.duesguard.model.Client;
import com.duesguard.model.Invoice;

import java.util.LinkedHashMap;
import java.util.Map;

public class PriorityScoreService {

    public record ScoreBreakdown(
        double amountScore,
        double daysScore,
        double clientScore,
        double evidenceScore,
        double reminderScore,
        double total
    ) {}

    /**
     * Calculate priority score (0-100) for an overdue invoice.
     * Formula:
     *   Amount due:           35%
     *   Days overdue:         25%
     *   Client behaviour:     20%
     *   Missing evidence:     10%
     *   Reminder response:    10%
     */
    public ScoreBreakdown calculateBreakdown(Invoice invoice, Client client) {
        double balance = invoice.getBalance();
        long daysOver = invoice.getDaysOverdue();

        // Amount score: scales up to ₹1,00,000 = 35 pts
        double amountScore = Math.min(35.0, (balance / 100000.0) * 35.0);

        // Days overdue score: 30 days = full 25 pts
        double daysScore = Math.min(25.0, (daysOver / 30.0) * 25.0);

        // Client behaviour score: based on typicalDelayDays
        double clientScore = 0;
        if (client != null) {
            int delay = client.getTypicalDelayDays();
            if (delay > 14) clientScore = 20;
            else if (delay > 7) clientScore = 15;
            else if (delay > 3) clientScore = 10;
            else if (delay > 0) clientScore = 5;
            else clientScore = 2;
        } else {
            clientScore = 10; // unknown
        }

        // Evidence gap: if readiness < 70% = 10, else 3
        double evidenceScore = (invoice.getReadinessScore() < 70) ? 10.0 : 3.0;

        // Reminder response: default = 10 (no response recorded)
        double reminderScore = 10.0;

        double total = Math.min(100.0, amountScore + daysScore + clientScore + evidenceScore + reminderScore);

        return new ScoreBreakdown(amountScore, daysScore, clientScore, evidenceScore, reminderScore, total);
    }

    public double calculate(Invoice invoice, Client client) {
        return calculateBreakdown(invoice, client).total();
    }

    public String getPriorityLabel(double score) {
        if (score >= 80) return "Critical";
        if (score >= 60) return "High";
        if (score >= 40) return "Medium";
        return "Low";
    }

    public String getPriorityColor(double score) {
        if (score >= 80) return "#FF6B6B";
        if (score >= 60) return "#FFB84D";
        if (score >= 40) return "#7C83FF";
        return "#49E3B4";
    }
}
