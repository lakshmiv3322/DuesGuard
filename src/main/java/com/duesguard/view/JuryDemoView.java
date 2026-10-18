package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.Invoice;
import com.duesguard.service.DemoDataService;
import com.duesguard.storage.DataStore;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Screen 19: Jury Demo Mode & 90-Second Guided Hackathon Tour.
 * Highlights the core differentiator of DuesGuard, provides interactive 14-step tour,
 * and allows 1-click resetting of demo data.
 */
public class JuryDemoView extends ScrollPane {
    private final DataStore store;
    private int currentTourStep = 1;
    private final Label tourStepTitle = new Label();
    private final Label tourStepDesc = new Label();
    private final Button btnTourAction = new Button();

    private final String[][] TOUR_STEPS = {
        {"1. Payment Recovery Command Centre", "Open the Dashboard to see ₹48,500 overdue across 4 invoices with immediate action prompts.", "Go to Dashboard", "DASHBOARD"},
        {"2. Follow-Up Priority Queue", "Navigate to the Priority Queue to see invoices ranked by explainable multi-factor scoring.", "Open Priority Queue", "PRIORITY_QUEUE"},
        {"3. Why GreenLeaf Events Ranks #1", "Inspect the score formula (86/100): ₹42,000 balance (35/35) + 24 days overdue (20/25) + missing work proof (10/10).", "Open Score Details", "PRIORITY_QUEUE"},
        {"4. One-Click Focus Mode", "Launch Focus Mode to eliminate noise and display the single highest-impact follow-up.", "Launch Focus Mode", "PRIORITY_QUEUE"},
        {"5. Context-Aware Reminder Composer", "View the generated respectful WhatsApp reminder with dynamic variables and tone selection.", "Prepare Follow-Up", "REMINDER_COMPOSER"},
        {"6. Dispute-Readiness Checklist", "Check the 5 required proofs for invoice INV-103 to protect receivables before disputes arise.", "View Readiness", "INVOICE_DETAILS"},
        {"7. Drag-and-Drop Evidence Upload", "Add deliverable completion proof to simulate readiness score increasing to 80%.", "Open Documents", "DOCUMENTS"},
        {"8. Cash-Flow Radar", "Inspect the weekly collection gap and 3-scenario projections (Best, Likely, Conservative).", "Open Cash Flow", "CASH_FLOW"},
        {"9. 'What If?' Simulator", "Simulate the immediate cash-flow impact if GreenLeaf Events clears this week.", "Run Simulator", "CASH_FLOW"},
        {"10. Record Settlement & Recovery", "Record a partial payment of ₹8,000 and trigger the recovery celebration animation.", "Record Payment", "PAYMENTS"}
    };

    public JuryDemoView() {
        this.store = AppContext.getInstance().getDataStore();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1100);

        // Header
        content.getChildren().add(buildHeader());

        // 90-Second Guided Tour Card
        content.getChildren().add(buildTourCard());

        // Why DuesGuard? Value Proposition Panel
        content.getChildren().add(buildWhyDuesGuardPanel());

        // Hackathon Jury Cheat Sheet & Metrics
        content.getChildren().add(buildJuryMetricsCard());

