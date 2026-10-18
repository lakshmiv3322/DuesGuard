package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.*;
import com.duesguard.service.PriorityScoreService;
import com.duesguard.service.ReadinessScoreService;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import com.duesguard.view.components.CelebrationOverlay;
import com.duesguard.view.components.ScoreRing;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.util.List;

/**
 * Screen 8: Invoice Detail View.
 * Contains:
 * 1. Financial summary (Total, Paid, Balance, Overdue)
 * 2. Payment-readiness checklist (Quotation, Sent, Completion, Delivery, Ack)
 * 3. Payment timeline (Interactive milestone trail)
 * 4. Follow-up composer (Tone, Channel, Editable preview, Copy)
 * 5. Payment history table + Record Payment action modal
 */
public class InvoiceDetailView extends ScrollPane {
    private final DataStore store;
    private final PriorityScoreService scoreService;
    private final ReadinessScoreService readinessService;
    private final Invoice invoice;
    private final Client client;
    private final VBox content;

    public InvoiceDetailView(Invoice inv) {
        this.store = AppContext.getInstance().getDataStore();
        this.scoreService = AppContext.getInstance().getPriorityScoreService();
        this.readinessService = AppContext.getInstance().getReadinessScoreService();

        // Fallback to first invoice if null
        if (inv == null && !store.getActiveInvoices().isEmpty()) {
            this.invoice = store.getActiveInvoices().get(0);
        } else {
            this.invoice = inv;
        }

        this.client = (this.invoice != null)
            ? store.findClientById(this.invoice.getClientId()).orElse(null)
            : null;

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        if (this.invoice == null) {
            Label noInv = new Label("No invoice selected.");
            noInv.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 16px;");
            content.getChildren().add(noInv);
        } else {
            renderView();
        }

        setContent(content);
    }

    private void renderView() {
        content.getChildren().clear();

        // 1. Top Header Bar
        content.getChildren().add(buildTopHeader());

        // 2. Financial Summary Cards (4 KPIs)
        content.getChildren().add(buildFinancialSummary());

        // 3. Two Column Layout:
        // Left (55%): Payment-Readiness Checklist + Payment Timeline
        // Right (45%): Follow-Up Composer + Payment History
        HBox twoCol = new HBox(24);
        twoCol.setAlignment(Pos.TOP_LEFT);

        VBox leftCol = new VBox(20);
        HBox.setHgrow(leftCol, Priority.ALWAYS);
        leftCol.getChildren().addAll(buildReadinessChecklist(), buildPaymentTimeline());

        VBox rightCol = new VBox(20);
        rightCol.setPrefWidth(480);
        rightCol.setMinWidth(420);
        rightCol.getChildren().addAll(buildFollowUpComposer(), buildPaymentHistory());

        twoCol.getChildren().addAll(leftCol, rightCol);
        content.getChildren().add(twoCol);
    }

