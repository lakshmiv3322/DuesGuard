package com.duesguard;

import com.duesguard.model.Invoice;
import com.duesguard.model.LineItem;
import com.duesguard.model.ServiceInvoice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Overdue Calculation Tests")
public class OverdueCalculationTest {

    @Test
    @DisplayName("Invoice with past due date should calculate positive days overdue")
    void testOverdueDays() {
        ServiceInvoice inv = new ServiceInvoice("INV-OD", "c1", "SEO Package");
        inv.getLineItems().add(new LineItem("Monthly SEO", 1, 15000));
        inv.setIssueDate(LocalDate.now().minusDays(33));
        inv.setDueDate(LocalDate.now().minusDays(18));

        assertEquals(18, inv.getDaysOverdue());
        assertEquals(0, inv.getDaysUntilDue());

        inv.updateStatusAutomatically();
        assertEquals(Invoice.Status.OVERDUE, inv.getStatus());
    }

    @Test
    @DisplayName("Invoice with future due date should have 0 days overdue and correct days until due")
    void testFutureDueDate() {
        ServiceInvoice inv = new ServiceInvoice("INV-FUT", "c1", "App Design");
        inv.getLineItems().add(new LineItem("UI Design", 1, 20000));
        inv.setDueDate(LocalDate.now().plusDays(10));

        assertEquals(0, inv.getDaysOverdue());
        assertEquals(10, inv.getDaysUntilDue());

        inv.updateStatusAutomatically();
        assertEquals(Invoice.Status.PENDING, inv.getStatus());
    }

    @Test
    @DisplayName("Invoice due today should have 0 days overdue and 0 days until due")
    void testDueToday() {
        ServiceInvoice inv = new ServiceInvoice("INV-TODAY", "c1", "Hosting");
        inv.getLineItems().add(new LineItem("Annual", 1, 6000));
        inv.setDueDate(LocalDate.now());

        assertEquals(0, inv.getDaysOverdue());
        assertEquals(0, inv.getDaysUntilDue());
    }

    @Test
    @DisplayName("Setting due date before issue date should be rejected")
    void testInvalidDueDate() {
        ServiceInvoice inv = new ServiceInvoice("INV-INVLD", "c1", "Maintenance");
        inv.setIssueDate(LocalDate.now());

        assertThrows(IllegalArgumentException.class, () ->
            inv.setDueDate(LocalDate.now().minusDays(5))
        );
    }
}
