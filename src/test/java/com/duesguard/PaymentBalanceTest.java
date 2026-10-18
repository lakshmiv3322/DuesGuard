package com.duesguard;

import com.duesguard.model.Invoice;
import com.duesguard.model.LineItem;
import com.duesguard.model.ServiceInvoice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment & Balance Calculation Tests")
public class PaymentBalanceTest {

    @Test
    @DisplayName("Should correctly calculate subtotal, discount, and tax")
    void testTotalCalculation() {
        ServiceInvoice inv = new ServiceInvoice("INV-001", "c1", "Web Development");
        inv.getLineItems().add(new LineItem("Frontend", 1, 30000));
        inv.getLineItems().add(new LineItem("Backend", 1, 20000));
        inv.setDiscount(10.0); // 10% discount on 50,000 = 5,000 off -> 45,000
        inv.setTaxPercent(18.0); // 18% GST on 45,000 = 8,100 -> Total 53,100

        assertEquals(50000.0, inv.getSubtotal(), 0.01);
        assertEquals(5000.0, inv.getDiscountAmount(), 0.01);
        assertEquals(8100.0, inv.getTaxAmount(), 0.01);
        assertEquals(53100.0, inv.getTotal(), 0.01);
        assertEquals(53100.0, inv.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Recording partial payment should update balance and change status to PARTIALLY_PAID")
    void testPartialPayment() {
        ServiceInvoice inv = new ServiceInvoice("INV-002", "c1", "Design");
        inv.getLineItems().add(new LineItem("Logo", 1, 10000));

        inv.recordPayment(4000.0);

        assertEquals(4000.0, inv.getAmountPaid(), 0.01);
        assertEquals(6000.0, inv.getBalance(), 0.01);
        assertEquals(Invoice.Status.PARTIALLY_PAID, inv.getStatus());
    }

    @Test
    @DisplayName("Recording full payment should set balance to zero and status to PAID")
    void testFullPayment() {
        ServiceInvoice inv = new ServiceInvoice("INV-003", "c1", "Consulting");
        inv.getLineItems().add(new LineItem("Review", 1, 15000));

        inv.recordPayment(15000.0);

        assertEquals(15000.0, inv.getAmountPaid(), 0.01);
        assertEquals(0.0, inv.getBalance(), 0.01);
        assertEquals(Invoice.Status.PAID, inv.getStatus());
    }

    @Test
    @DisplayName("Recording payment exceeding balance should throw IllegalArgumentException")
    void testOverpaymentPrevention() {
        ServiceInvoice inv = new ServiceInvoice("INV-004", "c1", "Copywriting");
        inv.getLineItems().add(new LineItem("Article", 1, 5000));

        assertThrows(IllegalArgumentException.class, () -> inv.recordPayment(6000.0),
            "Cannot pay more than the outstanding balance");
    }

    @Test
    @DisplayName("Negative or zero payments should be rejected")
    void testInvalidPaymentAmounts() {
        ServiceInvoice inv = new ServiceInvoice("INV-005", "c1", "Photo Shoot");
        inv.getLineItems().add(new LineItem("Shoot", 1, 10000));

        assertThrows(IllegalArgumentException.class, () -> inv.recordPayment(0));
        assertThrows(IllegalArgumentException.class, () -> inv.recordPayment(-500));
    }
}
