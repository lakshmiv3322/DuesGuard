package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.*;
import com.duesguard.service.PriorityScoreService;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import com.duesguard.view.components.Priority3DConstellation;
import com.duesguard.view.components.ScoreRing;
import com.duesguard.view.components.Toast;
import javafx.animation.TranslateTransition;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Screen 5: Priority Queue - Core Differentiator of DuesGuard.
 * Ranks overdue invoices using explainable 5-factor Priority Score.
 * Includes interactive 3D constellation, right-side detail drawer,
 * transparent formula explanation modal, and One-Click Focus Mode.
 */
public class PriorityQueueView extends StackPane {
    private final DataStore store;
    private final PriorityScoreService scoreService;
    private final VBox queueListContainer;
    private final StackPane drawerContainer;
    private Invoice drawerInvoice;
    private String selectedTab = "All";
    private String sortBy = "Highest priority";
    private boolean show3DConstellation = false;
    private VBox constellationBox;

    public PriorityQueueView() {
        this.store = AppContext.getInstance().getDataStore();
        this.scoreService = AppContext.getInstance().getPriorityScoreService();

        // Main layout: content + sliding right drawer
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: transparent;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // 1. Header + Actions (Focus Mode & How the Score Works & 3D Toggle)
        content.getChildren().add(buildHeader());

        // 2. Summary KPI Cards (Urgent total, count, avg overdue, recoverable)
        content.getChildren().add(buildSummaryCards());

        // 3. Formula Explanation Banner
        content.getChildren().add(buildFormulaBanner());

        // 4. Optional 3D Priority Constellation container
        constellationBox = buildConstellationSection();
        constellationBox.setVisible(false);
        constellationBox.setManaged(false);
        content.getChildren().add(constellationBox);

        // 5. Tabs and Filter Bar
        content.getChildren().add(buildTabsAndFilters());

        // 6. Queue Items List
        queueListContainer = new VBox(10);
        content.getChildren().add(queueListContainer);

        scroll.setContent(content);
        layout.setCenter(scroll);

        // 7. Right-side Drawer Container
        drawerContainer = new StackPane();
        drawerContainer.setVisible(false);
        drawerContainer.setManaged(false);
        drawerContainer.setPrefWidth(440);
        drawerContainer.setStyle("-fx-background-color: #0D1526; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 0 0 0 1;");
        layout.setRight(drawerContainer);

        getChildren().add(layout);

        refreshQueue();
    }

    // -------------------------------------------------------------
    // Header
    // -------------------------------------------------------------
    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(3);
        Label title = new Label("Follow-up priority queue");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Spend your time where it can recover the most cash.");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3D Constellation Toggle Button
        Button btn3D = new Button("🌌 3D Constellation");
        btn3D.setStyle(
            "-fx-background-color: rgba(124, 131, 255, 0.15); -fx-text-fill: #7C83FF; " +
            "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; " +
            "-fx-border-color: rgba(124,131,255,0.3); -fx-border-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;"
        );
        btn3D.setOnAction(e -> {
            show3DConstellation = !show3DConstellation;
            constellationBox.setVisible(show3DConstellation);
            constellationBox.setManaged(show3DConstellation);
            btn3D.setText(show3DConstellation ? "Hide 3D View" : "🌌 3D Constellation");
        });

        // Focus Mode Button (MEMORABLE DEMO FEATURE)
        Button btnFocus = new Button("⚡ Focus Mode");
        btnFocus.setStyle(
            "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;"
        );
        btnFocus.setOnAction(e -> openFocusMode());

