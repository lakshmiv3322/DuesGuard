package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.model.Payment;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Screen 11: Payments Ledger.
 * Displays all received payments with payment method breakdown,
 * CSV export, and quick Record Payment modal.
 */
public class PaymentsView extends ScrollPane {
    private final DataStore store;
    private final VBox tableContainer;

    public PaymentsView() {
        this.store = AppContext.getInstance().getDataStore();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // Header
        content.getChildren().add(buildHeader());

        // KPI summary row
        content.getChildren().add(buildSummaryRow());

        // Table container
        tableContainer = new VBox(10);
        content.getChildren().add(tableContainer);

        setContent(content);
        refreshTable();
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Payment Ledger");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Audit trail of all received settlements and partial collections");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExport = new Button("Export CSV");
        btnExport.setStyle("-fx-background-color: #121A2B; -fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;");
        btnExport.setOnAction(e -> exportPaymentsCSV());

        Button btnRecord = new Button("+ Record Payment");
        btnRecord.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");
        btnRecord.setOnAction(e -> openRecordPaymentModal());

        header.getChildren().addAll(titleCol, spacer, btnExport, btnRecord);
        return header;
    }

    private GridPane buildSummaryRow() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        List<Payment> payments = store.getPayments();
        double totalRec = payments.stream().mapToDouble(Payment::getAmount).sum();
        double upiTotal = payments.stream().filter(p -> p.getMethod() == Payment.Method.UPI).mapToDouble(Payment::getAmount).sum();
        double bankTotal = payments.stream().filter(p -> p.getMethod() == Payment.Method.BANK_TRANSFER).mapToDouble(Payment::getAmount).sum();
        double otherTotal = totalRec - upiTotal - bankTotal;

        grid.add(createKpiCard("Total Collected", CurrencyFormatter.format(totalRec), payments.size() + " settlements", "#4CAF50"), 0, 0);
        grid.add(createKpiCard("UPI Inflow", CurrencyFormatter.format(upiTotal), "Fast recovery channel", "#49E3B4"), 1, 0);
        grid.add(createKpiCard("Bank Transfer / NEFT", CurrencyFormatter.format(bankTotal), "Direct bank settlements", "#7C83FF"), 2, 0);
        grid.add(createKpiCard("Cash & Other", CurrencyFormatter.format(otherTotal), "In-person payments", "#FFB84D"), 3, 0);

        return grid;
    }

    private VBox createKpiCard(String lbl, String val, String sub, String colorHex) {
        VBox b = new VBox(6);
        b.setPadding(new Insets(16, 20, 16, 20));
        b.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1;");
        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        Label v = new Label(val);
        v.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");
        b.getChildren().addAll(l, v, s);
        return b;
    }

    private void refreshTable() {
        tableContainer.getChildren().clear();

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setStyle("-fx-background-color: #0D1526; -fx-background-radius: 8;");

        header.getChildren().addAll(
            createHeaderCell("Payment Date", 120),
            createHeaderCell("Client", 180),
            createHeaderCell("Invoice #", 120),
            createHeaderCell("Amount", 130),
            createHeaderCell("Method", 130),
            createHeaderCell("Reference Number", 180),
            createHeaderCell("Recorded By", 130)
        );

        tableContainer.getChildren().add(header);

        List<Payment> list = store.getPayments();
        if (list.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(48));
            Label emptyLbl = new Label("No payments recorded yet.");
            emptyLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
            empty.getChildren().add(emptyLbl);
            tableContainer.getChildren().add(empty);
            return;
        }

        for (Payment p : list) {
            Client c = store.findClientById(p.getClientId()).orElse(null);
            Invoice inv = store.findInvoiceById(p.getInvoiceId()).orElse(null);

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.04); -fx-border-radius: 10; -fx-border-width: 1;");

            Label dateLbl = new Label(DateUtil.formatDisplay(p.getPaymentDate()));
            dateLbl.setPrefWidth(120);
            dateLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

            Label clientLbl = new Label(c != null ? c.getName() : "Unknown");
            clientLbl.setPrefWidth(180);
            clientLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: bold;");

            Label invLbl = new Label(inv != null ? inv.getInvoiceNumber() : p.getInvoiceId());
            invLbl.setPrefWidth(120);
            invLbl.setStyle("-fx-text-fill: #7C83FF; -fx-font-size: 12px;");

            Label amtLbl = new Label(CurrencyFormatter.format(p.getAmount()));
            amtLbl.setPrefWidth(130);
            amtLbl.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label methodLbl = new Label(p.getMethod().getDisplay());
            methodLbl.setPrefWidth(130);
            methodLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px;");

            Label refLbl = new Label(p.getReference() != null ? p.getReference() : "—");
            refLbl.setPrefWidth(180);
            refLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

            Label byLbl = new Label(p.getRecordedBy() != null ? p.getRecordedBy() : "Lakshmi V.");
            byLbl.setPrefWidth(130);
            byLbl.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 12px;");

            row.getChildren().addAll(dateLbl, clientLbl, invLbl, amtLbl, methodLbl, refLbl, byLbl);
            tableContainer.getChildren().add(row);
        }
    }

    private Label createHeaderCell(String text, double w) {
        Label l = new Label(text);
        l.setPrefWidth(w);
        l.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private void openRecordPaymentModal() {
        List<Invoice> unpaid = store.getActiveInvoices().stream()
            .filter(i -> i.getStatus() != Invoice.Status.PAID && i.getStatus() != Invoice.Status.DRAFT)
            .toList();

        if (unpaid.isEmpty()) {
            Toast.show(this, "No outstanding invoices to record payment for!", true);
            return;
        }

        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.85);");
        modalOverlay.setPrefSize(getWidth(), getHeight());

        VBox card = new VBox(16);
        card.setMaxWidth(460);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(73, 227, 180, 0.3); -fx-border-radius: 20; -fx-border-width: 1.5;");

        Label title = new Label("Record Payment");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Invoice> invCombo = new ComboBox<>();
        invCombo.getItems().addAll(unpaid);
        invCombo.setValue(unpaid.get(0));
        invCombo.setMaxWidth(Double.MAX_VALUE);

        Label clientAutoLbl = new Label("Client: ");
        clientAutoLbl.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label balAutoLbl = new Label("Balance: ");
        balAutoLbl.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px; -fx-font-weight: bold;");

        TextField amtField = new TextField();
        amtField.setPromptText("Enter amount (₹)");

        Runnable updateSelected = () -> {
            Invoice sel = invCombo.getValue();
            if (sel != null) {
                Client c = store.findClientById(sel.getClientId()).orElse(null);
                clientAutoLbl.setText("Client: " + (c != null ? c.getName() : "Unknown"));
                balAutoLbl.setText("Balance: " + CurrencyFormatter.format(sel.getBalance()));
                amtField.setText(String.valueOf((int) sel.getBalance()));
            }
        };
        invCombo.setOnAction(e -> updateSelected.run());
        updateSelected.run();

        ComboBox<Payment.Method> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll(Payment.Method.values());
        methodCombo.setValue(Payment.Method.UPI);
        methodCombo.setMaxWidth(Double.MAX_VALUE);

        TextField refField = new TextField("TXN-" + System.currentTimeMillis() % 1000000);
        refField.setPromptText("Reference number");

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px;");
        errLbl.setVisible(false);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> ((Pane) getParent()).getChildren().remove(modalOverlay));

        Button btnSave = new Button("Save Payment");
        btnSave.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            Invoice inv = invCombo.getValue();
            if (inv == null) return;
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                if (amt <= 0 || amt > inv.getBalance() + 0.01) {
                    errLbl.setText("Amount must be between 1 and " + CurrencyFormatter.format(inv.getBalance()));
                    errLbl.setVisible(true);
                    return;
                }
                inv.recordPayment(amt);
                Payment p = new Payment(inv.getId(), inv.getClientId(), amt, methodCombo.getValue());
                p.setReference(refField.getText().trim());
                p.setRecordedBy("Lakshmi V.");
                store.addPayment(p);
                store.saveAll();

                ((Pane) getParent()).getChildren().remove(modalOverlay);
                Toast.show((Pane) getParent(), "Payment of " + CurrencyFormatter.format(amt) + " recorded successfully!", true);
                refreshTable();
            } catch (Exception ex) {
                errLbl.setText("Invalid amount.");
                errLbl.setVisible(true);
            }
        });

        btnRow.getChildren().addAll(btnCancel, btnSave);

        card.getChildren().addAll(
            title,
            createFormField("Select Invoice *", invCombo),
            clientAutoLbl, balAutoLbl, errLbl,
            createFormField("Payment Amount (₹) *", amtField),
            createFormField("Payment Method", methodCombo),
            createFormField("Reference Number", refField),
            btnRow
        );

        modalOverlay.getChildren().add(card);
        ((Pane) getParent()).getChildren().add(modalOverlay);
    }

    private VBox createFormField(String lbl, Control c) {
        VBox b = new VBox(4);
        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-font-weight: bold;");
        c.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 8;");
        b.getChildren().addAll(l, c);
        return b;
    }

    private void exportPaymentsCSV() {
        try {
            File f = new File(System.getProperty("user.home") + File.separator + "duesguard-payments.csv");
            try (PrintWriter pw = new PrintWriter(f)) {
                pw.println("PaymentID,InvoiceID,ClientID,Amount,PaymentDate,Method,Reference,Notes");
                for (Payment p : store.getPayments()) {
                    pw.println(p.toCSV());
                }
            }
            Toast.show(this, "Exported payments to " + f.getName(), true);
        } catch (Exception e) {
            Toast.show(this, "Export failed: " + e.getMessage(), false);
        }
    }
}
