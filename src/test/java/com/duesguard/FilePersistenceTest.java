package com.duesguard;

import com.duesguard.model.*;
import com.duesguard.service.DemoDataService;
import com.duesguard.storage.DataStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("File Save & Load Persistence Round-Trip Tests")
public class FilePersistenceTest {

    @Test
    @DisplayName("Should successfully seed, persist, and retrieve demo data")
    void testDemoDataRoundTrip() {
        DataStore store = DataStore.getInstance();
        DemoDataService.seedDemoData(store);

        // Verify in-memory seed
        assertEquals("Lakshmi Creative Studio", store.getBusiness().getName());
        assertEquals(4, store.getClients().size());
        assertEquals(6, store.getInvoices().size());
        assertFalse(store.getPayments().isEmpty());

        // Perform file persistence save
        assertDoesNotThrow(store::saveAll, "Saving all entities to JSON should not throw");

        // Verify retrieval of entities
        Invoice inv101 = store.findByInvoiceNumber("INV-101").orElse(null);
        assertNotNull(inv101, "INV-101 should exist in store");
        assertEquals(24500.0, inv101.getTotal(), 0.01);
        assertEquals(24500.0, inv101.getBalance(), 0.01);

        // Verify unique invoice number check
        assertFalse(store.isInvoiceNumberUnique("INV-101", "some-other-id"));
        assertTrue(store.isInvoiceNumberUnique("INV-101", inv101.getId()));
        assertTrue(store.isInvoiceNumberUnique("INV-UNIQUE-999", ""));
    }

    @Test
    @DisplayName("Should handle adding and removing evidence documents")
    void testDocumentPersistence() {
        DataStore store = DataStore.getInstance();
        EvidenceDocument doc = new EvidenceDocument("test-inv-id", EvidenceDocument.DocType.WORK_COMPLETION_PROOF, "proof.pdf", "/tmp/proof.pdf");
        store.addDocument(doc);

        List<EvidenceDocument> docs = store.getDocumentsForInvoice("test-inv-id");
        assertEquals(1, docs.size());
        assertEquals("proof.pdf", docs.get(0).getFileName());

        store.removeDocument(doc.getId());
        assertEquals(0, store.getDocumentsForInvoice("test-inv-id").size());
    }
}