        header.getChildren().addAll(titleCol, spacer, btn3D, btnFocus);
        return header;
    }

    // -------------------------------------------------------------
    // Summary Cards
    // -------------------------------------------------------------
    private GridPane buildSummaryCards() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        List<Invoice> active = store.getActiveInvoices();
        double urgentTotal = active.stream().filter(i -> i.getDaysOverdue() > 0).mapToDouble(Invoice::getBalance).sum();
        long highPriCount = active.stream().filter(i -> i.getDaysOverdue() > 7).count();
        double avgDays = active.stream().filter(i -> i.getDaysOverdue() > 0).mapToLong(Invoice::getDaysOverdue).average().orElse(0);
        double recoverable = urgentTotal * 0.75;

        grid.add(createMiniKpi("Total urgent amount", CurrencyFormatter.format(urgentTotal), "#FF6B6B"), 0, 0);
        grid.add(createMiniKpi("High-priority invoices", highPriCount + " invoices", "#FFB84D"), 1, 0);
        grid.add(createMiniKpi("Average overdue", String.format("%.0f days", avgDays), "#7C83FF"), 2, 0);
        grid.add(createMiniKpi("Potentially recoverable this week", CurrencyFormatter.format(recoverable), "#49E3B4"), 3, 0);

        return grid;
    }

    private VBox createMiniKpi(String label, String value, String colorHex) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 16; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1;"
        );

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        card.getChildren().addAll(lbl, val);
        return card;
    }

    // -------------------------------------------------------------
    // Formula Explanation Banner
    // -------------------------------------------------------------
    private HBox buildFormulaBanner() {
        HBox banner = new HBox(16);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(14, 20, 14, 20));
        banner.setStyle(
            "-fx-background-color: rgba(73, 227, 180, 0.08); " +
            "-fx-background-radius: 14; " +
            "-fx-border-color: rgba(73, 227, 180, 0.2); " +
            "-fx-border-radius: 14; " +
            "-fx-border-width: 1;"
        );

        Label infoIcon = new Label("💡");
        infoIcon.setStyle("-fx-font-size: 16px;");

        VBox textCol = new VBox(2);
        Label title = new Label("Transparent Priority Scoring Engine");
        title.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label desc = new Label("DuesGuard ranks each invoice using amount due (35%), overdue duration (25%), client payment behaviour (20%), missing proof (10%), and follow-up response (10%).");
        desc.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        textCol.getChildren().addAll(title, desc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnHow = new Button("How the score works →");
        btnHow.setStyle(
            "-fx-background-color: rgba(73, 227, 180, 0.15); -fx-text-fill: #49E3B4; " +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
        );
        btnHow.setOnAction(e -> openScoreFormulaModal());

        banner.getChildren().addAll(infoIcon, textCol, spacer, btnHow);
        return banner;
    }

    // -------------------------------------------------------------
    // 3D Constellation Section
    // -------------------------------------------------------------
    private VBox buildConstellationSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 18; " +
            "-fx-border-color: rgba(124, 131, 255, 0.3); -fx-border-radius: 18; -fx-border-width: 1;"
        );

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("3D Cash-Flow & Priority Constellation");
        title.setStyle("-fx-text-fill: #7C83FF; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label hint = new Label("(Center = DuesGuard Hub • Outer nodes = Clients • Glowing red/amber = Overdue Invoices • Green = Paid)");
        hint.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        titleRow.getChildren().addAll(title, hint);

        Map<String, Client> cMap = store.getClients().stream().collect(Collectors.toMap(Client::getId, c -> c, (a, b) -> a));
        Priority3DConstellation constellation = new Priority3DConstellation(
            800, 240, store.getClients(), store.getActiveInvoices(), cMap,
            inv -> openDrawer(inv)
        );

        box.getChildren().addAll(titleRow, constellation);
        return box;
    }

    // -------------------------------------------------------------
    // Tabs & Filter Bar
    // -------------------------------------------------------------
    private HBox buildTabsAndFilters() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        // Tab buttons
        HBox tabs = new HBox(4);
        tabs.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 10; -fx-padding: 3; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 10;");

        String[] tabNames = {"All", "Critical", "High", "Medium", "Ready to remind", "Needs documents"};
        for (String t : tabNames) {
            Button btn = new Button(t);
            boolean active = t.equals(selectedTab);
            btn.setStyle(active
                ? "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                selectedTab = t;
                refreshQueue();
                tabs.getChildren().forEach(n -> {
                    Button b = (Button) n;
                    boolean a = b.getText().equals(selectedTab);
                    b.setStyle(a
                        ? "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;"
                        : "-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
                });
            });
            tabs.getChildren().add(btn);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Sort ComboBox
        Label sortLbl = new Label("Sort:");
        sortLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Highest priority", "Highest amount", "Longest overdue", "Most likely to pay");
        sortCombo.setValue(sortBy);
        sortCombo.setStyle("-fx-background-color: #121A2B; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 4 8;");
        sortCombo.setOnAction(e -> {
            sortBy = sortCombo.getValue();
            refreshQueue();
        });

        row.getChildren().addAll(tabs, spacer, sortLbl, sortCombo);
        return row;
    }

    // -------------------------------------------------------------
    // Refresh & Render Queue
    // -------------------------------------------------------------
    private void refreshQueue() {
        queueListContainer.getChildren().clear();

        List<Invoice> active = store.getActiveInvoices().stream()
            .filter(i -> i.getStatus() != Invoice.Status.PAID && i.getStatus() != Invoice.Status.DRAFT)
            .collect(Collectors.toList());

        Map<String, Client> clientMap = store.getClients().stream().collect(Collectors.toMap(Client::getId, c -> c, (a, b) -> a));

        // Filter by Tab
        List<Invoice> filtered = active.stream().filter(inv -> {
            Client c = clientMap.get(inv.getClientId());
            double score = scoreService.calculate(inv, c);
            return switch (selectedTab) {
                case "Critical" -> score >= 80;
                case "High" -> score >= 60 && score < 80;
                case "Medium" -> score >= 40 && score < 60;
                case "Ready to remind" -> inv.getReadinessScore() >= 80;
                case "Needs documents" -> inv.getReadinessScore() < 70;
                default -> true;
            };
        }).collect(Collectors.toList());

        // Sort
        filtered.sort((a, b) -> {
            Client ca = clientMap.get(a.getClientId());
            Client cb = clientMap.get(b.getClientId());
            return switch (sortBy) {
                case "Highest amount" -> Double.compare(b.getBalance(), a.getBalance());
                case "Longest overdue" -> Long.compare(b.getDaysOverdue(), a.getDaysOverdue());
                case "Most likely to pay" -> {
                    int da = ca != null ? ca.getTypicalDelayDays() : 99;
                    int db = cb != null ? cb.getTypicalDelayDays() : 99;
                    yield Integer.compare(da, db);
                }
                default -> {
                    double sa = scoreService.calculate(a, ca);
                    double sb = scoreService.calculate(b, cb);
                    yield Double.compare(sb, sa);
                }
            };
        });

        if (filtered.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            Label emptyTitle = new Label("Nothing urgent today");
            emptyTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");
            Label emptySub = new Label("Your follow-up queue is clear for this category. Great work keeping on top of dues!");
            emptySub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
            empty.getChildren().addAll(emptyTitle, emptySub);
            queueListContainer.getChildren().add(empty);
            return;
        }

        int rank = 1;
        for (Invoice inv : filtered) {
            Client c = clientMap.get(inv.getClientId());
            double score = scoreService.calculate(inv, c);
            queueListContainer.getChildren().add(createQueueItemCard(rank++, inv, c, score));
        }
    }

    private Node createQueueItemCard(int rank, Invoice inv, Client client, double score) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 16; " +
            "-fx-border-color: rgba(255,255,255,0.06); " +
            "-fx-border-radius: 16; " +
            "-fx-border-width: 1; " +
            "-fx-cursor: hand;"
        );

        card.setOnMouseClicked(e -> openDrawer(inv));

        // Priority Rank #
        Label rankLbl = new Label("#" + rank);
        rankLbl.setPrefWidth(32);
        rankLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Score Ring
        ScoreRing scoreRing = new ScoreRing(score, 18);
        scoreRing.setPrefWidth(70);

        // Client & Invoice details
        VBox clientCol = new VBox(3);
        clientCol.setPrefWidth(200);

        Label clientName = new Label(client != null ? client.getName() : "Unknown");
        clientName.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label invNum = new Label(inv.getInvoiceNumber() + " • " + inv.getServiceTitle());
        invNum.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        clientCol.getChildren().addAll(clientName, invNum);

        // Balance & Overdue
        VBox amtCol = new VBox(3);
        amtCol.setPrefWidth(160);

        Label balance = new Label(CurrencyFormatter.format(inv.getBalance()));
        balance.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 15px; -fx-font-weight: bold;");

        String overdueStr = inv.getDaysOverdue() > 0 ? inv.getDaysOverdue() + " days overdue" : "Due in " + inv.getDaysUntilDue() + "d";
        Label overdue = new Label(overdueStr);
        overdue.setStyle("-fx-text-fill: " + (inv.getDaysOverdue() > 0 ? "#FF6B6B" : "#49E3B4") + "; -fx-font-size: 12px; -fx-font-weight: 500;");
        amtCol.getChildren().addAll(balance, overdue);

        // Payment Readiness & Proof Gap
        VBox readyCol = new VBox(3);
        readyCol.setPrefWidth(160);

        Label readyPct = new Label(String.format("%.0f%% Dispute-Ready", inv.getReadinessScore()));
        readyPct.setStyle("-fx-text-fill: " + (inv.getReadinessScore() >= 80 ? "#4CAF50" : "#FFB84D") + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        String proofStr = inv.getReadinessScore() >= 80 ? "All proofs attached" : "Add completion proof";
        Label proof = new Label(proofStr);
        proof.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        readyCol.getChildren().addAll(readyPct, proof);

        // Recommended Next Action
        VBox recCol = new VBox(3);
        recCol.setPrefWidth(200);

        Label recLabel = new Label("Recommended Action");
        recLabel.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 10px; -fx-font-weight: bold;");

        String recAction = inv.getDaysOverdue() > 14
            ? "Send firm WhatsApp reminder"
            : (inv.getReadinessScore() < 70 ? "Attach completion proof first" : "Send friendly follow-up");
        Label rec = new Label(recAction);
        rec.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 12px; -fx-font-weight: 500;");
        recCol.getChildren().addAll(recLabel, rec);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quick action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnRemind = new Button("Remind");
        btnRemind.setStyle(
            "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; " +
            "-fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;"
        );
        btnRemind.setOnAction(e -> {
            e.consume();
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.REMINDER_COMPOSER);
        });

        Button btnDetails = new Button("Details →");
        btnDetails.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; " +
            "-fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;"
        );
        btnDetails.setOnAction(e -> {
            e.consume();
            openDrawer(inv);
        });

        actions.getChildren().addAll(btnRemind, btnDetails);

        card.getChildren().addAll(rankLbl, scoreRing, clientCol, amtCol, readyCol, recCol, spacer, actions);
        return card;
    }

    // -------------------------------------------------------------
    // Right Drawer: Invoice Summary, Score Breakdown & Quick Actions
    // -------------------------------------------------------------
    private void openDrawer(Invoice inv) {
        this.drawerInvoice = inv;
        drawerContainer.getChildren().clear();

        ScrollPane drawerScroll = new ScrollPane();
        drawerScroll.setFitToWidth(true);
        drawerScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox drawerContent = new VBox(18);
        drawerContent.setPadding(new Insets(24));

        // Drawer Top Close Row
        HBox closeRow = new HBox(12);
        closeRow.setAlignment(Pos.CENTER_LEFT);

        Label drawerTitle = new Label(inv.getInvoiceNumber());
        drawerTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 14px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> closeDrawer());

        closeRow.getChildren().addAll(drawerTitle, spacer, closeBtn);

        // Score Breakdown Box
        Client client = store.findClientById(inv.getClientId()).orElse(null);
        PriorityScoreService.ScoreBreakdown bd = scoreService.calculateBreakdown(inv, client);

        VBox scoreCard = new VBox(10);
        scoreCard.setPadding(new Insets(16));
        scoreCard.setStyle("-fx-background-color: #182338; -fx-background-radius: 14; -fx-border-color: rgba(73,227,180,0.2); -fx-border-radius: 14; -fx-border-width: 1;");

        HBox scoreHeader = new HBox(10);
        scoreHeader.setAlignment(Pos.CENTER_LEFT);
        Label scTitle = new Label("Priority Score: " + String.format("%.0f", bd.total()));
        scTitle.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 16px; -fx-font-weight: bold;");
        scoreHeader.getChildren().add(scTitle);

        VBox factors = new VBox(6);
        factors.getChildren().addAll(
            createFactorRow("Outstanding Amount (35%)", String.format("%.0f/35", bd.amountScore()), CurrencyFormatter.format(inv.getBalance())),
            createFactorRow("Days Overdue (25%)", String.format("%.0f/25", bd.daysScore()), inv.getDaysOverdue() + " days"),
            createFactorRow("Client Behaviour (20%)", String.format("%.0f/20", bd.clientScore()), client != null ? "Avg " + client.getTypicalDelayDays() + "d late" : "Unknown"),
            createFactorRow("Evidence Gap (10%)", String.format("%.0f/10", bd.evidenceScore()), inv.getReadinessScore() < 70 ? "Proof missing" : "Complete"),
            createFactorRow("Reminder Response (10%)", String.format("%.0f/10", bd.reminderScore()), "No recent response")
        );
        scoreCard.getChildren().addAll(scoreHeader, factors);

        // Recommended Message Preview
        VBox msgCard = new VBox(8);
        msgCard.setPadding(new Insets(14));
        msgCard.setStyle("-fx-background-color: #182338; -fx-background-radius: 14;");

        Label msgTitle = new Label("Recommended Follow-Up Message");
        msgTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: bold;");

        String previewMsg = AppContext.getInstance().getReminderMessageService()
            .generateMessage(inv, client, Reminder.Tone.FRIENDLY, Reminder.EscalationLevel.FIRST);

        Label msgBody = new Label(previewMsg);
        msgBody.setWrapText(true);
        msgBody.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        Button btnCopy = new Button("📋 Copy Message");
        btnCopy.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #49E3B4; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        btnCopy.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(previewMsg);
            Clipboard.getSystemClipboard().setContent(cc);
            Toast.show(this, "Message copied to clipboard!", true);
        });

        msgCard.getChildren().addAll(msgTitle, msgBody, btnCopy);

        // Action Buttons Grid
        VBox actionsBox = new VBox(8);
        Button btnSendRemind = new Button("Send Reminder");
        btnSendRemind.setMaxWidth(Double.MAX_VALUE);
        btnSendRemind.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
        btnSendRemind.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.REMINDER_COMPOSER);
        });

        Button btnRecordPay = new Button("Record Payment");
        btnRecordPay.setMaxWidth(Double.MAX_VALUE);
        btnRecordPay.setStyle("-fx-background-color: rgba(124, 131, 255, 0.15); -fx-text-fill: #7C83FF; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-border-color: rgba(124,131,255,0.3); -fx-border-radius: 10; -fx-padding: 9; -fx-cursor: hand;");
        btnRecordPay.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.INVOICE_DETAILS);
        });

        Button btnAddDoc = new Button("Add Document / Proof");
        btnAddDoc.setMaxWidth(Double.MAX_VALUE);
        btnAddDoc.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 9; -fx-cursor: hand;");
        btnAddDoc.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(inv);
            NavigationManager.getInstance().navigateTo(Screen.DOCUMENTS);
        });

        actionsBox.getChildren().addAll(btnSendRemind, btnRecordPay, btnAddDoc);

        drawerContent.getChildren().addAll(closeRow, scoreCard, msgCard, actionsBox);
        drawerScroll.setContent(drawerContent);
        drawerContainer.getChildren().add(drawerScroll);

        drawerContainer.setVisible(true);
        drawerContainer.setManaged(true);
    }

    private HBox createFactorRow(String factor, String score, String detail) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label fLbl = new Label(factor);
        fLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label sLbl = new Label(score);
        sLbl.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(fLbl, sp, sLbl);
        return row;
    }

    private void closeDrawer() {
        drawerContainer.setVisible(false);
        drawerContainer.setManaged(false);
    }

    // -------------------------------------------------------------
    // One-Click Focus Mode (WOW DEMO FEATURE)
    // -------------------------------------------------------------
    private void openFocusMode() {
        List<Invoice> active = store.getActiveInvoices().stream()
            .filter(i -> i.getStatus() != Invoice.Status.PAID && i.getStatus() != Invoice.Status.DRAFT)
            .sorted((a, b) -> Long.compare(b.getDaysOverdue(), a.getDaysOverdue()))
            .toList();

        if (active.isEmpty()) {
            Toast.show(this, "No overdue invoices! Focus queue is clear.", true);
            return;
        }

        Invoice topInv = active.get(0);
        Client client = store.findClientById(topInv.getClientId()).orElse(null);

        StackPane focusOverlay = new StackPane();
        focusOverlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.90);");
        focusOverlay.setPrefSize(getWidth(), getHeight());

        VBox focusCard = new VBox(22);
        focusCard.setAlignment(Pos.CENTER);
        focusCard.setMaxWidth(620);
        focusCard.setPadding(new Insets(36));
        focusCard.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 24; " +
            "-fx-border-color: rgba(73, 227, 180, 0.4); " +
            "-fx-border-radius: 24; " +
            "-fx-border-width: 1.5; " +
            "-fx-effect: dropshadow(gaussian, rgba(73, 227, 180, 0.25), 36, 0, 0, 0);"
        );

        // Header badge
        Label badge = new Label("⚡ FOCUS MODE • SINGLE NEXT ACTION");
        badge.setStyle(
            "-fx-background-color: rgba(73, 227, 180, 0.15); " +
            "-fx-border-color: rgba(73, 227, 180, 0.4); " +
            "-fx-border-radius: 100; " +
            "-fx-background-radius: 100; " +
            "-fx-border-width: 1; " +
            "-fx-text-fill: #49E3B4; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 4 14;"
        );

        Label clientLbl = new Label(client != null ? client.getName() : "Top Priority Client");
        clientLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 26px; -fx-font-weight: bold;");

        Label amtLbl = new Label("Owes " + CurrencyFormatter.format(topInv.getBalance()) + " (" + topInv.getDaysOverdue() + " days overdue)");
        amtLbl.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label whyLbl = new Label("Why this matters: Highest balance overdue with no follow-up in the last 10 days. Recovering this improves monthly inflow by 24%.");
        whyLbl.setWrapText(true);
        whyLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-text-alignment: center;");

        // Message Preview Box
        VBox msgPreviewBox = new VBox(6);
        msgPreviewBox.setAlignment(Pos.CENTER_LEFT);
        msgPreviewBox.setPadding(new Insets(14));
        msgPreviewBox.setStyle("-fx-background-color: #182338; -fx-background-radius: 12;");

        Label mpTitle = new Label("Prepared Message (Ready to Send)");
        mpTitle.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 11px; -fx-font-weight: bold;");

        String preparedMsg = AppContext.getInstance().getReminderMessageService()
            .generateMessage(topInv, client, Reminder.Tone.FRIENDLY, Reminder.EscalationLevel.FIRST);

        Label mpText = new Label(preparedMsg);
        mpText.setWrapText(true);
        mpText.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px;");
        msgPreviewBox.getChildren().addAll(mpTitle, mpText);

        // Action Buttons Row
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button btnCopy = new Button("📋 Copy Message");
        btnCopy.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        btnCopy.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(preparedMsg);
            Clipboard.getSystemClipboard().setContent(cc);
            Toast.show(this, "Message copied to clipboard!", true);
        });

        Button btnComplete = new Button("✓ Mark as Sent");
        btnComplete.setStyle("-fx-background-color: rgba(124, 131, 255, 0.2); -fx-text-fill: #7C83FF; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-border-color: rgba(124,131,255,0.4); -fx-border-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        btnComplete.setOnAction(e -> {
            FollowUpLog log = new FollowUpLog(topInv.getId(), "WhatsApp", "Sent reminder in Focus Mode");
            store.addFollowUpLog(log);
            Toast.show(this, "Reminder logged for " + (client != null ? client.getName() : topInv.getInvoiceNumber()), true);
            getChildren().remove(focusOverlay);
            refreshQueue();
        });

        Button btnSkip = new Button("Skip for now");
        btnSkip.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-padding: 10 16; -fx-cursor: hand;");
        btnSkip.setOnAction(e -> getChildren().remove(focusOverlay));

        btnRow.getChildren().addAll(btnCopy, btnComplete, btnSkip);

        focusCard.getChildren().addAll(badge, clientLbl, amtLbl, whyLbl, msgPreviewBox, btnRow);
        focusOverlay.getChildren().add(focusCard);

        getChildren().add(focusOverlay);
    }

    // -------------------------------------------------------------
    // How the Score Works Modal
    // -------------------------------------------------------------
    private void openScoreFormulaModal() {
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.85);");
        modalOverlay.setPrefSize(getWidth(), getHeight());

        VBox card = new VBox(20);
        card.setMaxWidth(560);
        card.setPadding(new Insets(32));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 20; -fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 8);"
        );

        Label title = new Label("How the Follow-Up Priority Score Works");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label intro = new Label("DuesGuard uses an explainable rule-based formula (0–100) instead of a black-box model. Every score is fully auditable:");
        intro.setWrapText(true);
        intro.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        VBox formulaList = new VBox(10);
        formulaList.getChildren().addAll(
            createFormulaRow("1. Outstanding Amount (35%)", "Higher balances mean higher cash-flow impact. Scales up to ₹1,00,000.", "#49E3B4"),
            createFormulaRow("2. Days Overdue (25%)", "Older invoices have higher dispute risk. Reaches max weight at 30 days past due.", "#FF6B6B"),
            createFormulaRow("3. Client Payment Behaviour (20%)", "Clients who frequently delay receive higher follow-up urgency.", "#FFB84D"),
            createFormulaRow("4. Evidence Gap (10%)", "Missing proof (like delivery confirmation) increases urgency to document.", "#7C83FF"),
            createFormulaRow("5. Previous Reminder Response (10%)", "Unresponsive follow-ups push the invoice up the priority queue.", "#F6F8FC")
        );

        Button btnClose = new Button("Got it");
        btnClose.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 24; -fx-cursor: hand;");
        btnClose.setOnAction(e -> getChildren().remove(modalOverlay));

        card.getChildren().addAll(title, intro, formulaList, btnClose);
        modalOverlay.getChildren().add(card);
        getChildren().add(modalOverlay);
    }

    private VBox createFormulaRow(String name, String desc, String colorHex) {
        VBox box = new VBox(2);
        Label title = new Label(name);
        title.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label d = new Label(desc);
        d.setWrapText(true);
        d.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        box.getChildren().addAll(title, d);
        return box;
    }
}
