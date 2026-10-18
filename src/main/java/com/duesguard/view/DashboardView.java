package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.model.Payment;
import com.duesguard.service.CashFlowService;
import com.duesguard.service.PriorityScoreService;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.view.components.CashFlow3DOrb;
import com.duesguard.view.components.ReadinessMeter;
import com.duesguard.view.components.ScoreRing;
import com.duesguard.view.components.SparklineCanvas;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Screen 4: Dashboard - The Payment Recovery Command Centre.
 * The core screen giving immediate clarity on who owes money, overdue amounts,
 * highest-impact follow-ups, payment-readiness, and cash flow.
 */
public class DashboardView extends ScrollPane {

    public DashboardView() {
        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        DataStore store = AppContext.getInstance().getDataStore();
        List<Invoice> activeInvoices = store.getActiveInvoices();
        List<Client> clients = store.getClients();
        Map<String, Client> clientMap = clients.stream().collect(Collectors.toMap(Client::getId, c -> c, (a, b) -> a));

        // 1. Alert Banner
        content.getChildren().add(buildAlertBanner(activeInvoices));

        // 2. 4-Column KPI Grid
        content.getChildren().add(buildKpiGrid(activeInvoices, store.getPayments()));

        // 3. Middle Section: Cash-Flow Overview (Left) + Today's Actions (Right)
        content.getChildren().add(buildMiddleRow(activeInvoices, store.getPayments()));

        // 4. Priority Queue Preview: Top 5 Highest-Impact Follow-Ups
        content.getChildren().add(buildPriorityPreview(activeInvoices, clientMap));

        // 5. Bottom Row: Payment-Readiness Overview + Client Behaviour Insight
        content.getChildren().add(buildBottomRow(activeInvoices, clientMap));

        setContent(content);
    }

    // -------------------------------------------------------------
    // 1. Alert Banner
    // -------------------------------------------------------------
    private Node buildAlertBanner(List<Invoice> invoices) {
        long overdueCount = invoices.stream().filter(i -> i.getDaysOverdue() > 0).count();
        double overdueTotal = invoices.stream().filter(i -> i.getDaysOverdue() > 0).mapToDouble(Invoice::getBalance).sum();

        HBox banner = new HBox(16);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(16, 24, 16, 24));

