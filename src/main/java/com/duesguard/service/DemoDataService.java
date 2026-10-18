package com.duesguard.service;

import com.duesguard.model.*;
import com.duesguard.storage.DataStore;
import com.duesguard.util.PasswordUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the application with realistic Indian small-business demo data.
 * Business: Lakshmi Creative Studio
 */
public class DemoDataService {

    public static void seedDemoData(DataStore store) {
        store.clearAll();

        // Business
        Business biz = new Business();
        biz.setName("Lakshmi Creative Studio");
        biz.setOwnerName("Lakshmi V.");
        biz.setType("Design and creative services");
        biz.setEmail("lakshmi@lakshmicreative.in");
        biz.setPhone("+91 98765 43210");
        biz.setCity("Chennai");
        biz.setCurrency("INR");
        biz.setDefaultPaymentTerms(30);
        store.setBusiness(biz);

        // Demo user
        AppUser user = new AppUser("lakshmi", "lakshmi@lakshmicreative.in", PasswordUtil.hash("demo123"));
        user.setOnboardingCompleted(true);
        store.addUser(user);

        // ---- Clients ----
        Client arun = new Client("Arun Design Studio", "Arun Design Studio", "arun@arundesign.in", "+91 98765 11111");
        arun.setTypicalDelayDays(9);
        arun.setReliabilityLabel(Client.ReliabilityLabel.FREQUENTLY_DELAYED);
        arun.setPreferredChannel(Client.PreferredChannel.WHATSAPP);
        arun.setNotes("Usually pays after 2nd reminder. Prefers WhatsApp communication.");
        store.addClient(arun);

        Client bright = new Client("Bright Academy", "Bright Academy", "accounts@brightacademy.edu", "+91 98765 22222");
        bright.setTypicalDelayDays(3);
        bright.setReliabilityLabel(Client.ReliabilityLabel.OCCASIONALLY_DELAYED);
        bright.setPreferredChannel(Client.PreferredChannel.EMAIL);
        bright.setNotes("Reliable payer. Minor delays due to internal approval process.");
        store.addClient(bright);

        Client greenleaf = new Client("GreenLeaf Events", "GreenLeaf Events", "billing@greenleafevents.in", "+91 98765 33333");
        greenleaf.setTypicalDelayDays(12);
        greenleaf.setReliabilityLabel(Client.ReliabilityLabel.FREQUENTLY_DELAYED);
        greenleaf.setPreferredChannel(Client.PreferredChannel.WHATSAPP);
        greenleaf.setNotes("Large client. Often delays due to internal procurement approvals. Always pays eventually.");
        store.addClient(greenleaf);

        Client nova = new Client("Nova Repairs", "Nova Repairs", "nova@novarepairs.in", "+91 98765 44444");
        nova.setTypicalDelayDays(1);
        nova.setReliabilityLabel(Client.ReliabilityLabel.USUALLY_ON_TIME);
        nova.setPreferredChannel(Client.PreferredChannel.WHATSAPP);
        nova.setNotes("Excellent payment track record. Usually pays same day or next day.");
        store.addClient(nova);

        LocalDate today = LocalDate.now();

        // ---- Invoices ----
        // INV-101: ₹24,500, 18 days overdue, Arun
        ServiceInvoice inv101 = new ServiceInvoice("INV-101", arun.getId(), "Brand Identity Design");
        inv101.getLineItems().add(new LineItem("Logo design", 1, 15000));
        inv101.getLineItems().add(new LineItem("Brand guidelines", 1, 9500));
        inv101.setIssueDate(today.minusDays(33));
        inv101.setDueDate(today.minusDays(18));
        inv101.setStatus(Invoice.Status.OVERDUE);
        inv101.setPaymentTerms(15);
        inv101.setReadinessScore(60.0);  // missing completion proof
        store.addInvoice(inv101);

        // INV-102: ₹18,000, due in 3 days, Bright Academy
        ServiceInvoice inv102 = new ServiceInvoice("INV-102", bright.getId(), "Training Material Design");
        inv102.getLineItems().add(new LineItem("Presentation design", 6, 2000));
        inv102.getLineItems().add(new LineItem("Workbook layout", 3, 2000));
        inv102.setIssueDate(today.minusDays(27));
        inv102.setDueDate(today.plusDays(3));
        inv102.setStatus(Invoice.Status.SENT);
        inv102.setPaymentTerms(30);
        inv102.setReadinessScore(80.0);
        store.addInvoice(inv102);

        // INV-103: ₹42,000, 24 days overdue, GreenLeaf
        ServiceInvoice inv103 = new ServiceInvoice("INV-103", greenleaf.getId(), "Event Branding Package");
        inv103.getLineItems().add(new LineItem("Event signage design", 1, 18000));
        inv103.getLineItems().add(new LineItem("Social media graphics", 10, 1500));
        inv103.getLineItems().add(new LineItem("Print artwork", 1, 9000));
        inv103.setIssueDate(today.minusDays(54));
        inv103.setDueDate(today.minusDays(24));
        inv103.setStatus(Invoice.Status.OVERDUE);
        inv103.setPaymentTerms(30);
        inv103.setReadinessScore(40.0);  // missing proof
        store.addInvoice(inv103);

        // INV-104: ₹12,000 total, ₹8,000 paid, balance ₹4,000, Bright Academy
        ServiceInvoice inv104 = new ServiceInvoice("INV-104", bright.getId(), "Social Media Content Pack");
        inv104.getLineItems().add(new LineItem("Social media posts (16 posts)", 16, 750));
        inv104.setIssueDate(today.minusDays(20));
        inv104.setDueDate(today.minusDays(5));
        inv104.setAmountPaid(8000);
        inv104.setStatus(Invoice.Status.PARTIALLY_PAID);
        inv104.setPaymentTerms(15);
        inv104.setReadinessScore(80.0);
        store.addInvoice(inv104);

        // INV-105: ₹4,800, PAID, Nova Repairs
        ServiceInvoice inv105 = new ServiceInvoice("INV-105", nova.getId(), "Business Card & Letterhead Design");
        inv105.getLineItems().add(new LineItem("Business card design", 1, 2500));
        inv105.getLineItems().add(new LineItem("Letterhead design", 1, 2300));
        inv105.setIssueDate(today.minusDays(15));
        inv105.setDueDate(today.minusDays(1));
        inv105.setAmountPaid(4800);
        inv105.setStatus(Invoice.Status.PAID);
        inv105.setPaymentTerms(14);
        inv105.setReadinessScore(100.0);
        store.addInvoice(inv105);

        // INV-106: ₹30,000, due in 12 days, GreenLeaf
        ServiceInvoice inv106 = new ServiceInvoice("INV-106", greenleaf.getId(), "Website Redesign (Phase 1)");
        inv106.getLineItems().add(new LineItem("UI/UX design", 1, 20000));
        inv106.getLineItems().add(new LineItem("Style guide", 1, 10000));
        inv106.setIssueDate(today.minusDays(18));
        inv106.setDueDate(today.plusDays(12));
        inv106.setStatus(Invoice.Status.SENT);
        inv106.setPaymentTerms(30);
        inv106.setReadinessScore(60.0);
        store.addInvoice(inv106);

        // ---- Payments ----
        // INV-104 partial payment
        Payment p1 = new Payment(inv104.getId(), bright.getId(), 8000, Payment.Method.UPI);
        p1.setPaymentDate(today.minusDays(10));
        p1.setReference("UPI-BRT-" + today.minusDays(10).toString().replace("-", ""));
        p1.setNotes("Partial payment received via UPI");
        p1.setRecordedBy("Lakshmi V.");
        store.addPayment(p1);

        // INV-105 full payment
        Payment p2 = new Payment(inv105.getId(), nova.getId(), 4800, Payment.Method.BANK_TRANSFER);
        p2.setPaymentDate(today.minusDays(2));
        p2.setReference("NEFT-NOV-" + today.minusDays(2).toString().replace("-", ""));
        p2.setNotes("Full payment via NEFT");
        p2.setRecordedBy("Lakshmi V.");
        store.addPayment(p2);

        // Previous payment from Arun (older invoice)
        Payment p3 = new Payment(inv101.getId(), arun.getId(), 5000, Payment.Method.CASH);
        p3.setPaymentDate(today.minusDays(45));
        p3.setNotes("Advance payment received in cash");
        p3.setRecordedBy("Lakshmi V.");
        // Not actually applying to inv101 balance for demo purposes

        // ---- Expenses ----
        Expense rent = new Expense(Expense.Category.RENT, "Studio rent - October", 12000, today.plusDays(5));
        rent.setRecurring(true);
        store.addExpense(rent);

        Expense software = new Expense(Expense.Category.OTHER, "Adobe Creative Cloud", 4500, today.plusDays(10));
        software.setRecurring(true);
        store.addExpense(software);

        Expense salary = new Expense(Expense.Category.SALARY, "Intern stipend - October", 8000, today.plusDays(12));
        store.addExpense(salary);

        // ---- Evidence Documents ----
        // inv101: has quotation, no completion proof
        EvidenceDocument doc1 = new EvidenceDocument(inv101.getId(), EvidenceDocument.DocType.QUOTATION, "quotation-arun-101.pdf", "/demo/quotation-arun-101.pdf");
        doc1.setVerified(true);
        store.addDocument(doc1);

        // inv104: has quotation + invoice sent
        EvidenceDocument doc2 = new EvidenceDocument(inv104.getId(), EvidenceDocument.DocType.QUOTATION, "quotation-bright-104.pdf", "/demo/quotation-bright-104.pdf");
        doc2.setVerified(true);
        store.addDocument(doc2);

        EvidenceDocument doc3 = new EvidenceDocument(inv104.getId(), EvidenceDocument.DocType.WORK_COMPLETION_PROOF, "completion-proof-104.pdf", "/demo/completion-proof-104.pdf");
        doc3.setVerified(true);
        store.addDocument(doc3);

        // inv105: fully documented
        EvidenceDocument doc4 = new EvidenceDocument(inv105.getId(), EvidenceDocument.DocType.QUOTATION, "quotation-nova-105.pdf", "/demo/quotation-nova-105.pdf");
        doc4.setVerified(true);
        store.addDocument(doc4);

        EvidenceDocument doc5 = new EvidenceDocument(inv105.getId(), EvidenceDocument.DocType.WORK_COMPLETION_PROOF, "completion-nova-105.pdf", "/demo/completion-nova-105.pdf");
        doc5.setVerified(true);
        store.addDocument(doc5);

        // ---- Follow-up Logs ----
        FollowUpLog log1 = new FollowUpLog(inv101.getId(), "WhatsApp", "Sent reminder");
        log1.setDate(today.minusDays(10));
        log1.setOutcome("No response");
        log1.setNotes("First reminder sent via WhatsApp");
        store.addFollowUpLog(log1);

        FollowUpLog log2 = new FollowUpLog(inv103.getId(), "WhatsApp", "Sent reminder");
        log2.setDate(today.minusDays(15));
        log2.setOutcome("Client acknowledged, promised payment");
        log2.setNotes("Client said will pay by end of week, but did not");
        store.addFollowUpLog(log2);

        store.setCurrentUser(user);
    }
}
