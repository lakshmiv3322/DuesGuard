package com.duesguard.service;

import com.duesguard.model.Client;
import com.duesguard.model.Expense;
import com.duesguard.model.Invoice;
import com.duesguard.model.Payment;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CashFlowService {

    public record CashFlowSummary(
        double expectedThisMonth,
        double receivedThisMonth,
        double totalOutstanding,
        double overdueAmount,
        double predictedGap
    ) {}

    public record Scenario(
        String name,
        double projectedBalance,
        String description
    ) {}

    public CashFlowSummary getSummary(List<Invoice> invoices, List<Payment> payments) {
        YearMonth thisMonth = YearMonth.now();
        LocalDate start = thisMonth.atDay(1);
        LocalDate end = thisMonth.atEndOfMonth();

        double expected = invoices.stream()
            .filter(i -> !i.isArchived())
            .filter(i -> i.getDueDate() != null &&
                !i.getDueDate().isBefore(start) && !i.getDueDate().isAfter(end))
            .mapToDouble(Invoice::getBalance)
            .sum();

        double received = payments.stream()
            .filter(p -> p.getPaymentDate() != null &&
                !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end))
            .mapToDouble(Payment::getAmount)
            .sum();

        double outstanding = invoices.stream()
            .filter(i -> !i.isArchived())
            .filter(i -> i.getStatus() != Invoice.Status.PAID && i.getStatus() != Invoice.Status.DRAFT)
            .mapToDouble(Invoice::getBalance)
            .sum();

        double overdue = invoices.stream()
            .filter(i -> !i.isArchived())
            .filter(i -> i.getDaysOverdue() > 0)
            .mapToDouble(Invoice::getBalance)
            .sum();

        double gap = expected - received;

        return new CashFlowSummary(expected, received, outstanding, overdue, gap);
    }

    public List<Scenario> getScenarios(List<Invoice> invoices, List<Payment> payments, List<Expense> expenses, List<Client> clients) {
        Map<String, Client> clientMap = clients.stream()
            .collect(Collectors.toMap(Client::getId, c -> c));

        double totalExpenses = expenses.stream()
            .filter(e -> e.getStatus() == Expense.Status.UPCOMING)
            .mapToDouble(Expense::getAmount)
            .sum();

        double overdueInvoices = invoices.stream()
            .filter(i -> i.getDaysOverdue() > 0)
            .mapToDouble(Invoice::getBalance)
            .sum();

        double allOutstanding = invoices.stream()
            .filter(i -> i.getStatus() != Invoice.Status.PAID && i.getStatus() != Invoice.Status.DRAFT)
            .mapToDouble(Invoice::getBalance)
            .sum();

        // Best case: all invoices paid near due date
        Scenario best = new Scenario(
            "Best case",
            allOutstanding - totalExpenses,
            "All outstanding invoices received near their due dates."
        );

        // Likely case: apply average client delay
        double likelyInflow = invoices.stream()
            .filter(i -> i.getStatus() != Invoice.Status.PAID && i.getStatus() != Invoice.Status.DRAFT)
            .mapToDouble(i -> {
                Client c = clientMap.get(i.getClientId());
                int delay = c != null ? c.getTypicalDelayDays() : 7;
                // Reduce expected inflow by delay factor
                return i.getBalance() * (1.0 - Math.min(0.4, delay / 60.0));
            })
            .sum();

        Scenario likely = new Scenario(
            "Likely case",
            likelyInflow - totalExpenses,
            "Applying recorded client payment delays to outstanding invoices."
        );

        // Conservative: overdue invoices delayed further by 14 days
        double conservativeInflow = allOutstanding - overdueInvoices * 0.5;
        Scenario conservative = new Scenario(
            "Conservative case",
            conservativeInflow - totalExpenses,
            "Overdue invoices delayed by 14+ additional days."
        );

        return List.of(best, likely, conservative);
    }
}
