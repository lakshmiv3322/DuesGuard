package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.service.PriorityScoreService;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import com.duesguard.view.components.Toast;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Screen 6: Invoices Management.
 * Filterable invoice table with status pills, priority rankings,
 * bulk actions, CSV export, and quick actions menu.
 */
public class InvoicesView extends ScrollPane {
    private final DataStore store;
    private final PriorityScoreService scoreService;
    private final VBox tableContainer;
    private String currentStatusFilter = "All";
    private String searchQuery = "";
    private final Set<Invoice> selectedInvoices = new HashSet<>();

    public InvoicesView() {
        this.store = AppContext.getInstance().getDataStore();
        this.scoreService = AppContext.getInstance().getPriorityScoreService();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // Header + Search + Export + Create Invoice
        content.getChildren().add(buildHeader());

        // Status Summary Cards
        content.getChildren().add(buildSummaryCards());

        // Table Container
        tableContainer = new VBox(10);
        content.getChildren().add(tableContainer);

        setContent(content);
        refreshTable();
    }

    private HBox buildHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Invoices");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Track issuance, balances, and payment-readiness");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search Field
        TextField search = new TextField();
        search.setPromptText("Filter invoices...");
        search.setStyle("-fx-background-color: #121A2B; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 7 12;");
        search.textProperty().addListener((obs, o, n) -> {
            searchQuery = n.trim().toLowerCase();
            refreshTable();
        });

        // Bulk Action: Export CSV
        Button btnExport = new Button("Export CSV");
        btnExport.setStyle(
            "-fx-background-color: #121A2B; -fx-text-fill: #F6F8FC; -fx-font-size: 13px; " +
            "-fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;"
        );
        btnExport.setOnAction(e -> exportInvoicesCSV());

        // Create Invoice Button
        Button btnCreate = new Button("+ Create invoice");
        btnCreate.setStyle(
            "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;"
        );
        btnCreate.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(null);
            NavigationManager.getInstance().navigateTo(Screen.CREATE_INVOICE);
        });

        header.getChildren().addAll(titleCol, spacer, search, btnExport, btnCreate);
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

        List<Invoice> active = store.getActiveInvoices();
        long totalCount = active.size();
        long pendingCount = active.stream().filter(i -> i.getStatus() == Invoice.Status.PENDING || i.getStatus() == Invoice.Status.SENT).count();
        long partialCount = active.stream().filter(i -> i.getStatus() == Invoice.Status.PARTIALLY_PAID).count();
        long paidCount = active.stream().filter(i -> i.getStatus() == Invoice.Status.PAID).count();
        long overdueCount = active.stream().filter(i -> i.getDaysOverdue() > 0).count();

        grid.add(createFilterCard("All invoices", totalCount, "#9BA8BC", "All"), 0, 0);
        grid.add(createFilterCard("Pending / Sent", pendingCount, "#7C83FF", "Pending"), 1, 0);
        grid.add(createFilterCard("Partially paid", partialCount, "#49E3B4", "Partially paid"), 2, 0);
        grid.add(createFilterCard("Paid", paidCount, "#4CAF50", "Paid"), 3, 0);
        grid.add(createFilterCard("Overdue", overdueCount, "#FF6B6B", "Overdue"), 4, 0);

        return grid;
    }

    private VBox createFilterCard(String title, long count, String colorHex, String statusKey) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 18, 14, 18));
        boolean active = currentStatusFilter.equals(statusKey);
        card.setStyle(
            "-fx-background-color: " + (active ? "rgba(73,227,180,0.1)" : "#121A2B") + "; " +
            "-fx-background-radius: 14; " +
            "-fx-border-color: " + (active ? "#49E3B4" : "rgba(255,255,255,0.06)") + "; " +
            "-fx-border-radius: 14; -fx-border-width: 1; -fx-cursor: hand;"
        );

        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");

        Label cLbl = new Label(String.valueOf(count));
        cLbl.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        card.getChildren().addAll(tLbl, cLbl);
        card.setOnMouseClicked(e -> {
            currentStatusFilter = statusKey;
            refreshTable();
        });
        return card;
    }

    private void refreshTable() {
        tableContainer.getChildren().clear();

        // Table Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setStyle("-fx-background-color: #0D1526; -fx-background-radius: 8;");

        header.getChildren().addAll(
            createHeaderCell("ID", 90),
            createHeaderCell("Client", 180),
            createHeaderCell("Service / Title", 180),
            createHeaderCell("Due Date", 110),
            createHeaderCell("Total", 100),
            createHeaderCell("Paid", 100),
            createHeaderCell("Balance", 100),
            createHeaderCell("Status", 120),
            createHeaderCell("Priority", 80)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer, createHeaderCell("Actions", 130));

        tableContainer.getChildren().add(header);

        // Filter invoices
        List<Invoice> list = store.getActiveInvoices().stream()
            .filter(inv -> {
                if ("All".equals(currentStatusFilter)) return true;
                if ("Pending".equals(currentStatusFilter)) return inv.getStatus() == Invoice.Status.PENDING || inv.getStatus() == Invoice.Status.SENT;
                if ("Partially paid".equals(currentStatusFilter)) return inv.getStatus() == Invoice.Status.PARTIALLY_PAID;
                if ("Paid".equals(currentStatusFilter)) return inv.getStatus() == Invoice.Status.PAID;
                if ("Overdue".equals(currentStatusFilter)) return inv.getDaysOverdue() > 0;
                return true;
            })
            .filter(inv -> {
                if (searchQuery.isEmpty()) return true;
                Client c = store.findClientById(inv.getClientId()).orElse(null);
                String clientName = c != null ? c.getName().toLowerCase() : "";
                return inv.getInvoiceNumber().toLowerCase().contains(searchQuery)
                    || inv.getServiceTitle().toLowerCase().contains(searchQuery)
                    || clientName.contains(searchQuery);
            })
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();

        if (list.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(48));
            Label emptyText = new Label("No invoices found for this filter.");
            emptyText.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
            empty.getChildren().add(emptyText);
            tableContainer.getChildren().add(empty);
            return;
        }

        for (Invoice inv : list) {
            Client c = store.findClientById(inv.getClientId()).orElse(null);
            tableContainer.getChildren().add(createInvoiceRow(inv, c));
        }
    }

    private Label createHeaderCell(String text, double width) {
        Label lbl = new Label(text);
        lbl.setPrefWidth(width);
        lbl.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px; -fx-font-weight: bold;");
        return lbl;
    }

    private HBox createInvoiceRow(Invoice inv, Client client) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 10; " +
            "-fx-border-color: rgba(255,255,255,0.04); -fx-border-radius: 10; -fx-border-width: 1;"
        );

        Label idLbl = new Label(inv.getInvoiceNumber());
        idLbl.setPrefWidth(90);
        idLbl.setStyle("-fx-text-fill: #7C83FF; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label clientLbl = new Label(client != null ? client.getName() : "Unknown");
        clientLbl.setPrefWidth(180);
        clientLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label serviceLbl = new Label(inv.getServiceTitle());
        serviceLbl.setPrefWidth(180);
        serviceLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        Label dueLbl = new Label(DateUtil.formatShort(inv.getDueDate()));
        dueLbl.setPrefWidth(110);
        dueLbl.setStyle("-fx-text-fill: " + (inv.getDaysOverdue() > 0 ? "#FF6B6B" : "#9BA8BC") + "; -fx-font-size: 12px;");

        Label totalLbl = new Label(CurrencyFormatter.format(inv.getTotal()));
        totalLbl.setPrefWidth(100);
        totalLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px;");

        Label paidLbl = new Label(CurrencyFormatter.format(inv.getAmountPaid()));
        paidLbl.setPrefWidth(100);
        paidLbl.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");

        Label balLbl = new Label(CurrencyFormatter.format(inv.getBalance()));
        balLbl.setPrefWidth(100);
        balLbl.setStyle("-fx-text-fill: " + (inv.getBalance() > 0 ? "#FF6B6B" : "#4CAF50") + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Status pill with icon and color
        HBox statusPill = createStatusPill(inv.getStatus());
        statusPill.setPrefWidth(120);

        // Priority score
        double score = scoreService.calculate(inv, client);
        Label priLbl = new Label(String.format("%.0f", score));
        priLbl.setPrefWidth(80);
        priLbl.setStyle("-fx-text-fill: " + scoreService.getPriorityColor(score) + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions: View details + Context menu
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = new Button("View");
        btnView.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        btnView.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.INVOICE_DETAILS);
        });

        MenuButton moreBtn = new MenuButton("•••");
        moreBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-cursor: hand;");

        MenuItem miPay = new MenuItem("Record Payment");
        miPay.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.INVOICE_DETAILS);
        });

        MenuItem miRemind = new MenuItem("Send Reminder");
        miRemind.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.REMINDER_COMPOSER);
        });

        MenuItem miDuplicate = new MenuItem("Duplicate");
        miDuplicate.setOnAction(e -> duplicateInvoice(inv));

        MenuItem miArchive = new MenuItem("Archive");
        miArchive.setOnAction(e -> {
            inv.setArchived(true);
            store.saveAll();
            Toast.show(this, inv.getInvoiceNumber() + " archived", true);
            refreshTable();
        });

        moreBtn.getItems().addAll(miPay, miRemind, miDuplicate, miArchive);
        actions.getChildren().addAll(btnView, moreBtn);

        row.getChildren().addAll(idLbl, clientLbl, serviceLbl, dueLbl, totalLbl, paidLbl, balLbl, statusPill, priLbl, spacer, actions);
        return row;
    }

    private HBox createStatusPill(Invoice.Status status) {
        HBox pill = new HBox(6);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setStyle(
            "-fx-background-color: " + getStatusBg(status) + "; " +
            "-fx-background-radius: 100; -fx-padding: 3 8;"
        );

        Circle dot = new Circle(3.5);
        dot.setFill(Color.web(status.getColor()));

        Label lbl = new Label(status.getDisplay());
        lbl.setStyle("-fx-text-fill: " + status.getColor() + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        pill.getChildren().addAll(dot, lbl);
        return pill;
    }

    private String getStatusBg(Invoice.Status s) {
        return switch (s) {
            case PAID -> "rgba(76, 175, 80, 0.15)";
            case OVERDUE -> "rgba(255, 107, 107, 0.15)";
            case PARTIALLY_PAID -> "rgba(73, 227, 180, 0.15)";
            case SENT -> "rgba(124, 131, 255, 0.15)";
            default -> "rgba(255, 184, 77, 0.15)";
        };
    }

    private void duplicateInvoice(Invoice original) {
        Invoice copy = original instanceof com.duesguard.model.ProductInvoice
            ? new com.duesguard.model.ProductInvoice()
            : new com.duesguard.model.ServiceInvoice();

        copy.setInvoiceNumber(original.getInvoiceNumber() + "-COPY");
        copy.setClientId(original.getClientId());
        copy.setServiceTitle(original.getServiceTitle() + " (Copy)");
        copy.setLineItems(new ArrayList<>(original.getLineItems()));
        copy.setDueDate(LocalDate.now().plusDays(15));
        copy.setStatus(Invoice.Status.DRAFT);
        store.addInvoice(copy);

        Toast.show(this, "Invoice duplicated as " + copy.getInvoiceNumber(), true);
        refreshTable();
    }

    private void exportInvoicesCSV() {
        try {
            File file = new File(System.getProperty("user.home") + File.separator + "duesguard-invoices.csv");
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("ID,InvoiceNumber,ClientID,Title,Total,Paid,Balance,Status,IssueDate,DueDate,Type");
                for (Invoice inv : store.getActiveInvoices()) {
                    pw.println(inv.toCSV());
                }
            }
            Toast.show(this, "Exported " + store.getActiveInvoices().size() + " invoices to " + file.getName(), true);
        } catch (Exception e) {
            Toast.show(this, "Export failed: " + e.getMessage(), false);
        }
    }
}