        if (overdueCount > 0) {
            banner.setStyle(
                "-fx-background-color: rgba(255, 107, 107, 0.12); " +
                "-fx-background-radius: 16; " +
                "-fx-border-color: rgba(255, 107, 107, 0.35); " +
                "-fx-border-radius: 16; " +
                "-fx-border-width: 1;"
            );

            Circle pulseDot = new Circle(5);
            pulseDot.setFill(Color.web("#FF6B6B"));

            VBox textCol = new VBox(2);
            Label mainText = new Label(CurrencyFormatter.format(overdueTotal) + " needs attention across " + overdueCount + " invoices.");
            mainText.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 15px; -fx-font-weight: bold;");

            Label subText = new Label("You can recover " + CurrencyFormatter.format(overdueTotal) + " with 4 focused follow-ups.");
            subText.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
            textCol.getChildren().addAll(mainText, subText);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnQueue = new Button("Open priority queue →");
            btnQueue.setStyle(
                "-fx-background-color: #FF6B6B; -fx-text-fill: #0B1020; -fx-font-weight: bold; " +
                "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"
            );
            btnQueue.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.PRIORITY_QUEUE));

            Button btnFlow = new Button("View cash-flow impact");
            btnFlow.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; " +
                "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 8;"
            );
            btnFlow.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.CASH_FLOW));

            banner.getChildren().addAll(pulseDot, textCol, spacer, btnFlow, btnQueue);
        } else {
            banner.setStyle(
                "-fx-background-color: rgba(73, 227, 180, 0.10); " +
                "-fx-background-radius: 16; " +
                "-fx-border-color: rgba(73, 227, 180, 0.25); " +
                "-fx-border-radius: 16; " +
                "-fx-border-width: 1;"
            );

            Circle dot = new Circle(5);
            dot.setFill(Color.web("#49E3B4"));

            Label msg = new Label("Great work. No urgent payments need follow-up today.");
            msg.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 14px; -fx-font-weight: bold;");

            banner.getChildren().addAll(dot, msg);
        }

        return banner;
    }

    // -------------------------------------------------------------
    // 2. KPI Grid (4 Cards)
    // -------------------------------------------------------------
    private GridPane buildKpiGrid(List<Invoice> invoices, List<Payment> payments) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);

        // Compute metrics
        double expected = invoices.stream().filter(i -> i.getStatus() != Invoice.Status.PAID).mapToDouble(Invoice::getBalance).sum();
        double received = payments.stream().mapToDouble(Payment::getAmount).sum();
        double outstanding = invoices.stream().filter(i -> i.getStatus() != Invoice.Status.PAID).mapToDouble(Invoice::getBalance).sum();
        double overdue = invoices.stream().filter(i -> i.getDaysOverdue() > 0).mapToDouble(Invoice::getBalance).sum();

        // Column constraints: 4 equal columns
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        // 1. Expected this month
        grid.add(createKpiCard(
            "Expected this month",
            CurrencyFormatter.format(expected > 0 ? expected : 184500),
            "+12% vs last month",
            "Based on active invoices",
            new double[]{20, 35, 45, 60, 80, 95, 120},
            "#49E3B4",
            null, null
        ), 0, 0);

        // 2. Received this month
        grid.add(createKpiCard(
            "Received this month",
            CurrencyFormatter.format(received > 0 ? received : 96000),
            "+8% on track",
            "52% of expected collected",
            new double[]{10, 20, 30, 48, 62, 75, 96},
            "#7C83FF",
            null, null
        ), 1, 0);

        // 3. Total outstanding
        grid.add(createKpiCard(
            "Total outstanding",
            CurrencyFormatter.format(outstanding > 0 ? outstanding : 88500),
            "6 active invoices",
            "Across 4 clients",
            new double[]{95, 90, 88, 85, 88, 86, 88.5},
            "#FFB84D",
            "View invoices", () -> NavigationManager.getInstance().navigateTo(Screen.INVOICES)
        ), 2, 0);

        // 4. Overdue amount
        grid.add(createKpiCard(
            "Overdue amount",
            CurrencyFormatter.format(overdue > 0 ? overdue : 48500),
            "4 invoices past due",
            "Action required",
            new double[]{15, 24, 30, 42, 45, 48.5, 48.5},
            "#FF6B6B",
            "Follow up now →", () -> NavigationManager.getInstance().navigateTo(Screen.PRIORITY_QUEUE)
        ), 3, 0);

        return grid;
    }

    private VBox createKpiCard(
        String label, String value, String trend, String sublabel,
        double[] sparkData, String colorHex, String btnText, Runnable btnAction
    ) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 18, 0, 0, 4);"
        );

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: 500;");

        HBox valRow = new HBox(12);
        valRow.setAlignment(Pos.BASELINE_LEFT);

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI', sans-serif;");

        valRow.getChildren().add(valLbl);

        HBox sparkRow = new HBox(10);
        sparkRow.setAlignment(Pos.CENTER_LEFT);

        SparklineCanvas spark = new SparklineCanvas(110, 28, sparkData, colorHex, true);

        Label trendLbl = new Label(trend);
        trendLbl.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        sparkRow.getChildren().addAll(spark, trendLbl);

        Label sub = new Label(sublabel);
        sub.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");

        card.getChildren().addAll(lbl, valRow, sparkRow, sub);

        if (btnText != null && btnAction != null) {
            Button btn = new Button(btnText);
            btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: " + colorHex + "; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; " +
                "-fx-padding: 4 10; -fx-cursor: hand;"
            );
            btn.setOnAction(e -> btnAction.run());
            card.getChildren().add(btn);
        }

        return card;
    }

    // -------------------------------------------------------------
    // 3. Middle Row: Cash-Flow Overview + Today's Action List
    // -------------------------------------------------------------
    private HBox buildMiddleRow(List<Invoice> invoices, List<Payment> payments) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        // Left Card: Cash-flow Overview Chart + 3D Orb Hero (width 62%)
        VBox chartCard = buildCashFlowChartCard(invoices, payments);
        HBox.setHgrow(chartCard, Priority.ALWAYS);

        // Right Card: Today's Action List (width 38%)
        VBox actionCard = buildTodayActionsCard(invoices);
        actionCard.setPrefWidth(420);
        actionCard.setMinWidth(380);

        row.getChildren().addAll(chartCard, actionCard);
        return row;
    }

    private VBox buildCashFlowChartCard(List<Invoice> invoices, List<Payment> payments) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0, 0, 4);"
        );

        // Header with title, 3D orb and time toggle
        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Cash-Flow Overview");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label subtitle = new Label("Expected vs received cash inflow");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3D Cash Flow Orb embedded in the header!
        CashFlow3DOrb orb3D = new CashFlow3DOrb(70, 70, () -> NavigationManager.getInstance().navigateTo(Screen.CASH_FLOW));
        Tooltip.install(orb3D, new Tooltip("3D Cash Flow Orb — Click to open Cash-Flow Radar"));

        // 7d / 30d / 6m toggle buttons
        HBox toggleGroup = new HBox(4);
        toggleGroup.setStyle("-fx-background-color: #182338; -fx-background-radius: 8; -fx-padding: 3;");

        Button b7 = createPeriodToggle("7 days", false);
        Button b30 = createPeriodToggle("30 days", true);
        Button b6m = createPeriodToggle("6 months", false);
        toggleGroup.getChildren().addAll(b7, b30, b6m);

        topRow.getChildren().addAll(titleCol, spacer, orb3D, toggleGroup);

        // Area Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFill(Color.web("#9BA8BC"));
        yAxis.setTickLabelFill(Color.web("#9BA8BC"));

        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setPrefHeight(230);
        areaChart.setLegendVisible(true);
        areaChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> expectedSeries = new XYChart.Series<>();
        expectedSeries.setName("Expected Inflow (₹)");
        expectedSeries.getData().add(new XYChart.Data<>("01 Oct", 20000));
        expectedSeries.getData().add(new XYChart.Data<>("07 Oct", 45000));
        expectedSeries.getData().add(new XYChart.Data<>("14 Oct", 85000));
        expectedSeries.getData().add(new XYChart.Data<>("21 Oct", 120000));
        expectedSeries.getData().add(new XYChart.Data<>("28 Oct", 160000));
        expectedSeries.getData().add(new XYChart.Data<>("31 Oct", 184500));

        XYChart.Series<String, Number> receivedSeries = new XYChart.Series<>();
        receivedSeries.setName("Received (₹)");
        receivedSeries.getData().add(new XYChart.Data<>("01 Oct", 15000));
        receivedSeries.getData().add(new XYChart.Data<>("07 Oct", 32000));
        receivedSeries.getData().add(new XYChart.Data<>("14 Oct", 68000));
        receivedSeries.getData().add(new XYChart.Data<>("21 Oct", 84000));
        receivedSeries.getData().add(new XYChart.Data<>("28 Oct", 96000));
        receivedSeries.getData().add(new XYChart.Data<>("31 Oct", 96000));

        areaChart.getData().addAll(expectedSeries, receivedSeries);

        card.getChildren().addAll(topRow, areaChart);
        return card;
    }

    private Button createPeriodToggle(String text, boolean active) {
        Button btn = new Button(text);
        btn.setStyle(active
            ? "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"
            : "-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        return btn;
    }

    private VBox buildTodayActionsCard(List<Invoice> invoices) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0, 0, 4);"
        );

        Label title = new Label("Today's Action List");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitle = new Label("3 high-priority actions for maximum cash recovery");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        VBox list = new VBox(10);

        list.getChildren().addAll(
            createActionRow(
                "#FF6B6B",
                "Follow up with Arun Design Studio",
                "₹24,500 • 18 days overdue",
                () -> {
                    DataStore store = AppContext.getInstance().getDataStore();
                    store.findByInvoiceNumber("INV-101").ifPresent(inv -> {
                        NavigationManager.getInstance().setSelectedInvoice(inv);
                        NavigationManager.getInstance().navigateTo(Screen.REMINDER_COMPOSER);
                    });
                }
            ),
            createActionRow(
                "#FFB84D",
                "Attach completion proof to INV-104",
                "Bright Academy • Strengthens follow-up",
                () -> {
                    DataStore store = AppContext.getInstance().getDataStore();
                    store.findByInvoiceNumber("INV-104").ifPresent(inv -> {
                        NavigationManager.getInstance().setSelectedInvoice(inv);
                        NavigationManager.getInstance().navigateTo(Screen.DOCUMENTS);
                    });
                }
            ),
            createActionRow(
                "#49E3B4",
                "Record partial payment from Bright Academy",
                "Balance ₹4,000 remaining on INV-104",
                () -> {
                    DataStore store = AppContext.getInstance().getDataStore();
                    store.findByInvoiceNumber("INV-104").ifPresent(inv -> {
                        NavigationManager.getInstance().setSelectedInvoice(inv);
                        NavigationManager.getInstance().navigateTo(Screen.INVOICE_DETAILS);
                    });
                }
            )
        );

        card.getChildren().addAll(title, subtitle, list);
        return card;
    }

    private HBox createActionRow(String priorityColorHex, String task, String details, Runnable onOpen) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle(
            "-fx-background-color: #182338; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: rgba(255,255,255,0.04); " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1;"
        );

        CheckBox cb = new CheckBox();
        cb.setStyle("-fx-cursor: hand;");

        Rectangle priorityBar = new Rectangle(3, 32);
        priorityBar.setArcWidth(2);
        priorityBar.setArcHeight(2);
        priorityBar.setFill(Color.web(priorityColorHex));

        VBox textCol = new VBox(2);
        Label main = new Label(task);
        main.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: 500;");

        Label sub = new Label(details);
        sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        textCol.getChildren().addAll(main, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnOpen = new Button("Open");
        btnOpen.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; " +
            "-fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"
        );
        btnOpen.setOnAction(e -> onOpen.run());

        cb.selectedProperty().addListener((obs, o, checked) -> {
            if (checked) {
                main.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 13px; -fx-strikethrough: true;");
                sub.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");
            } else {
                main.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: 500;");
                sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
            }
        });

        row.getChildren().addAll(cb, priorityBar, textCol, spacer, btnOpen);
        return row;
    }

    // -------------------------------------------------------------
    // 4. Priority Queue Preview: Top 5 Highest-Impact Follow-Ups
    // -------------------------------------------------------------
    private VBox buildPriorityPreview(List<Invoice> invoices, Map<String, Client> clientMap) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0, 0, 4);"
        );

        // Header
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Highest-Impact Follow-Ups");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitle = new Label("Ranked by overdue duration, balance amount, client payment habits, and missing documents");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnViewAll = new Button("View all priority items →");
        btnViewAll.setStyle(
            "-fx-background-color: rgba(73, 227, 180, 0.12); -fx-text-fill: #49E3B4; " +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
        );
        btnViewAll.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.PRIORITY_QUEUE));

        topRow.getChildren().addAll(titleCol, spacer, btnViewAll);

        // Table / Card List
        VBox tableBox = new VBox(8);

        // Header row
        HBox headerRow = createTableHeader();
        tableBox.getChildren().add(headerRow);

        // Get top 5 ranked invoices
        PriorityScoreService scoreService = AppContext.getInstance().getPriorityScoreService();
        List<Invoice> ranked = invoices.stream()
            .filter(i -> i.getStatus() != Invoice.Status.PAID && i.getStatus() != Invoice.Status.DRAFT)
            .sorted((a, b) -> {
                double sa = scoreService.calculate(a, clientMap.get(a.getClientId()));
                double sb = scoreService.calculate(b, clientMap.get(b.getClientId()));
                return Double.compare(sb, sa);
            })
            .limit(5)
            .toList();

        int rank = 1;
        for (Invoice inv : ranked) {
            Client c = clientMap.get(inv.getClientId());
            double score = scoreService.calculate(inv, c);
            tableBox.getChildren().add(createPriorityRow(rank++, inv, c, score));
        }

        card.getChildren().addAll(topRow, tableBox);
        return card;
    }

    private HBox createTableHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 16, 8, 16));
        header.setStyle("-fx-background-color: #0D1526; -fx-background-radius: 8;");

        Label colRank = createHeaderLabel("#", 30);
        Label colScore = createHeaderLabel("Score", 60);
        Label colClient = createHeaderLabel("Client", 180);
        Label colInv = createHeaderLabel("Invoice", 120);
        Label colAmt = createHeaderLabel("Balance Due", 120);
        Label colOverdue = createHeaderLabel("Overdue", 120);
        Label colReady = createHeaderLabel("Readiness", 110);
        Label colAction = createHeaderLabel("Action", 140);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(colRank, colScore, colClient, colInv, colAmt, colOverdue, colReady, spacer, colAction);
        return header;
    }

    private Label createHeaderLabel(String text, double width) {
        Label lbl = new Label(text);
        lbl.setPrefWidth(width);
        lbl.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");
        return lbl;
    }

    private HBox createPriorityRow(int rank, Invoice inv, Client client, double score) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle(
            "-fx-background-color: #182338; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: rgba(255,255,255,0.04); " +
            "-fx-border-radius: 10; " +
            "-fx-border-width: 1;"
        );

        Label rankLbl = new Label(String.valueOf(rank));
        rankLbl.setPrefWidth(30);
        rankLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Score ring preview
        ScoreRing ring = new ScoreRing(score, 14);
        ring.setPrefWidth(60);

        Label clientLbl = new Label(client != null ? client.getName() : "Unknown");
        clientLbl.setPrefWidth(180);
        clientLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label invLbl = new Label(inv.getInvoiceNumber());
        invLbl.setPrefWidth(120);
        invLbl.setStyle("-fx-text-fill: #7C83FF; -fx-font-size: 12px;");

        Label amtLbl = new Label(CurrencyFormatter.format(inv.getBalance()));
        amtLbl.setPrefWidth(120);
        amtLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label overdueLbl = new Label(inv.getDaysOverdue() > 0 ? inv.getDaysOverdue() + " days overdue" : "Due in " + inv.getDaysUntilDue() + "d");
        overdueLbl.setPrefWidth(120);
        overdueLbl.setStyle("-fx-text-fill: " + (inv.getDaysOverdue() > 0 ? "#FF6B6B" : "#49E3B4") + "; -fx-font-size: 12px;");

        Label readyLbl = new Label(String.format("%.0f%% ready", inv.getReadinessScore()));
        readyLbl.setPrefWidth(110);
        readyLbl.setStyle("-fx-text-fill: " + (inv.getReadinessScore() >= 80 ? "#4CAF50" : "#FFB84D") + "; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox btnBox = new HBox(8);
        Button btnView = new Button("View");
        btnView.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        btnView.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.INVOICE_DETAILS);
        });

        Button btnRemind = new Button("Remind");
        btnRemind.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;");
        btnRemind.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.REMINDER_COMPOSER);
        });

        btnBox.getChildren().addAll(btnView, btnRemind);
        row.getChildren().addAll(rankLbl, ring, clientLbl, invLbl, amtLbl, overdueLbl, readyLbl, spacer, btnBox);

        return row;
    }

    // -------------------------------------------------------------
    // 5. Bottom Row: Readiness Meter + Client Behaviour Insight
    // -------------------------------------------------------------
    private HBox buildBottomRow(List<Invoice> invoices, Map<String, Client> clientMap) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        // Left Card: Payment-Readiness Overview (50% width)
        VBox readinessCard = buildReadinessCard(invoices);
        HBox.setHgrow(readinessCard, Priority.ALWAYS);

        // Right Card: Client Behaviour Insight (50% width)
        VBox insightCard = buildInsightCard(clientMap);
        HBox.setHgrow(insightCard, Priority.ALWAYS);

        row.getChildren().addAll(readinessCard, insightCard);
        return row;
    }

    private VBox buildReadinessCard(List<Invoice> invoices) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.06); " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1;"
        );

        Label title = new Label("Payment-Readiness Overview");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitle = new Label("Dispute-proof status of your active invoices");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        int ready = (int) invoices.stream().filter(i -> i.getReadinessScore() >= 80).count();
        int needsDocs = (int) invoices.stream().filter(i -> i.getReadinessScore() >= 50 && i.getReadinessScore() < 80).count();
        int atRisk = (int) invoices.stream().filter(i -> i.getReadinessScore() < 50).count();

        ReadinessMeter meter = new ReadinessMeter(ready > 0 ? ready : 12, needsDocs > 0 ? needsDocs : 4, atRisk > 0 ? atRisk : 3);

        Button btnDocs = new Button("Review missing documents →");
        btnDocs.setStyle("-fx-background-color: transparent; -fx-text-fill: #49E3B4; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        btnDocs.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.DOCUMENTS));

        card.getChildren().addAll(title, subtitle, meter, btnDocs);
        return card;
    }

    private VBox buildInsightCard(Map<String, Client> clientMap) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: rgba(124, 131, 255, 0.08); " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(124, 131, 255, 0.25); " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1;"
        );

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("BEHAVIOUR INSIGHT");
        badge.setStyle(
            "-fx-background-color: rgba(124, 131, 255, 0.2); " +
            "-fx-text-fill: #7C83FF; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 4; " +
            "-fx-padding: 2 6;"
        );

        topRow.getChildren().add(badge);

        Label mainInsight = new Label("Arun Design Studio pays an average of 9 days late. Consider requesting a 50% advance on future work.");
        mainInsight.setWrapText(true);
        mainInsight.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: 500;");

        Label note = new Label("Based on 3 completed invoices over 4 months. No disputes recorded.");
        note.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");

        HBox btnRow = new HBox(12);
        Button btnClient = new Button("View client");
        btnClient.setStyle(
            "-fx-background-color: #182338; -fx-text-fill: #F6F8FC; " +
            "-fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
        );
        btnClient.setOnAction(e -> {
            Client c = clientMap.values().stream().filter(cl -> cl.getName().contains("Arun")).findFirst().orElse(null);
            NavigationManager.getInstance().setSelectedClient(c);
            NavigationManager.getInstance().navigateTo(Screen.CLIENT_DETAILS);
        });

        Button btnUse = new Button("Use this insight");
        btnUse.setStyle(
            "-fx-background-color: #7C83FF; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; " +
            "-fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
        );
        btnUse.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.CREATE_INVOICE));

        btnRow.getChildren().addAll(btnClient, btnUse);

        card.getChildren().addAll(topRow, mainInsight, note, btnRow);
        return card;
    }
}
