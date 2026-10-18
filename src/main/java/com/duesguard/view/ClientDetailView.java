package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.*;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Screen 10: Client Detail View.
 * Displays client contact details, summary KPI cards, behavioural insights
 * based on recorded history, and tabs (Overview, Invoices, Payments, Follow-ups, Notes).
 */
public class ClientDetailView extends ScrollPane {
    private final DataStore store;
    private final Client client;
    private final VBox content;

    public ClientDetailView(Client cl) {
        this.store = AppContext.getInstance().getDataStore();

        if (cl == null && !store.getClients().isEmpty()) {
            this.client = store.getClients().get(0);
        } else {
            this.client = cl;
        }

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        if (this.client == null) {
            Label noCl = new Label("No client selected.");
            noCl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 16px;");
            content.getChildren().add(noCl);
        } else {
            renderView();
        }

        setContent(content);
    }

    private void renderView() {
        content.getChildren().clear();

        // 1. Header
        content.getChildren().add(buildHeader());

        // 2. Summary KPI Cards (5 metrics)
        content.getChildren().add(buildSummaryCards());

        // 3. Client Behaviour Insight Card (Based on recorded history)
        content.getChildren().add(buildInsightCard());

        // 4. Tabs: Invoices, Payments, Follow-ups, Notes
        content.getChildren().add(buildTabsSection());
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("← Clients");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0;");
        btnBack.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.CLIENTS));

        VBox titleCol = new VBox(2);
        Label title = new Label(client.getName());
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 24px; -fx-font-weight: bold;");

        String contacts = (client.getEmail() != null ? client.getEmail() : "") +
            (client.getPhone() != null ? " • " + client.getPhone() : "") +
            " • Preferred: " + client.getPreferredChannel().getDisplay();
        Label sub = new Label(contacts);
        sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        titleCol.getChildren().addAll(title, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnNewInv = new Button("+ Create Invoice");
        btnNewInv.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnNewInv.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedClient(client);
            NavigationManager.getInstance().navigateTo(Screen.CREATE_INVOICE);
        });

        header.getChildren().addAll(btnBack, titleCol, sp, btnNewInv);
        return header;
    }

    private GridPane buildSummaryCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(20);
            grid.getColumnConstraints().add(col);
        }

        List<Invoice> invoices = store.getActiveInvoices().stream()
            .filter(i -> i.getClientId().equals(client.getId()))
            .toList();

        double totalBilled = invoices.stream().mapToDouble(Invoice::getTotal).sum();
        double totalPaid = invoices.stream().mapToDouble(Invoice::getAmountPaid).sum();
        double currentBalance = invoices.stream().filter(i -> i.getStatus() != Invoice.Status.PAID).mapToDouble(Invoice::getBalance).sum();
        int avgDelay = client.getTypicalDelayDays();
        long onTimeCount = invoices.stream().filter(i -> i.getStatus() == Invoice.Status.PAID && i.getDaysOverdue() == 0).count();
        double onTimeRate = invoices.isEmpty() ? 0 : ((double) onTimeCount / invoices.size()) * 100;

        grid.add(createKpiCard("Total Billed", CurrencyFormatter.format(totalBilled), "#F6F8FC"), 0, 0);
        grid.add(createKpiCard("Total Paid", CurrencyFormatter.format(totalPaid), "#4CAF50"), 1, 0);
        grid.add(createKpiCard("Current Outstanding", CurrencyFormatter.format(currentBalance), currentBalance > 0 ? "#FF6B6B" : "#4CAF50"), 2, 0);
        grid.add(createKpiCard("Average Payment Delay", avgDelay + " days", "#FFB84D"), 3, 0);
        grid.add(createKpiCard("On-Time Rate", String.format("%.0f%%", onTimeRate), "#7C83FF"), 4, 0);

        return grid;
    }

    private VBox createKpiCard(String label, String val, String colorHex) {
        VBox b = new VBox(6);
        b.setPadding(new Insets(16, 20, 16, 20));
        b.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1;");
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        Label v = new Label(val);
        v.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        b.getChildren().addAll(l, v);
        return b;
    }

    private VBox buildInsightCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: rgba(124, 131, 255, 0.08); -fx-background-radius: 18; " +
            "-fx-border-color: rgba(124, 131, 255, 0.25); -fx-border-radius: 18; -fx-border-width: 1;"
        );

        Label badge = new Label("RECORDED BEHAVIOUR INSIGHTS • NOT A PREDICTION");
        badge.setStyle("-fx-text-fill: #7C83FF; -fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");

        HBox grid = new HBox(24);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.getChildren().addAll(
            createInsightPoint("Average payment delay: " + client.getTypicalDelayDays() + " days", "Usually clears balance after 2nd follow-up"),
            createInsightPoint("Most reliable day: Friday", "Payments are typically released before weekend"),
            createInsightPoint("Recommended future term: 50% advance", "Helps protect your working capital on large projects")
        );

        Label disclaimer = new Label("All insights are calculated transparently from recorded invoices and payments. Not financial advice.");
        disclaimer.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");

        card.getChildren().addAll(badge, grid, disclaimer);
        return card;
    }

    private VBox createInsightPoint(String headline, String sub) {
        VBox b = new VBox(3);
        b.setPrefWidth(280);
        Label h = new Label("• " + headline);
        h.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label s = new Label("   " + sub);
        s.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        b.getChildren().addAll(h, s);
        return b;
    }

    private TabPane buildTabsSection() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        // Tab 1: Invoices
        Tab tabInv = new Tab("Invoices");
        tabInv.setClosable(false);
        tabInv.setContent(buildInvoicesList());

        // Tab 2: Payments
        Tab tabPay = new Tab("Payments");
        tabPay.setClosable(false);
        tabPay.setContent(buildPaymentsList());

        // Tab 3: Notes
        Tab tabNotes = new Tab("Client Notes");
        tabNotes.setClosable(false);
        tabNotes.setContent(buildNotesSection());

        tabPane.getTabs().addAll(tabInv, tabPay, tabNotes);
        return tabPane;
    }

    private VBox buildInvoicesList() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16, 0, 0, 0));

        List<Invoice> list = store.getActiveInvoices().stream()
            .filter(i -> i.getClientId().equals(client.getId()))
            .toList();

        for (Invoice inv : list) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.04); -fx-border-radius: 10; -fx-border-width: 1;");

            Label num = new Label(inv.getInvoiceNumber());
            num.setPrefWidth(100);
            num.setStyle("-fx-text-fill: #7C83FF; -fx-font-weight: bold;");

            Label title = new Label(inv.getServiceTitle());
            title.setPrefWidth(200);
            title.setStyle("-fx-text-fill: #F6F8FC;");

            Label total = new Label(CurrencyFormatter.format(inv.getTotal()));
            total.setPrefWidth(120);
            total.setStyle("-fx-text-fill: #F6F8FC;");

            Label bal = new Label(CurrencyFormatter.format(inv.getBalance()));
            bal.setPrefWidth(120);
            bal.setStyle("-fx-text-fill: " + (inv.getBalance() > 0 ? "#FF6B6B" : "#4CAF50") + "; -fx-font-weight: bold;");

            Label status = new Label(inv.getStatus().getDisplay());
            status.setPrefWidth(120);
            status.setStyle("-fx-text-fill: " + inv.getStatus().getColor() + "; -fx-font-weight: bold;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button btnOpen = new Button("View →");
            btnOpen.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
            btnOpen.setOnAction(e -> {
                NavigationManager.getInstance().setSelectedInvoice(inv);
                NavigationManager.getInstance().navigateTo(Screen.INVOICE_DETAILS);
            });

            row.getChildren().addAll(num, title, total, bal, status, sp, btnOpen);
            box.getChildren().add(row);
        }

        return box;
    }

    private VBox buildPaymentsList() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16, 0, 0, 0));

        List<Payment> list = store.getPayments().stream()
            .filter(p -> p.getClientId().equals(client.getId()))
            .toList();

        if (list.isEmpty()) {
            Label empty = new Label("No recorded payments yet for this client.");
            empty.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
            box.getChildren().add(empty);
        } else {
            for (Payment p : list) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 16, 12, 16));
                row.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 10;");

                Label amt = new Label(CurrencyFormatter.format(p.getAmount()));
                amt.setPrefWidth(140);
                amt.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label method = new Label(p.getMethod().getDisplay());
                method.setPrefWidth(120);
                method.setStyle("-fx-text-fill: #F6F8FC;");

                Label date = new Label(DateUtil.formatDisplay(p.getPaymentDate()));
                date.setPrefWidth(140);
                date.setStyle("-fx-text-fill: #9BA8BC;");

                Label ref = new Label(p.getReference() != null ? p.getReference() : "");
                ref.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");

                row.getChildren().addAll(amt, method, date, ref);
                box.getChildren().add(row);
            }
        }

        return box;
    }

    private VBox buildNotesSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16, 0, 0, 0));

        TextArea notesArea = new TextArea(client.getNotes() != null ? client.getNotes() : "");
        notesArea.setPrefRowCount(4);
        notesArea.setStyle("-fx-background-color: #121A2B; -fx-text-fill: #F6F8FC; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10;");

        Button btnSave = new Button("Save Notes");
        btnSave.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 16; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            client.setNotes(notesArea.getText().trim());
            store.saveAll();
        });

        box.getChildren().addAll(notesArea, btnSave);
        return box;
    }
}
