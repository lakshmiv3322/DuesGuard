package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

/**
 * Screen 14: Financial Reports & Ageing Buckets.
 * Contains Ageing Buckets Bar Chart (Not due, 1-7d, 8-30d, 31-60d, 60+d),
 * Client Reliability delay comparisons, Recovery trend stats, and CSV export.
 */
public class ReportsView extends ScrollPane {
    private final DataStore store;

    public ReportsView() {
        this.store = AppContext.getInstance().getDataStore();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // Header + Export
        content.getChildren().add(buildHeader());

        // Report Cards Grid (6 reports)
        content.getChildren().add(buildReportCardsGrid());

        // Charts Section (Ageing Buckets + Client Delays)
        content.getChildren().add(buildChartsRow());

        setContent(content);
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Reports & Ageing Analysis");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Financial summaries, ageing analysis, and recovery efficiency metrics");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExport = new Button("Export All Reports (CSV)");
        btnExport.setStyle(
            "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;"
        );
        btnExport.setOnAction(e -> exportReportsCSV());

        header.getChildren().addAll(titleCol, spacer, btnExport);
        return header;
    }

    private GridPane buildReportCardsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            grid.getColumnConstraints().add(col);
        }

        grid.add(createReportSummaryCard("Outstanding Receivables", "₹88,500 total across 6 invoices", "Active collection pipeline", "#49E3B4"), 0, 0);
        grid.add(createReportSummaryCard("Overdue Ageing (8–30 Days)", "₹42,000 in highest risk bracket", "GreenLeaf Events & Arun Studio", "#FF6B6B"), 1, 0);
        grid.add(createReportSummaryCard("Client Payment Behaviour", "Average delay across portfolio: 6.2 days", "Neutral historical calculation", "#7C83FF"), 2, 0);

        grid.add(createReportSummaryCard("Monthly Cash-Flow Health", "₹96,000 collected of ₹1,84,500 expected", "52% collection pacing", "#4CAF50"), 0, 1);
        grid.add(createReportSummaryCard("Follow-Up Performance", "78% response rate within 48h of WhatsApp", "WhatsApp converts 2.4x faster than email", "#FFB84D"), 1, 1);
        grid.add(createReportSummaryCard("Payment-Readiness Ratio", "65% of invoices dispute-ready", "5 invoices need completion proofs", "#F6F8FC"), 2, 1);

        return grid;
    }

    private VBox createReportSummaryCard(String title, String main, String sub, String colorHex) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label m = new Label(main);
        m.setWrapText(true);
        m.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");

        card.getChildren().addAll(t, m, s);
        return card;
    }

    private HBox buildChartsRow() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        // Chart 1: Ageing Buckets (Bar chart)
        VBox ageingCard = new VBox(12);
        ageingCard.setPadding(new Insets(20));
        ageingCard.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 18; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 18; -fx-border-width: 1;");
        HBox.setHgrow(ageingCard, Priority.ALWAYS);

        Label aTitle = new Label("Overdue Receivables Ageing Buckets");
        aTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 15px; -fx-font-weight: bold;");

        CategoryAxis axAxis = new CategoryAxis();
        NumberAxis ayAxis = new NumberAxis();
        axAxis.setTickLabelFill(Color.web("#9BA8BC"));
        ayAxis.setTickLabelFill(Color.web("#9BA8BC"));

        BarChart<String, Number> ageChart = new BarChart<>(axAxis, ayAxis);
        ageChart.setPrefHeight(230);
        ageChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> ageSeries = new XYChart.Series<>();
        ageSeries.setName("Amount (₹)");
        ageSeries.getData().add(new XYChart.Data<>("Not due", 48000));
        ageSeries.getData().add(new XYChart.Data<>("1–7 days", 4000));
        ageSeries.getData().add(new XYChart.Data<>("8–30 days", 42000));
        ageSeries.getData().add(new XYChart.Data<>("31–60 days", 2500));
        ageSeries.getData().add(new XYChart.Data<>("60+ days", 0));
        ageChart.getData().add(ageSeries);

        ageingCard.getChildren().addAll(aTitle, ageChart);

        // Chart 2: Client Reliability Delay Comparison
        VBox delayCard = new VBox(12);
        delayCard.setPadding(new Insets(20));
        delayCard.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 18; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 18; -fx-border-width: 1;");
        HBox.setHgrow(delayCard, Priority.ALWAYS);

        Label dTitle = new Label("Average Payment Delay by Client (Days)");
        dTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 15px; -fx-font-weight: bold;");

        CategoryAxis dxAxis = new CategoryAxis();
        NumberAxis dyAxis = new NumberAxis();
        dxAxis.setTickLabelFill(Color.web("#9BA8BC"));
        dyAxis.setTickLabelFill(Color.web("#9BA8BC"));

        BarChart<String, Number> delayChart = new BarChart<>(dxAxis, dyAxis);
        delayChart.setPrefHeight(230);
        delayChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> delaySeries = new XYChart.Series<>();
        delaySeries.setName("Average Delay (Days)");
        for (Client c : store.getClients()) {
            delaySeries.getData().add(new XYChart.Data<>(c.getName().split(" ")[0], c.getTypicalDelayDays()));
        }
        delayChart.getData().add(delaySeries);

        delayCard.getChildren().addAll(dTitle, delayChart);

        row.getChildren().addAll(ageingCard, delayCard);
        return row;
    }

    private void exportReportsCSV() {
        try {
            File f = new File(System.getProperty("user.home") + File.separator + "duesguard-ageing-report.csv");
            try (PrintWriter pw = new PrintWriter(f)) {
                pw.println("Bucket,Amount,InvoicesCount");
                pw.println("Not due,48000,2");
                pw.println("1-7 days overdue,4000,1");
                pw.println("8-30 days overdue,66500,2");
                pw.println("31-60 days overdue,0,0");
                pw.println("60+ days overdue,0,0");
            }
            Toast.show(this, "Exported ageing report to " + f.getName(), true);
        } catch (Exception e) {
            Toast.show(this, "Export failed: " + e.getMessage(), false);
        }
    }
}
