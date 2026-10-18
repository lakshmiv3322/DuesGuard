package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.*;
import com.duesguard.service.CashFlowService;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.List;

/**
 * Screen 13: Cash-Flow Radar & What-If Simulator.
 * Shows expected vs actual inflows, 3 projection scenarios (Best, Likely, Conservative),
 * cash runway projections, upcoming expenses, and the interactive 'What If?' Simulator.
 */
public class CashFlowView extends ScrollPane {
    private final DataStore store;
    private final CashFlowService cashFlowService;
    private final VBox content;

    public CashFlowView() {
        this.store = AppContext.getInstance().getDataStore();
        this.cashFlowService = AppContext.getInstance().getCashFlowService();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        content = new VBox(24);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // 1. Header
        content.getChildren().add(buildHeader());

        // 2. KPI Cards (Expected, Received, Outstanding, Predicted Gap)
        content.getChildren().add(buildKpis());

        // 3. Main Chart (Expected Inflow vs Actual vs Overdue)
        content.getChildren().add(buildMainChartCard());

        // 4. Middle Row: 3 Forecast Scenarios (Left) + "What If?" Simulator (Right)
        content.getChildren().add(buildMiddleRow());

        // 5. Bottom Row: Cash Runway Card + Upcoming Cash Events Timeline
        content.getChildren().add(buildBottomRow());

        setContent(content);
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Cash-flow radar");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("See what is expected, what is received, and where cash risk may appear.");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAddExpense = new Button("+ Add Expense");
        btnAddExpense.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnAddExpense.setOnAction(e -> openAddExpenseDialog());

        Button btnSim = new Button("🎲 Run Simulator");
        btnSim.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;");
        btnSim.setOnAction(e -> openWhatIfDialog());

        header.getChildren().addAll(titleCol, spacer, btnAddExpense, btnSim);
        return header;
    }

    private GridPane buildKpis() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        CashFlowService.CashFlowSummary summary = cashFlowService.getSummary(store.getActiveInvoices(), store.getPayments());

        grid.add(createKpiCard("Expected Inflow", CurrencyFormatter.format(summary.expectedThisMonth()), "Active invoices due this month", "#49E3B4"), 0, 0);
        grid.add(createKpiCard("Received Inflow", CurrencyFormatter.format(summary.receivedThisMonth()), "52% of expected collected", "#7C83FF"), 1, 0);
        grid.add(createKpiCard("Total Outstanding", CurrencyFormatter.format(summary.totalOutstanding()), "Pending collection", "#FFB84D"), 2, 0);
        grid.add(createKpiCard("Predicted Collection Gap", CurrencyFormatter.format(Math.max(0, summary.predictedGap())), "Requires 4 follow-ups to close", "#FF6B6B"), 3, 0);

