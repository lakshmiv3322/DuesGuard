package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.EvidenceDocument;
import com.duesguard.model.Invoice;
import com.duesguard.storage.DataStore;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Screen 15: Documents & Dispute Readiness.
 * Drag-and-drop upload zone for supporting documents (Quotation, Work Completion, Delivery Proof),
 * readiness breakdown per invoice, and documents repository.
 */
public class DocumentsView extends ScrollPane {
    private final DataStore store;
    private final VBox docsListContainer;
    private final VBox invoiceReadinessContainer;

    public DocumentsView() {
        this.store = AppContext.getInstance().getDataStore();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // Header
        content.getChildren().add(buildHeader());

        // Drag-and-drop upload zone
        content.getChildren().add(buildUploadDropZone());

        // Two columns:
        // Left (50%): Invoices & their dispute-readiness meters
        // Right (50%): Uploaded Evidence Documents repository
        HBox twoCol = new HBox(20);
        twoCol.setAlignment(Pos.TOP_LEFT);

        VBox leftCol = new VBox(14);
        HBox.setHgrow(leftCol, Priority.ALWAYS);
        Label leftTitle = new Label("Invoice Dispute-Readiness Status");
        leftTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");
        invoiceReadinessContainer = new VBox(10);
        leftCol.getChildren().addAll(leftTitle, invoiceReadinessContainer);

        VBox rightCol = new VBox(14);
        HBox.setHgrow(rightCol, Priority.ALWAYS);
        Label rightTitle = new Label("Evidence Documents Repository");
        rightTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");
        docsListContainer = new VBox(10);
        rightCol.getChildren().addAll(rightTitle, docsListContainer);

        twoCol.getChildren().addAll(leftCol, rightCol);
        content.getChildren().add(twoCol);

        setContent(content);
        refreshLists();
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Documents & Payment-Readiness");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Dispute-proof evidence documents protecting your receivables");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        header.getChildren().add(titleCol);
        return header;
    }

    private VBox buildUploadDropZone() {
        VBox dropZone = new VBox(10);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(32));
        dropZone.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 18; " +
            "-fx-border-color: rgba(73, 227, 180, 0.3); -fx-border-radius: 18; " +
            "-fx-border-width: 2; -fx-border-style: dashed; -fx-cursor: hand;"
        );

        Label icon = new Label("📁");
        icon.setStyle("-fx-font-size: 28px;");

        Label text = new Label("Drag & drop completion proof, quotations, or sign-offs here");
        text.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label sub = new Label("or click 'Browse Files' to attach supporting documents to an invoice");
        sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        Button btnBrowse = new Button("Browse Files...");
        btnBrowse.setStyle("-fx-background-color: rgba(73, 227, 180, 0.15); -fx-text-fill: #49E3B4; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnBrowse.setOnAction(e -> handleBrowseFile());

        dropZone.getChildren().addAll(icon, text, sub, btnBrowse);

        // JavaFX DragEvent handling
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                dropZone.setStyle("-fx-background-color: rgba(73, 227, 180, 0.1); -fx-background-radius: 18; -fx-border-color: #49E3B4; -fx-border-radius: 18; -fx-border-width: 2; -fx-border-style: dashed;");
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 18; -fx-border-color: rgba(73, 227, 180, 0.3); -fx-border-radius: 18; -fx-border-width: 2; -fx-border-style: dashed;");
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    simulateDocumentUpload(file);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return dropZone;
    }

    private void handleBrowseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Evidence Document");
        File file = fc.showOpenDialog(getScene().getWindow());
        if (file != null) {
            simulateDocumentUpload(file);
        }
    }

    private void simulateDocumentUpload(File file) {
        try {
            // Pick an invoice needing proof (e.g., INV-101 or INV-103)
            List<Invoice> active = store.getActiveInvoices();
            Invoice target = active.stream().filter(i -> i.getReadinessScore() < 70).findFirst().orElse(active.get(0));

            // Copy file to app data directory
            String appData = System.getProperty("user.home") + File.separator + ".duesguard" + File.separator + "docs";
            Files.createDirectories(Paths.get(appData));
            Path dest = Paths.get(appData).resolve(file.getName());
            try {
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}

            EvidenceDocument doc = new EvidenceDocument(target.getId(), EvidenceDocument.DocType.WORK_COMPLETION_PROOF, file.getName(), dest.toString());
            doc.setVerified(true);
            store.addDocument(doc);

            // Boost invoice readiness score
            target.setReadinessScore(Math.min(100.0, target.getReadinessScore() + 20.0));
            store.saveAll();

            Toast.show(this, "Uploaded '" + file.getName() + "' and linked to " + target.getInvoiceNumber() + "! Readiness increased.", true);
            refreshLists();
        } catch (Exception e) {
            Toast.show(this, "Upload error: " + e.getMessage(), false);
        }
    }

    private void refreshLists() {
        invoiceReadinessContainer.getChildren().clear();
        docsListContainer.getChildren().clear();

        // 1. Invoice readiness meters
        for (Invoice inv : store.getActiveInvoices()) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.04); -fx-border-radius: 10; -fx-border-width: 1;");

            Label invLbl = new Label(inv.getInvoiceNumber());
            invLbl.setPrefWidth(90);
            invLbl.setStyle("-fx-text-fill: #7C83FF; -fx-font-weight: bold;");

            // Segmented readiness bar
            double score = inv.getReadinessScore();
            ProgressBar pBar = new ProgressBar(score / 100.0);
            pBar.setPrefWidth(140);
            pBar.setPrefHeight(8);
            pBar.setStyle("-fx-accent: " + (score >= 80 ? "#4CAF50" : (score >= 50 ? "#FFB84D" : "#FF6B6B")) + ";");

            Label scoreLbl = new Label(String.format("%.0f%%", score));
            scoreLbl.setPrefWidth(50);
            scoreLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 11px; -fx-font-weight: bold;");

            Label statusLbl = new Label(score >= 80 ? "Dispute-ready" : "Needs documents");
            statusLbl.setStyle("-fx-text-fill: " + (score >= 80 ? "#4CAF50" : "#FFB84D") + "; -fx-font-size: 11px;");

            row.getChildren().addAll(invLbl, pBar, scoreLbl, statusLbl);
            invoiceReadinessContainer.getChildren().add(row);
        }

        // 2. Documents repository list
        List<EvidenceDocument> docs = store.getDocuments();
        if (docs.isEmpty()) {
            Label noDocs = new Label("No documents uploaded yet.");
            noDocs.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
            docsListContainer.getChildren().add(noDocs);
        } else {
            for (EvidenceDocument doc : docs) {
                HBox docRow = new HBox(12);
                docRow.setAlignment(Pos.CENTER_LEFT);
                docRow.setPadding(new Insets(10, 14, 10, 14));
                docRow.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 10;");

                Label docIcon = new Label("📄");
                docIcon.setStyle("-fx-font-size: 16px;");

                VBox text = new VBox(2);
                Label name = new Label(doc.getFileName());
                name.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-font-weight: bold;");

                Invoice targetInv = store.findInvoiceById(doc.getInvoiceId()).orElse(null);
                String invTag = targetInv != null ? targetInv.getInvoiceNumber() : "INV-XXX";
                Label sub = new Label(doc.getType().getDisplay() + " • " + invTag);
                sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 10px;");
                text.getChildren().addAll(name, sub);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Button btnDel = new Button("✕");
                btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6B6B; -fx-cursor: hand;");
                btnDel.setOnAction(e -> {
                    store.removeDocument(doc.getId());
                    Toast.show(this, "Document removed", true);
                    refreshLists();
                });

                docRow.getChildren().addAll(docIcon, text, sp, btnDel);
                docsListContainer.getChildren().add(docRow);
            }
        }
    }
}
