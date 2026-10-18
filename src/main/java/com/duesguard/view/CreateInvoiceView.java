package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.*;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen 7: Create / Edit Invoice.
 * Clean two-column layout: Form on the left, Live Invoice Preview on the right.
 * Real-time calculation of subtotal, tax, discount, balance.
 */
public class CreateInvoiceView extends ScrollPane {
    private final DataStore store;
    private final Invoice existingInvoice; // null if creating new

    // Form inputs
    private final ComboBox<Client> clientCombo = new ComboBox<>();
    private final TextField invoiceNumField = new TextField();
    private final TextField serviceTitleField = new TextField();
    private final TextArea descField = new TextArea();
    private final DatePicker issueDatePicker = new DatePicker(LocalDate.now());
    private final DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(15));
    private final ComboBox<Integer> termsCombo = new ComboBox<>();
    private final TextField discountField = new TextField("0");
    private final TextField taxField = new TextField("18");
    private final TextArea notesField = new TextArea();

    // Line items list
    private final VBox lineItemsBox = new VBox(8);
    private final List<LineItemRow> lineItemRows = new ArrayList<>();

    // Live preview labels
    private final Label previewInvNum = new Label("INV-XXX");
    private final Label previewClientName = new Label("Client Name");
    private final Label previewIssueDate = new Label();
    private final Label previewDueDate = new Label();
    private final VBox previewItemsList = new VBox(6);
    private final Label previewSubtotal = new Label("₹0.00");
    private final Label previewDiscount = new Label("₹0.00");
    private final Label previewTax = new Label("₹0.00");
    private final Label previewTotal = new Label("₹0.00");
    private final Label errorBanner = new Label();

    private static class LineItemRow {
        TextField descField = new TextField();
        TextField qtyField = new TextField("1");
        TextField rateField = new TextField("0");
        Label amtLabel = new Label("₹0.00");
        HBox row;
    }

    public CreateInvoiceView(Invoice invoiceToEdit) {
        this.store = AppContext.getInstance().getDataStore();
        this.existingInvoice = invoiceToEdit;

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // Header
        content.getChildren().add(buildHeader());

        // Error banner
        errorBanner.setStyle("-fx-background-color: rgba(255,107,107,0.15); -fx-border-color: #FF6B6B; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #FF6B6B; -fx-padding: 10 16; -fx-font-size: 13px;");
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);
        content.getChildren().add(errorBanner);

        // Two-column layout
        HBox twoCol = new HBox(28);
        twoCol.setAlignment(Pos.TOP_LEFT);

        // Left Column: Form (60%)
        VBox leftForm = buildFormColumn();
        HBox.setHgrow(leftForm, Priority.ALWAYS);

        // Right Column: Live Preview (40%)
        VBox rightPreview = buildPreviewColumn();
        rightPreview.setPrefWidth(440);
        rightPreview.setMinWidth(400);

        twoCol.getChildren().addAll(leftForm, rightPreview);
        content.getChildren().add(twoCol);

        setContent(content);

        populateClients();
        initDefaults();
        updatePreview();
    }

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("← Back to Invoices");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0;");
        btnBack.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.INVOICES));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label title = new Label(existingInvoice == null ? "Create New Invoice" : "Edit " + existingInvoice.getInvoiceNumber());
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 20px; -fx-font-weight: bold;");

        header.getChildren().addAll(btnBack, sp, title);
        return header;
    }

    private VBox buildFormColumn() {
        VBox form = new VBox(18);
        form.setPadding(new Insets(24));
        form.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;"
        );

        Label formTitle = new Label("Invoice Details");
        formTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(16);
        metaGrid.setVgap(12);

        // Client & Invoice #
        metaGrid.add(createField("Client *", clientCombo), 0, 0);
        metaGrid.add(createField("Invoice Number *", invoiceNumField), 1, 0);

        // Title & Description
        metaGrid.add(createField("Service / Deliverable Title *", serviceTitleField), 0, 1, 2, 1);

        // Dates & Terms
        metaGrid.add(createField("Issue Date", issueDatePicker), 0, 2);
        metaGrid.add(createField("Due Date *", dueDatePicker), 1, 2);

        termsCombo.getItems().addAll(7, 15, 30, 45, 60);
        termsCombo.setValue(15);
        termsCombo.setOnAction(e -> {
            if (issueDatePicker.getValue() != null && termsCombo.getValue() != null) {
                dueDatePicker.setValue(issueDatePicker.getValue().plusDays(termsCombo.getValue()));
            }
        });
        metaGrid.add(createField("Payment Terms (Days)", termsCombo), 0, 3);

        // Line Items section
        VBox itemsSection = new VBox(10);
        HBox itemsHeader = new HBox(12);
        itemsHeader.setAlignment(Pos.CENTER_LEFT);

        Label itemsTitle = new Label("Line Items");
        itemsTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: bold;");

        Region itemSp = new Region();
        HBox.setHgrow(itemSp, Priority.ALWAYS);

        Button btnAddItem = new Button("+ Add Item");
        btnAddItem.setStyle("-fx-background-color: rgba(73, 227, 180, 0.15); -fx-text-fill: #49E3B4; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        btnAddItem.setOnAction(e -> addLineItem("New Service", 1, 5000));

        itemsHeader.getChildren().addAll(itemsTitle, itemSp, btnAddItem);

        // Line items column headers
        HBox colHeader = new HBox(8);
        colHeader.getChildren().addAll(
            createHeader("Description", 240),
            createHeader("Qty", 60),
            createHeader("Rate (₹)", 100),
            createHeader("Amount", 90)
        );

        itemsSection.getChildren().addAll(itemsHeader, colHeader, lineItemsBox);

        // Discount & Tax Grid
        GridPane calcGrid = new GridPane();
        calcGrid.setHgap(16);
        calcGrid.setVgap(12);
        calcGrid.add(createField("Discount (%)", discountField), 0, 0);
        calcGrid.add(createField("Tax / GST (%)", taxField), 1, 0);

        // Notes
        descField.setPrefRowCount(2);
        descField.setPromptText("Optional notes or payment instructions...");
        descField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8;");

        // Action Buttons
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.INVOICES));

        Button btnSaveDraft = new Button("Save Draft");
        btnSaveDraft.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; -fx-font-size: 13px; " +
            "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;"
        );
        btnSaveDraft.setOnAction(e -> saveInvoice(Invoice.Status.DRAFT));

        Button btnSaveSend = new Button("Save & Send →");
        btnSaveSend.setStyle(
            "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; " +
            "-fx-background-radius: 8; -fx-padding: 10 22; -fx-cursor: hand;"
        );
        btnSaveSend.setOnAction(e -> saveInvoice(Invoice.Status.SENT));

        btnRow.getChildren().addAll(btnCancel, btnSaveDraft, btnSaveSend);

        form.getChildren().addAll(formTitle, metaGrid, new Separator(), itemsSection, new Separator(), calcGrid, createField("Notes / Instructions", descField), btnRow);
        return form;
    }

    private Label createHeader(String t, double w) {
        Label l = new Label(t);
        l.setPrefWidth(w);
        l.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private VBox buildPreviewColumn() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #182338; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(73, 227, 180, 0.2); -fx-border-radius: 20; -fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 4);"
        );

        Label pTitle = new Label("LIVE INVOICE PREVIEW");
        pTitle.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        // Brand / Biz header
        Business biz = store.getBusiness();
        Label bizName = new Label(biz != null ? biz.getName() : "Lakshmi Creative Studio");
        bizName.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        previewInvNum.setStyle("-fx-text-fill: #7C83FF; -fx-font-size: 14px; -fx-font-weight: bold;");
        previewClientName.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: 500;");

        HBox dateRow = new HBox(12);
        dateRow.getChildren().addAll(previewIssueDate, previewDueDate);

        previewIssueDate.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        previewDueDate.setStyle("-fx-text-fill: #FFB84D; -fx-font-size: 11px; -fx-font-weight: bold;");

        VBox previewBox = new VBox(10, pTitle, bizName, previewInvNum, previewClientName, dateRow, new Separator(), previewItemsList, new Separator());

        // Totals Box
        VBox totals = new VBox(6);
        totals.getChildren().addAll(
            createTotalRow("Subtotal:", previewSubtotal),
            createTotalRow("Discount:", previewDiscount),
            createTotalRow("Tax (GST):", previewTax),
            createTotalRow("Total Amount:", previewTotal)
        );

        previewTotal.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 18px; -fx-font-weight: bold;");

        card.getChildren().addAll(previewBox, totals);
        return card;
    }

    private HBox createTotalRow(String label, Label val) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        r.getChildren().addAll(lbl, sp, val);
        return r;
    }

    private void addLineItem(String desc, double qty, double rate) {
        LineItemRow lir = new LineItemRow();
        lir.descField.setText(desc);
        lir.descField.setPrefWidth(240);
        lir.descField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6; -fx-padding: 6;");

        lir.qtyField.setText(String.valueOf((int) qty));
        lir.qtyField.setPrefWidth(60);
        lir.qtyField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6; -fx-padding: 6;");

        lir.rateField.setText(String.valueOf((int) rate));
        lir.rateField.setPrefWidth(100);
        lir.rateField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6; -fx-padding: 6;");

        lir.amtLabel.setPrefWidth(90);
        lir.amtLabel.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button btnDel = new Button("✕");
        btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6B6B; -fx-cursor: hand;");
        btnDel.setOnAction(e -> {
            lineItemRows.remove(lir);
            lineItemsBox.getChildren().remove(lir.row);
            updatePreview();
        });

        // Listeners for live update
        lir.descField.textProperty().addListener((o, ov, nv) -> updatePreview());
        lir.qtyField.textProperty().addListener((o, ov, nv) -> updatePreview());
        lir.rateField.textProperty().addListener((o, ov, nv) -> updatePreview());

        HBox row = new HBox(8, lir.descField, lir.qtyField, lir.rateField, lir.amtLabel, btnDel);
        row.setAlignment(Pos.CENTER_LEFT);
        lir.row = row;

        lineItemRows.add(lir);
        lineItemsBox.getChildren().add(row);
        updatePreview();
    }

    private void updatePreview() {
        previewInvNum.setText(invoiceNumField.getText().isEmpty() ? "INV-XXX" : invoiceNumField.getText());
        Client c = clientCombo.getValue();
        previewClientName.setText(c != null ? "Billed to: " + c.getName() : "Select a client");

        if (issueDatePicker.getValue() != null) {
            previewIssueDate.setText("Issued: " + DateUtil.formatShort(issueDatePicker.getValue()));
        }
        if (dueDatePicker.getValue() != null) {
            previewDueDate.setText("Due: " + DateUtil.formatShort(dueDatePicker.getValue()));
        }

        previewItemsList.getChildren().clear();
        double subtotal = 0;

        for (LineItemRow lir : lineItemRows) {
            double q = 1;
            double r = 0;
            try { q = Double.parseDouble(lir.qtyField.getText().trim()); } catch (Exception ignored) {}
            try { r = Double.parseDouble(lir.rateField.getText().trim()); } catch (Exception ignored) {}
            double lineTotal = q * r;
            subtotal += lineTotal;

            lir.amtLabel.setText(CurrencyFormatter.format(lineTotal));

            HBox pRow = new HBox(8);
            Label pDesc = new Label(lir.descField.getText().isEmpty() ? "Service Item" : lir.descField.getText());
            pDesc.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px;");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label pAmt = new Label(CurrencyFormatter.format(lineTotal));
            pAmt.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
            pRow.getChildren().addAll(pDesc, sp, pAmt);
            previewItemsList.getChildren().add(pRow);
        }

        double discPct = 0;
        try { discPct = Double.parseDouble(discountField.getText().trim()); } catch (Exception ignored) {}
        double discAmt = subtotal * (discPct / 100.0);

        double taxPct = 0;
        try { taxPct = Double.parseDouble(taxField.getText().trim()); } catch (Exception ignored) {}
        double taxAmt = (subtotal - discAmt) * (taxPct / 100.0);

        double total = subtotal - discAmt + taxAmt;

        previewSubtotal.setText(CurrencyFormatter.format(subtotal));
        previewDiscount.setText("- " + CurrencyFormatter.format(discAmt));
        previewTax.setText("+ " + CurrencyFormatter.format(taxAmt));
        previewTotal.setText(CurrencyFormatter.format(total));
    }

    private void initDefaults() {
        if (existingInvoice != null) {
            invoiceNumField.setText(existingInvoice.getInvoiceNumber());
            serviceTitleField.setText(existingInvoice.getServiceTitle());
            issueDatePicker.setValue(existingInvoice.getIssueDate());
            dueDatePicker.setValue(existingInvoice.getDueDate());
            discountField.setText(String.valueOf(existingInvoice.getDiscount()));
            taxField.setText(String.valueOf(existingInvoice.getTaxPercent()));

            for (LineItem li : existingInvoice.getLineItems()) {
                addLineItem(li.getDescription(), li.getQuantity(), li.getRate());
            }
        } else {
            // Auto generate next invoice number
            int nextNum = store.getActiveInvoices().size() + 101;
            invoiceNumField.setText("INV-" + nextNum);
            serviceTitleField.setText("Client Project Deliverable");
            addLineItem("Professional Services", 1, 25000);
        }

        invoiceNumField.textProperty().addListener((o, ov, nv) -> updatePreview());
        serviceTitleField.textProperty().addListener((o, ov, nv) -> updatePreview());
        discountField.textProperty().addListener((o, ov, nv) -> updatePreview());
        taxField.textProperty().addListener((o, ov, nv) -> updatePreview());
        clientCombo.setOnAction(e -> updatePreview());
    }

    private void populateClients() {
        clientCombo.getItems().addAll(store.getClients());
        if (!store.getClients().isEmpty()) {
            if (existingInvoice != null) {
                store.findClientById(existingInvoice.getClientId()).ifPresent(clientCombo::setValue);
            } else {
                clientCombo.setValue(store.getClients().get(0));
            }
        }
    }

    private VBox createField(String label, Control ctrl) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");

        ctrl.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 8;");
        ctrl.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().addAll(lbl, ctrl);
        return box;
    }

    private void saveInvoice(Invoice.Status status) {
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);

        Client client = clientCombo.getValue();
        if (client == null) {
            showError("Please select a client for this invoice.");
            return;
        }

        String invNum = invoiceNumField.getText().trim();
        if (invNum.isEmpty()) {
            showError("Invoice number is required.");
            return;
        }

        String excludeId = existingInvoice != null ? existingInvoice.getId() : "";
        if (!store.isInvoiceNumberUnique(invNum, excludeId)) {
            showError("Invoice number '" + invNum + "' already exists. Please choose a unique number.");
            return;
        }

        LocalDate issueDate = issueDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        if (dueDate != null && issueDate != null && dueDate.isBefore(issueDate)) {
            showError("Due date cannot be before issue date.");
            return;
        }

        if (lineItemRows.isEmpty()) {
            showError("Please add at least one line item.");
            return;
        }

        ServiceInvoice inv = (existingInvoice instanceof ServiceInvoice si) ? si : new ServiceInvoice();
        inv.setInvoiceNumber(invNum);
        inv.setClientId(client.getId());
        inv.setServiceTitle(serviceTitleField.getText().trim());
        inv.setIssueDate(issueDate);
        inv.setDueDate(dueDate);
        inv.setStatus(status);

        try {
            inv.setDiscount(Double.parseDouble(discountField.getText().trim()));
            inv.setTaxPercent(Double.parseDouble(taxField.getText().trim()));
        } catch (Exception ignored) {}

        inv.getLineItems().clear();
        for (LineItemRow lir : lineItemRows) {
            try {
                double q = Double.parseDouble(lir.qtyField.getText().trim());
                double r = Double.parseDouble(lir.rateField.getText().trim());
                inv.getLineItems().add(new LineItem(lir.descField.getText().trim(), q, r));
            } catch (Exception ignored) {}
        }

        if (inv.getTotal() <= 0) {
            showError("Invoice total must be greater than zero.");
            return;
        }

        inv.updateStatusAutomatically();
        store.addInvoice(inv);

        Toast.show((Pane) getScene().getRoot(), "Invoice " + inv.getInvoiceNumber() + " saved successfully!", true);
        NavigationManager.getInstance().setSelectedInvoice(inv);
        NavigationManager.getInstance().navigateTo(Screen.INVOICE_DETAILS);
    }

    private void showError(String msg) {
        errorBanner.setText("⚠ " + msg);
        errorBanner.setVisible(true);
        errorBanner.setManaged(true);
    }
}