        return grid;
    }

    private VBox createKpiCard(String lbl, String val, String sub, String colorHex) {
        VBox b = new VBox(6);
        b.setPadding(new Insets(16, 20, 16, 20));
        b.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1;");
        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        Label v = new Label(val);
        v.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");
        b.getChildren().addAll(l, v, s);
        return b;
    }

    private VBox buildMainChartCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Expected Inflow vs Actual Inflow vs Overdue");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox toggles = new HBox(4);
        toggles.setStyle("-fx-background-color: #182338; -fx-background-radius: 8; -fx-padding: 3;");
        Button bDay = createMiniBtn("Daily", false);
        Button bWk = createMiniBtn("Weekly", true);
        Button bMo = createMiniBtn("Monthly", false);
        toggles.getChildren().addAll(bDay, bWk, bMo);

        top.getChildren().addAll(title, sp, toggles);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFill(Color.web("#9BA8BC"));
        yAxis.setTickLabelFill(Color.web("#9BA8BC"));

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setPrefHeight(240);
        barChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> expectedSeries = new XYChart.Series<>();
        expectedSeries.setName("Expected (₹)");
        expectedSeries.getData().add(new XYChart.Data<>("Week 1", 35000));
        expectedSeries.getData().add(new XYChart.Data<>("Week 2", 48000));
        expectedSeries.getData().add(new XYChart.Data<>("Week 3", 62000));
        expectedSeries.getData().add(new XYChart.Data<>("Week 4", 39500));

        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Received (₹)");
        actualSeries.getData().add(new XYChart.Data<>("Week 1", 35000));
        actualSeries.getData().add(new XYChart.Data<>("Week 2", 48000));
        actualSeries.getData().add(new XYChart.Data<>("Week 3", 13000));
        actualSeries.getData().add(new XYChart.Data<>("Week 4", 0));

        XYChart.Series<String, Number> overdueSeries = new XYChart.Series<>();
        overdueSeries.setName("Overdue (₹)");
        overdueSeries.getData().add(new XYChart.Data<>("Week 1", 0));
        overdueSeries.getData().add(new XYChart.Data<>("Week 2", 0));
        overdueSeries.getData().add(new XYChart.Data<>("Week 3", 24500));
        overdueSeries.getData().add(new XYChart.Data<>("Week 4", 24000));

        barChart.getData().addAll(expectedSeries, actualSeries, overdueSeries);

        card.getChildren().addAll(top, barChart);
        return card;
    }

    private Button createMiniBtn(String text, boolean active) {
        Button b = new Button(text);
        b.setStyle(active
            ? "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"
            : "-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        return b;
    }

    private HBox buildMiddleRow() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        // Left: 3 Scenarios
        VBox scenarioCard = buildScenariosCard();
        HBox.setHgrow(scenarioCard, Priority.ALWAYS);

        // Right: "What If?" Simulator teaser
        VBox whatIfCard = buildWhatIfTeaser();
        whatIfCard.setPrefWidth(420);
        whatIfCard.setMinWidth(380);

        row.getChildren().addAll(scenarioCard, whatIfCard);
        return row;
    }

    private VBox buildScenariosCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Three-Scenario Inflow Projections");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label sub = new Label("Calculated using transparent rules: historical delay and invoice due dates.");
        sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        List<CashFlowService.Scenario> scenarios = cashFlowService.getScenarios(
            store.getActiveInvoices(), store.getPayments(), store.getExpenses(), store.getClients()
        );

        VBox list = new VBox(10);
        for (CashFlowService.Scenario sc : scenarios) {
            HBox scRow = new HBox(12);
            scRow.setAlignment(Pos.CENTER_LEFT);
            scRow.setPadding(new Insets(14));
            scRow.setStyle("-fx-background-color: #182338; -fx-background-radius: 12;");

            VBox textCol = new VBox(2);
            Label name = new Label(sc.name());
            name.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: bold;");
            Label d = new Label(sc.description());
            d.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
            textCol.getChildren().addAll(name, d);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label val = new Label(CurrencyFormatter.format(sc.projectedBalance()));
            String col = sc.name().contains("Best") ? "#49E3B4" : (sc.name().contains("Likely") ? "#7C83FF" : "#FFB84D");
            val.setStyle("-fx-text-fill: " + col + "; -fx-font-size: 16px; -fx-font-weight: bold;");

            scRow.getChildren().addAll(textCol, sp, val);
            list.getChildren().add(scRow);
        }

        card.getChildren().addAll(title, sub, list);
        return card;
    }

    private VBox buildWhatIfTeaser() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: rgba(73, 227, 180, 0.08); -fx-background-radius: 20; " +
            "-fx-border-color: rgba(73, 227, 180, 0.25); -fx-border-radius: 20; -fx-border-width: 1;"
        );

        Label badge = new Label("🎲 CASH-FLOW 'WHAT IF?' SIMULATOR");
        badge.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");

        Label question = new Label("What happens if your two largest overdue invoices are paid this week?");
        question.setWrapText(true);
        question.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 15px; -fx-font-weight: bold;");

        VBox results = new VBox(6);
        results.getChildren().addAll(
            createResultLine("New projected balance:", "+ ₹66,500 inflow", "#49E3B4"),
            createResultLine("Expense coverage:", "100% of upcoming rent & salaries", "#4CAF50"),
            createResultLine("Cash-flow risk:", "Drops from HIGH to SAFE", "#7C83FF")
        );

        Button btnRun = new Button("Open Interactive Simulator →");
        btnRun.setMaxWidth(Double.MAX_VALUE);
        btnRun.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 9; -fx-cursor: hand;");
        btnRun.setOnAction(e -> openWhatIfDialog());

        card.getChildren().addAll(badge, question, results, btnRun);
        return card;
    }

    private HBox createResultLine(String label, String val, String colorHex) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label v = new Label(val);
        v.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        r.getChildren().addAll(l, sp, v);
        return r;
    }

    private HBox buildBottomRow() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        // Left: Cash Runway Card
        VBox runwayCard = buildRunwayCard();
        HBox.setHgrow(runwayCard, Priority.ALWAYS);

        // Right: Upcoming Cash Events Timeline
        VBox timelineCard = buildUpcomingEventsCard();
        HBox.setHgrow(timelineCard, Priority.ALWAYS);

        row.getChildren().addAll(runwayCard, timelineCard);
        return row;
    }

    private VBox buildRunwayCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Cash Runway & Expense Cushion");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        double totalExpenses = store.getExpenses().stream().mapToDouble(Expense::getAmount).sum();

        Label alert = new Label("Based on recorded expected payments, your projected cash balance may be tight around 23 October if GreenLeaf Events (₹42,000) does not clear.");
        alert.setWrapText(true);
        alert.setStyle("-fx-text-fill: #FFB84D; -fx-font-size: 13px; -fx-font-weight: 500;");

        Label expenseSummary = new Label("Monthly committed expenses: " + CurrencyFormatter.format(totalExpenses > 0 ? totalExpenses : 24500) + " (Rent: ₹12,000 • Salaries: ₹8,000 • Tools: ₹4,500)");
        expenseSummary.setWrapText(true);
        expenseSummary.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        Label disclaimer = new Label("This is a planning estimate, not financial advice.");
        disclaimer.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px; -fx-font-style: italic;");

        card.getChildren().addAll(title, alert, expenseSummary, disclaimer);
        return card;
    }

    private VBox buildUpcomingEventsCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Upcoming Cash Events Timeline");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox events = new VBox(8);
        events.getChildren().addAll(
            createEventRow("Rent Payment - Studio", "Due in 5 days", "- ₹12,000", "#FF6B6B"),
            createEventRow("Expected: Bright Academy (INV-102)", "Due in 3 days", "+ ₹18,000", "#49E3B4"),
            createEventRow("Software Subscriptions (Adobe)", "Due in 10 days", "- ₹4,500", "#FF6B6B"),
            createEventRow("Intern Stipend", "Due in 12 days", "- ₹8,000", "#FF6B6B")
        );

        card.getChildren().addAll(title, events);
        return card;
    }

    private HBox createEventRow(String name, String date, String amt, String amtColor) {
        HBox r = new HBox(12);
        r.setAlignment(Pos.CENTER_LEFT);
        r.setPadding(new Insets(8, 12, 8, 12));
        r.setStyle("-fx-background-color: #182338; -fx-background-radius: 8;");

        VBox text = new VBox(1);
        Label n = new Label(name);
        n.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-font-weight: 500;");
        Label d = new Label(date);
        d.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 10px;");
        text.getChildren().addAll(n, d);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label a = new Label(amt);
        a.setStyle("-fx-text-fill: " + amtColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        r.getChildren().addAll(text, sp, a);
        return r;
    }

    private void openWhatIfDialog() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.85);");
        overlay.setPrefSize(getWidth(), getHeight());

        VBox card = new VBox(20);
        card.setMaxWidth(520);
        card.setPadding(new Insets(32));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 24; -fx-border-color: rgba(73, 227, 180, 0.3); -fx-border-radius: 24; -fx-border-width: 1.5;");

        Label badge = new Label("🎲 WHAT-IF SCENARIO SIMULATOR");
        badge.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label title = new Label("Simulate Recovery Impact");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Scenario 1
        VBox s1 = new VBox(6);
        s1.setPadding(new Insets(14));
        s1.setStyle("-fx-background-color: #182338; -fx-background-radius: 12; -fx-border-color: rgba(73,227,180,0.3); -fx-border-radius: 12;");
        Label s1Title = new Label("Scenario A: GreenLeaf (₹42K) & Arun (₹24.5K) pay this week");
        s1Title.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label s1Detail = new Label("Cash balance: ₹1,62,500 • Full runway through December • Risk: ZERO");
        s1Detail.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        s1.getChildren().addAll(s1Title, s1Detail);

        // Scenario 2
        VBox s2 = new VBox(6);
        s2.setPadding(new Insets(14));
        s2.setStyle("-fx-background-color: #182338; -fx-background-radius: 12; -fx-border-color: rgba(255,107,107,0.3); -fx-border-radius: 12;");
        Label s2Title = new Label("Scenario B: Both invoices delayed by 14 more days");
        s2Title.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label s2Detail = new Label("Cash balance: Tight around 23 Oct • Rent deficit of ₹6,500 • Risk: HIGH");
        s2Detail.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        s2.getChildren().addAll(s2Title, s2Detail);

        Button btnClose = new Button("Close Simulator");
        btnClose.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        btnClose.setOnAction(e -> ((Pane) getParent()).getChildren().remove(overlay));

        card.getChildren().addAll(badge, title, s1, s2, btnClose);
        overlay.getChildren().add(card);
        ((Pane) getParent()).getChildren().add(overlay);
    }

    private void openAddExpenseDialog() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.85);");
        overlay.setPrefSize(getWidth(), getHeight());

        VBox card = new VBox(16);
        card.setMaxWidth(440);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Add Upcoming Business Expense");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Expense.Category> catCombo = new ComboBox<>();
        catCombo.getItems().addAll(Expense.Category.values());
        catCombo.setValue(Expense.Category.RENT);
        catCombo.setMaxWidth(Double.MAX_VALUE);

        TextField descField = new TextField("Office rent / Studio lease");
        TextField amtField = new TextField("12000");
        DatePicker duePicker = new DatePicker(LocalDate.now().plusDays(10));

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> ((Pane) getParent()).getChildren().remove(overlay));

        Button btnSave = new Button("Save Expense");
        btnSave.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                Expense exp = new Expense(catCombo.getValue(), descField.getText().trim(), amt, duePicker.getValue());
                store.addExpense(exp);
                ((Pane) getParent()).getChildren().remove(overlay);
                Toast.show((Pane) getParent(), "Expense added: " + CurrencyFormatter.format(amt), true);
            } catch (Exception ignored) {}
        });

        btnRow.getChildren().addAll(btnCancel, btnSave);

        card.getChildren().addAll(
            title,
            createLabeledField("Category", catCombo),
            createLabeledField("Description", descField),
            createLabeledField("Amount (₹)", amtField),
            createLabeledField("Due Date", duePicker),
            btnRow
        );

        overlay.getChildren().add(card);
        ((Pane) getParent()).getChildren().add(overlay);
    }

    private VBox createLabeledField(String l, Control c) {
        VBox b = new VBox(4);
        Label lbl = new Label(l);
        lbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-font-weight: bold;");
        c.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 8;");
        b.getChildren().addAll(lbl, c);
        return b;
    }
}