        setContent(content);
        updateTourStep(1);
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Hackathon Jury Demo Mode");
        title.setStyle("-fx-text-fill: #FFB84D; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Guided 90-second evaluation tour showcasing the Priority Scoring Engine");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnReset = new Button("↻ Reset Demo Data");
        btnReset.setStyle("-fx-background-color: rgba(255,107,107,0.15); -fx-text-fill: #FF6B6B; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnReset.setOnAction(e -> {
            DemoDataService.seedDemoData(store);
            Toast.show(this, "Demo state reset to Lakshmi Creative Studio baseline!", true);
        });

        header.getChildren().addAll(titleCol, spacer, btnReset);
        return header;
    }

    private VBox buildTourCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setStyle(
            "-fx-background-color: linear-gradient(135deg, #121A2B, #182338); " +
            "-fx-background-radius: 20; -fx-border-color: rgba(255, 184, 77, 0.4); " +
            "-fx-border-radius: 20; -fx-border-width: 1.5; " +
            "-fx-effect: dropshadow(gaussian, rgba(255, 184, 77, 0.15), 24, 0, 0, 4);"
        );

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("90-SECOND GUIDED TOUR");
        badge.setStyle("-fx-background-color: rgba(255, 184, 77, 0.2); -fx-text-fill: #FFB84D; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 100; -fx-padding: 3 10;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label stepCounter = new Label("STEP " + currentTourStep + " OF " + TOUR_STEPS.length);
        stepCounter.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 12px; -fx-font-weight: bold;");

        top.getChildren().addAll(badge, sp, stepCounter);

        tourStepTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        tourStepDesc.setWrapText(true);
        tourStepDesc.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 14px;");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button btnPrev = new Button("← Previous");
        btnPrev.setStyle("-fx-background-color: #182338; -fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnPrev.setOnAction(e -> {
            if (currentTourStep > 1) {
                currentTourStep--;
                updateTourStep(currentTourStep);
                stepCounter.setText("STEP " + currentTourStep + " OF " + TOUR_STEPS.length);
            }
        });

        Button btnNext = new Button("Next Step →");
        btnNext.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnNext.setOnAction(e -> {
            if (currentTourStep < TOUR_STEPS.length) {
                currentTourStep++;
                updateTourStep(currentTourStep);
                stepCounter.setText("STEP " + currentTourStep + " OF " + TOUR_STEPS.length);
            }
        });

        btnTourAction.setStyle("-fx-background-color: #FFB84D; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");

        btnRow.getChildren().addAll(btnPrev, btnNext, btnTourAction);

        card.getChildren().addAll(top, tourStepTitle, tourStepDesc, btnRow);
        return card;
    }

    private void updateTourStep(int step) {
        String[] s = TOUR_STEPS[step - 1];
        tourStepTitle.setText(s[0]);
        tourStepDesc.setText(s[1]);
        btnTourAction.setText("▶ " + s[2]);
        btnTourAction.setOnAction(e -> {
            Screen target = Screen.valueOf(s[3]);
            if (target == Screen.REMINDER_COMPOSER || target == Screen.INVOICE_DETAILS) {
                store.findByInvoiceNumber("INV-103").ifPresent(NavigationManager.getInstance()::setSelectedInvoice);
            }
            NavigationManager.getInstance().navigateTo(target);
        });
    }

    private VBox buildWhyDuesGuardPanel() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Why DuesGuard? (Jury Evaluation Criteria)");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(14);

        grid.add(createCritCard("Not Another Accounting Suite", "Tally and QuickBooks record what happened. DuesGuard tells business owners what to do next to recover cash.", "#49E3B4"), 0, 0);
        grid.add(createCritCard("Explainable Priority Scoring", "0–100 score transparently weighted: Amount (35%), Overdue (25%), Client Delay (20%), Proof Gap (10%), Reminder (10%).", "#7C83FF"), 1, 0);
        grid.add(createCritCard("Pre-Dispute Evidence Architecture", "Empowers small businesses to collect signed approvals and completion proofs before a client disputes an invoice.", "#FFB84D"), 0, 1);
        grid.add(createCritCard("Built with Pure Java & JavaFX", "Zero heavy JavaScript or Electron bloat. Responsive desktop performance with native JavaFX 3D scene graphs.", "#4CAF50"), 1, 1);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createCritCard(String title, String desc, String colorHex) {
        VBox b = new VBox(4);
        b.setPrefWidth(460);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label d = new Label(desc);
        d.setWrapText(true);
        d.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        b.getChildren().addAll(t, d);
        return b;
    }

    private VBox buildJuryMetricsCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Demo Business State Snapshot");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox stats = new HBox(20);
        stats.getChildren().addAll(
            createStatPill("Studio", "Lakshmi Creative Studio"),
            createStatPill("Total Inflow Target", "₹1,84,500"),
            createStatPill("Current Overdue", "₹48,500 across 4 invoices"),
            createStatPill("Highest Urgency", "GreenLeaf Events (INV-103, ₹42,000)")
        );

        card.getChildren().addAll(title, stats);
        return card;
    }

    private VBox createStatPill(String l, String v) {
        VBox b = new VBox(2);
        b.setPadding(new Insets(10, 14, 10, 14));
        b.setStyle("-fx-background-color: #182338; -fx-background-radius: 10;");
        Label lbl = new Label(l);
        lbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 10px;");
        Label val = new Label(v);
        val.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-font-weight: bold;");
        b.getChildren().addAll(lbl, val);
        return b;
    }
}