    // -------------------------------------------------------------
    // Top Header
    // -------------------------------------------------------------
    private HBox buildTopHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("← Invoices");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0;");
        btnBack.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.INVOICES));

        Label invNum = new Label(invoice.getInvoiceNumber());
        invNum.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Status pill
        HBox statusPill = new HBox(6);
        statusPill.setAlignment(Pos.CENTER_LEFT);
        statusPill.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08); " +
            "-fx-background-radius: 100; -fx-padding: 4 10;"
        );
        Circle dot = new Circle(4);
        dot.setFill(Color.web(invoice.getStatus().getColor()));
        Label statusLbl = new Label(invoice.getStatus().getDisplay());
        statusLbl.setStyle("-fx-text-fill: " + invoice.getStatus().getColor() + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        statusPill.getChildren().addAll(dot, statusLbl);

        // Priority Score
        double score = scoreService.calculate(invoice, client);
        Label scoreBadge = new Label("Priority Score: " + String.format("%.0f", score));
        scoreBadge.setStyle(
            "-fx-background-color: rgba(255, 107, 107, 0.15); " +
            "-fx-border-color: #FF6B6B; -fx-border-radius: 100; -fx-background-radius: 100; -fx-border-width: 1; " +
            "-fx-text-fill: #FF6B6B; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buttons
        Button btnRemind = new Button("Prepare Reminder");
        btnRemind.setStyle(
            "-fx-background-color: rgba(124, 131, 255, 0.15); -fx-text-fill: #7C83FF; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-border-color: rgba(124,131,255,0.3); -fx-border-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"
        );
        btnRemind.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(invoice);
            NavigationManager.getInstance().navigateTo(Screen.REMINDER_COMPOSER);
        });

        Button btnRecord = new Button("+ Record Payment");
        btnRecord.setStyle(
            "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;"
        );
        btnRecord.setOnAction(e -> openRecordPaymentDialog());

        header.getChildren().addAll(btnBack, invNum, statusPill, scoreBadge, spacer, btnRemind, btnRecord);
        return header;
    }

    // -------------------------------------------------------------
    // Financial Summary Cards
    // -------------------------------------------------------------
    private GridPane buildFinancialSummary() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        grid.add(createSummaryCard("Total Billed", CurrencyFormatter.format(invoice.getTotal()), client != null ? client.getName() : "Client", "#F6F8FC"), 0, 0);
        grid.add(createSummaryCard("Amount Paid", CurrencyFormatter.format(invoice.getAmountPaid()), invoice.getAmountPaid() > 0 ? "Recorded via UPI/Bank" : "No payments yet", "#4CAF50"), 1, 0);
        grid.add(createSummaryCard("Balance Due", CurrencyFormatter.format(invoice.getBalance()), "Outstanding amount", invoice.getBalance() > 0 ? "#FF6B6B" : "#4CAF50"), 2, 0);

        String overdueStr = invoice.getDaysOverdue() > 0
            ? invoice.getDaysOverdue() + " days overdue"
            : (invoice.getDaysUntilDue() == 0 ? "Due today" : "Due in " + invoice.getDaysUntilDue() + " days");
        grid.add(createSummaryCard("Due Date", DateUtil.formatDisplay(invoice.getDueDate()), overdueStr, invoice.getDaysOverdue() > 0 ? "#FF6B6B" : "#FFB84D"), 3, 0);

        return grid;
    }

    private VBox createSummaryCard(String label, String value, String sub, String valColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1;");

        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");

        Label v = new Label(value);
        v.setStyle("-fx-text-fill: " + valColor + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");

        card.getChildren().addAll(l, v, s);
        return card;
    }

    // -------------------------------------------------------------
    // Payment-Readiness Checklist
    // -------------------------------------------------------------
    private VBox buildReadinessChecklist() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;"
        );

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Payment-Readiness Checklist");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label sub = new Label("Dispute-proof supporting proof required before escalating");
        sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        titleCol.getChildren().addAll(title, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        List<EvidenceDocument> docs = store.getDocumentsForInvoice(invoice.getId());
        ReadinessScoreService.ReadinessResult rr = readinessService.calculate(invoice, docs);

        Label scorePill = new Label(String.format("%.0f%% Ready", rr.score()));
        scorePill.setStyle(
            "-fx-background-color: " + (rr.score() >= 80 ? "rgba(76,175,80,0.2)" : "rgba(255,184,77,0.2)") + "; " +
            "-fx-text-fill: " + (rr.score() >= 80 ? "#4CAF50" : "#FFB84D") + "; " +
            "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 100; -fx-padding: 3 10;"
        );

        top.getChildren().addAll(titleCol, sp, scorePill);

        VBox checkList = new VBox(10);
        checkList.getChildren().addAll(
            createCheckItem("1. Approved Quotation attached", rr.hasQuotation(), "quotation-arun-101.pdf"),
            createCheckItem("2. Official invoice delivered to client", rr.invoiceSent(), "Delivered via WhatsApp & Email"),
            createCheckItem("3. Work completion proof attached", rr.hasCompletionProof(), rr.hasCompletionProof() ? "Verified deliverable link" : "MISSING: Attach work proof"),
            createCheckItem("4. Delivery / Service sign-off proof", rr.hasDeliveryProof(), rr.hasDeliveryProof() ? "Delivery confirmation noted" : "MISSING: Proof of handoff"),
            createCheckItem("5. Client payment acknowledgement received", rr.hasClientAck(), rr.hasClientAck() ? "Client acknowledged via chat" : "No written acknowledgment")
        );

        Button btnAddProof = new Button("+ Add Missing Proof");
        btnAddProof.setStyle(
            "-fx-background-color: rgba(73, 227, 180, 0.15); -fx-text-fill: #49E3B4; " +
            "-fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
        );
        btnAddProof.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(invoice);
            NavigationManager.getInstance().navigateTo(Screen.DOCUMENTS);
        });

        card.getChildren().addAll(top, checkList, btnAddProof);
        return card;
    }

    private HBox createCheckItem(String labelText, boolean checked, String subtext) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle(
            "-fx-background-color: " + (checked ? "rgba(76,175,80,0.06)" : "rgba(255,107,107,0.06)") + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: " + (checked ? "rgba(76,175,80,0.2)" : "rgba(255,107,107,0.2)") + "; " +
            "-fx-border-radius: 10; -fx-border-width: 1;"
        );

        Label mark = new Label(checked ? "✓" : "✕");
        mark.setStyle("-fx-text-fill: " + (checked ? "#4CAF50" : "#FF6B6B") + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox textCol = new VBox(1);
        Label main = new Label(labelText);
        main.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: 500;");

        Label sub = new Label(subtext);
        sub.setStyle("-fx-text-fill: " + (checked ? "#9BA8BC" : "#FF6B6B") + "; -fx-font-size: 11px;");
        textCol.getChildren().addAll(main, sub);

        row.getChildren().addAll(mark, textCol);
        return row;
    }

    // -------------------------------------------------------------
    // Payment Timeline
    // -------------------------------------------------------------
    private VBox buildPaymentTimeline() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;"
        );

        Label title = new Label("Payment Timeline");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox trail = new VBox(10);
        trail.getChildren().addAll(
            createTimelineItem("Invoice Created", DateUtil.formatDisplay(invoice.getIssueDate()), "Invoice drafted and verified", true),
            createTimelineItem("Invoice Sent", DateUtil.formatDisplay(invoice.getIssueDate()), "Dispatched to client via preferred channel", true),
            createTimelineItem("Payment Due Date", DateUtil.formatDisplay(invoice.getDueDate()), invoice.getDaysOverdue() > 0 ? "Due date passed" : "Upcoming payment date", invoice.getDaysOverdue() > 0),
            createTimelineItem("Overdue Status Triggered", invoice.getDaysOverdue() > 0 ? invoice.getDaysOverdue() + " days past due" : "Not triggered", "Entered Priority Queue ranking", invoice.getDaysOverdue() > 0)
        );

        card.getChildren().addAll(title, trail);
        return card;
    }

    private HBox createTimelineItem(String step, String date, String detail, boolean completed) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(5);
        dot.setFill(Color.web(completed ? "#49E3B4" : "#4A5568"));

        VBox col = new VBox(2);
        Label sLbl = new Label(step + " • " + date);
        sLbl.setStyle("-fx-text-fill: " + (completed ? "#F6F8FC" : "#9BA8BC") + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label dLbl = new Label(detail);
        dLbl.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");
        col.getChildren().addAll(sLbl, dLbl);

        row.getChildren().addAll(dot, col);
        return row;
    }

    // -------------------------------------------------------------
    // Follow-up Composer
    // -------------------------------------------------------------
    private VBox buildFollowUpComposer() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;"
        );

        Label title = new Label("Follow-Up Composer");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Tone Selector
        HBox toneBox = new HBox(8);
        toneBox.setAlignment(Pos.CENTER_LEFT);
        Label toneLbl = new Label("Tone:");
        toneLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        ComboBox<Reminder.Tone> toneCombo = new ComboBox<>();
        toneCombo.getItems().addAll(Reminder.Tone.values());
        toneCombo.setValue(Reminder.Tone.FRIENDLY);
        toneCombo.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8;");

        // Channel Selector
        Label chanLbl = new Label("Channel:");
        chanLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        ComboBox<Reminder.Channel> chanCombo = new ComboBox<>();
        chanCombo.getItems().addAll(Reminder.Channel.values());
        chanCombo.setValue(Reminder.Channel.WHATSAPP);
        chanCombo.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8;");

        toneBox.getChildren().addAll(toneLbl, toneCombo, chanLbl, chanCombo);

        // Editable Text Area
        TextArea msgArea = new TextArea();
        msgArea.setPrefRowCount(5);
        msgArea.setWrapText(true);
        msgArea.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-font-size: 12px;");

        Runnable updateMsg = () -> {
            String gen = AppContext.getInstance().getReminderMessageService()
                .generateMessage(invoice, client, toneCombo.getValue(), Reminder.EscalationLevel.FIRST);
            msgArea.setText(gen);
        };
        updateMsg.run();

        toneCombo.setOnAction(e -> updateMsg.run());

        // Buttons
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCopy = new Button("📋 Copy Message");
        btnCopy.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;");
        btnCopy.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(msgArea.getText());
            Clipboard.getSystemClipboard().setContent(cc);
            Toast.show((Pane) getScene().getRoot(), "Message copied to clipboard!", true);
        });

        Button btnMarkSent = new Button("✓ Mark as Sent");
        btnMarkSent.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");
        btnMarkSent.setOnAction(e -> {
            FollowUpLog log = new FollowUpLog(invoice.getId(), chanCombo.getValue().name(), "Sent " + toneCombo.getValue().name() + " reminder");
            store.addFollowUpLog(log);
            Toast.show((Pane) getScene().getRoot(), "Follow-up logged successfully!", true);
        });

        btnRow.getChildren().addAll(btnCopy, btnMarkSent);

        card.getChildren().addAll(title, toneBox, msgArea, btnRow);
        return card;
    }

    // -------------------------------------------------------------
    // Payment History Table
    // -------------------------------------------------------------
    private VBox buildPaymentHistory() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;"
        );

        Label title = new Label("Payment History");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        List<Payment> payments = store.getPaymentsForInvoice(invoice.getId());

        if (payments.isEmpty()) {
            Label empty = new Label("No payments recorded yet for this invoice.");
            empty.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
            card.getChildren().addAll(title, empty);
        } else {
            VBox pList = new VBox(8);
            for (Payment p : payments) {
                HBox pRow = new HBox(12);
                pRow.setAlignment(Pos.CENTER_LEFT);
                pRow.setPadding(new Insets(10, 14, 10, 14));
                pRow.setStyle("-fx-background-color: #182338; -fx-background-radius: 8;");

                VBox col = new VBox(2);
                Label amt = new Label(CurrencyFormatter.format(p.getAmount()));
                amt.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label ref = new Label(p.getMethod().getDisplay() + " • " + DateUtil.formatDisplay(p.getPaymentDate()) + (p.getReference() != null ? " • " + p.getReference() : ""));
                ref.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
                col.getChildren().addAll(amt, ref);

                pRow.getChildren().add(col);
                pList.getChildren().add(pRow);
            }
            card.getChildren().addAll(title, pList);
        }

        return card;
    }

    // -------------------------------------------------------------
    // Record Payment Dialog Modal
    // -------------------------------------------------------------
    private void openRecordPaymentDialog() {
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.85);");
        modalOverlay.setPrefSize(getWidth(), getHeight());

        VBox card = new VBox(18);
        card.setMaxWidth(460);
        card.setPadding(new Insets(32));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(73, 227, 180, 0.3); -fx-border-radius: 20; -fx-border-width: 1.5; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 8);"
        );

        Label title = new Label("Record Payment for " + invoice.getInvoiceNumber());
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label balHint = new Label("Outstanding Balance: " + CurrencyFormatter.format(invoice.getBalance()));
        balHint.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px;");
        errLbl.setVisible(false);

        // Amount field (defaults to balance)
        TextField amtField = new TextField(String.valueOf((int) invoice.getBalance()));
        amtField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 15px; -fx-font-weight: bold;");

        // Payment Method
        ComboBox<Payment.Method> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll(Payment.Method.values());
        methodCombo.setValue(Payment.Method.UPI);
        methodCombo.setMaxWidth(Double.MAX_VALUE);
        methodCombo.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

        // Reference
        TextField refField = new TextField("UPI-" + System.currentTimeMillis() % 1000000);
        refField.setPromptText("Transaction / Ref Number");
        refField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-padding: 8;");

        // Notes
        TextField notesField = new TextField("Full / partial payment received");
        notesField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-padding: 8;");

        // Buttons
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> ((Pane) getParent()).getChildren().remove(modalOverlay));

        Button btnSave = new Button("Confirm Payment");
        btnSave.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                if (amt <= 0) {
                    errLbl.setText("Amount must be greater than zero.");
                    errLbl.setVisible(true);
                    return;
                }
                if (amt > invoice.getBalance() + 0.01) {
                    errLbl.setText("Payment exceeds outstanding balance of " + CurrencyFormatter.format(invoice.getBalance()));
                    errLbl.setVisible(true);
                    return;
                }

                // Apply payment
                invoice.recordPayment(amt);
                Payment p = new Payment(invoice.getId(), invoice.getClientId(), amt, methodCombo.getValue());
                p.setReference(refField.getText().trim());
                p.setNotes(notesField.getText().trim());
                p.setRecordedBy("Lakshmi V.");
                store.addPayment(p);
                store.saveAll();

                ((Pane) getParent()).getChildren().remove(modalOverlay);

                // Check if this was an overdue invoice that got paid: WOW CELEBRATION!
                if (invoice.getStatus() == Invoice.Status.PAID) {
                    CelebrationOverlay.showRecoveryCelebration((Pane) getParent(), CurrencyFormatter.format(amt));
                } else {
                    Toast.show((Pane) getParent(), "Recorded partial payment of " + CurrencyFormatter.format(amt), true);
                }

                renderView();
            } catch (Exception ex) {
                errLbl.setText("Invalid payment amount: " + ex.getMessage());
                errLbl.setVisible(true);
            }
        });

        btnRow.getChildren().addAll(btnCancel, btnSave);

        card.getChildren().addAll(
            title, balHint, errLbl,
            createLabeledField("Payment Amount (₹) *", amtField),
            createLabeledField("Payment Method", methodCombo),
            createLabeledField("Reference Number", refField),
            createLabeledField("Notes", notesField),
            btnRow
        );

        modalOverlay.getChildren().add(card);
        ((Pane) getParent()).getChildren().add(modalOverlay);
    }

    private VBox createLabeledField(String lbl, Control ctrl) {
        VBox b = new VBox(4);
        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-font-weight: bold;");
        b.getChildren().addAll(l, ctrl);
        return b;
    }
}
