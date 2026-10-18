package com.duesguard;

import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.model.LineItem;
import com.duesguard.model.ServiceInvoice;
import com.duesguard.service.PriorityScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Priority Score Calculation Tests")
public class PriorityScoreTest {
    private PriorityScoreService service;
    private Client client;

    @BeforeEach
    void setUp() {
        service = new PriorityScoreService();
        client = new Client("Test Studio", "Test Co", "test@test.com", "9876543210");
        client.setTypicalDelayDays(9);
    }

    @Test
    @DisplayName("Should calculate score between 0 and 100 for overdue invoice")
    void testScoreRange() {
        ServiceInvoice inv = new ServiceInvoice("INV-001", client.getId(), "Design Services");
        inv.getLineItems().add(new LineItem("Branding", 1, 24500));
        inv.setIssueDate(LocalDate.now().minusDays(33));
        inv.setDueDate(LocalDate.now().minusDays(18));
        inv.setReadinessScore(60.0);

        double score = service.calculate(inv, client);
        assertTrue(score >= 0.0 && score <= 100.0, "Score should be bounded in [0, 100]");
        assertTrue(score > 40.0, "Overdue invoice with balance should receive high priority");
    }

    @Test
    @DisplayName("Should rank higher balance invoices with higher score")
    void testAmountWeight() {
        ServiceInvoice lowAmt = new ServiceInvoice("INV-LOW", client.getId(), "Small task");
        lowAmt.getLineItems().add(new LineItem("Icon", 1, 2000));
        lowAmt.setIssueDate(LocalDate.now().minusDays(25));
        lowAmt.setDueDate(LocalDate.now().minusDays(10));
        lowAmt.setReadinessScore(60.0);

        ServiceInvoice highAmt = new ServiceInvoice("INV-HIGH", client.getId(), "Large project");
        highAmt.getLineItems().add(new LineItem("Website", 1, 80000));
        highAmt.setIssueDate(LocalDate.now().minusDays(25));
        highAmt.setDueDate(LocalDate.now().minusDays(10));
        highAmt.setReadinessScore(60.0);

        double lowScore = service.calculate(lowAmt, client);
        double highScore = service.calculate(highAmt, client);

        assertTrue(highScore > lowScore, "Higher amount invoice should yield higher priority score");
    }

    @Test
    @DisplayName("Should increase score for clients with frequent historical delays")
    void testClientBehaviourWeight() {
        Client promptClient = new Client("Prompt Corp", "Prompt", "p@p.com", "123");
        promptClient.setTypicalDelayDays(0);

        Client slowClient = new Client("Slow Corp", "Slow", "s@s.com", "456");
        slowClient.setTypicalDelayDays(15);

        ServiceInvoice inv = new ServiceInvoice("INV-COMMON", "test", "Service");
        inv.getLineItems().add(new LineItem("Task", 1, 20000));
        inv.setIssueDate(LocalDate.now().minusDays(20));
        inv.setDueDate(LocalDate.now().minusDays(5));
        inv.setReadinessScore(80.0);

        double promptScore = service.calculate(inv, promptClient);
        double slowScore = service.calculate(inv, slowClient);

        assertTrue(slowScore > promptScore, "Frequently delayed client should increase urgency");
    }

    @Test
    @DisplayName("Score breakdown should sum up to total score")
    void testScoreBreakdownConsistency() {
        ServiceInvoice inv = new ServiceInvoice("INV-BD", client.getId(), "Audit");
        inv.getLineItems().add(new LineItem("Audit", 1, 35000));
        inv.setIssueDate(LocalDate.now().minusDays(27));
        inv.setDueDate(LocalDate.now().minusDays(12));
        inv.setReadinessScore(50.0);

        PriorityScoreService.ScoreBreakdown bd = service.calculateBreakdown(inv, client);
        double sum = bd.amountScore() + bd.daysScore() + bd.clientScore() + bd.evidenceScore() + bd.reminderScore();

        assertEquals(Math.min(100.0, sum), bd.total(), 0.01, "Breakdown components should sum to total");
    }
}
