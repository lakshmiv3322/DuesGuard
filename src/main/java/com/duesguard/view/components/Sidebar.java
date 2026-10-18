package com.duesguard.view.components;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.Invoice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed left sidebar (width 248px) for DuesGuard.
 * Provides categorized navigation with active indicators and urgent-item count badges.
 */
public class Sidebar extends VBox {
    private final Map<Screen, Button> navButtons = new HashMap<>();
    private Screen activeScreen = Screen.DASHBOARD;
    private Label urgentBadge;

    public Sidebar() {
        setPrefWidth(248);
        setMinWidth(248);
        setMaxWidth(248);
        getStyleClass().add("sidebar");
        setStyle("-fx-background-color: #0D1526; -fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 0 1 0 0;");

        // Top Brand Logo
        HBox logoBox = DuesGuardLogo.createBrandLockup(true);
        logoBox.getStyleClass().add("sidebar-logo-area");
        logoBox.setPadding(new Insets(24, 20, 20, 20));
        logoBox.setStyle("-fx-cursor: hand;");
        logoBox.setOnMouseClicked(e -> NavigationManager.getInstance().navigateTo(Screen.DASHBOARD));

        // Scrollable nav list
        VBox navList = new VBox(4);
        navList.setPadding(new Insets(0, 12, 16, 12));

        // Section: RECOVERY
        navList.getChildren().add(createSectionHeader("RECOVERY COMMAND"));
        Button btnDash = createNavItem("Dashboard", "M 3 9 L 12 2 L 21 9 L 21 20 A 2 2 0 0 1 19 22 L 5 22 A 2 2 0 0 1 3 20 Z", Screen.DASHBOARD);
        Button btnQueue = createNavItemWithBadge("Priority Queue", "M 13 2 L 3 14 L 12 14 L 11 22 L 21 10 L 12 10 Z", Screen.PRIORITY_QUEUE);
        Button btnRemind = createNavItem("Reminder Composer", "M 21 15 A 2 2 0 0 1 19 17 L 7 17 L 3 21 L 3 5 A 2 2 0 0 1 5 3 L 19 3 A 2 2 0 0 1 21 5 Z", Screen.REMINDER_COMPOSER);
        Button btnCash = createNavItem("Cash-Flow Radar", "M 2 12 A 10 10 0 1 0 22 12 M 6 12 A 6 6 0 1 0 18 12 M 10 12 A 2 2 0 1 0 14 12", Screen.CASH_FLOW);
        navList.getChildren().addAll(btnDash, btnQueue, btnRemind, btnCash);

        // Section: MANAGEMENT
        navList.getChildren().add(createSectionHeader("MANAGEMENT"));
        Button btnInv = createNavItem("Invoices", "M 14 2 H 6 A 2 2 0 0 0 4 4 V 20 A 2 2 0 0 0 6 22 H 18 A 2 2 0 0 0 20 20 V 8 Z M 14 2 V 8 H 20", Screen.INVOICES);
        Button btnClients = createNavItem("Clients", "M 17 21 V 19 A 4 4 0 0 0 13 15 H 7 A 4 4 0 0 0 3 19 V 21 M 10 11 A 4 4 0 1 0 10 3 A 4 4 0 0 0 10 11 Z", Screen.CLIENTS);
        Button btnPayments = createNavItem("Payments", "M 2 5 H 22 M 2 10 H 22 M 2 4 A 2 2 0 0 1 4 2 H 20 A 2 2 0 0 1 22 4 V 20 A 2 2 0 0 1 20 22 H 4 A 2 2 0 0 1 2 20 Z", Screen.PAYMENTS);
        Button btnDocs = createNavItem("Documents", "M 22 19 A 2 2 0 0 1 20 21 H 4 A 2 2 0 0 1 2 19 V 5 A 2 2 0 0 1 4 3 H 9 L 11 6 H 20 A 2 2 0 0 1 22 8 Z", Screen.DOCUMENTS);
        navList.getChildren().addAll(btnInv, btnClients, btnPayments, btnDocs);

        // Section: INSIGHTS & TOOLS
        navList.getChildren().add(createSectionHeader("INSIGHTS & SYSTEM"));
        Button btnReports = createNavItem("Reports", "M 18 20 V 10 M 12 20 V 4 M 6 20 V 14", Screen.REPORTS);
        Button btnSettings = createNavItem("Settings", "M 12 15 A 3 3 0 1 0 12 9 A 3 3 0 0 0 12 15 Z M 19.4 15 A 1.65 1.65 0 0 0 20 16.6", Screen.SETTINGS);
        Button btnHelp = createNavItem("Help & Checklist", "M 12 22 A 10 10 0 1 0 12 2 A 10 10 0 0 0 12 22 Z M 9.09 9 A 3 3 0 0 1 15 10 C 15 12 12 12.5 12 14 M 12 17 H 12.01", Screen.HELP);
        Button btnJudge = createJudgeNavItem();
        navList.getChildren().addAll(btnReports, btnSettings, btnHelp, btnJudge);

        ScrollPane scroll = new ScrollPane(navList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Bottom User Profile Pill
        VBox bottomArea = createBottomProfilePill();

        getChildren().addAll(logoBox, scroll, bottomArea);
        updateUrgentCount();
    }

    private Label createSectionHeader(String title) {
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 14 8 4 8; -fx-letter-spacing: 1px;");
        return lbl;
    }

    private Button createNavItem(String text, String svgPath, Screen screen) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("nav-item");

        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.TRANSPARENT);
        icon.setStroke(Color.web("#9BA8BC"));
        icon.setStrokeWidth(1.6);
        icon.setScaleX(0.75);
        icon.setScaleY(0.75);

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        content.getChildren().addAll(icon, label);
        btn.setGraphic(content);

        btn.setOnAction(e -> NavigationManager.getInstance().navigateTo(screen));
        navButtons.put(screen, btn);
        return btn;
    }

    private Button createNavItemWithBadge(String text, String svgPath, Screen screen) {
        Button btn = createNavItem(text, svgPath, screen);
        HBox content = (HBox) btn.getGraphic();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        urgentBadge = new Label("4");
        urgentBadge.setStyle(
            "-fx-background-color: #FF6B6B; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 100; " +
            "-fx-padding: 1 6;"
        );

        content.getChildren().addAll(spacer, urgentBadge);
        return btn;
    }

    private Button createJudgeNavItem() {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("nav-item");

        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = new SVGPath();
        icon.setContent("M 12 2 L 15.09 8.26 L 22 9.27 L 17 14.14 L 18.18 21.02 L 12 17.77 L 5.82 21.02 L 7 14.14 L 2 9.27 L 8.91 8.26 Z");
        icon.setFill(Color.web("#FFB84D"));
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);

        Label label = new Label("Judge Demo Mode");
        label.setStyle("-fx-text-fill: #FFB84D; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("TOUR");
        badge.setStyle(
            "-fx-background-color: rgba(255, 184, 77, 0.2); " +
            "-fx-border-color: rgba(255, 184, 77, 0.5); " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-border-width: 1; " +
            "-fx-text-fill: #FFB84D; " +
            "-fx-font-size: 9px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 1 5;"
        );

        content.getChildren().addAll(icon, label, spacer, badge);
        btn.setGraphic(content);
        btn.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.JURY_DEMO));
        navButtons.put(Screen.JURY_DEMO, btn);
        return btn;
    }

    private VBox createBottomProfilePill() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12, 16, 16, 16));
        box.setStyle("-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1 0 0 0;");

        HBox profileRow = new HBox(10);
        profileRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        Circle circ = new Circle(16);
        circ.setFill(Color.web("#7C83FF"));
        Label initials = new Label("LV");
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        avatar.getChildren().addAll(circ, initials);

        VBox userText = new VBox(1);
        Label name = new Label("Lakshmi V.");
        name.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label role = new Label("Creative Studio");
        role.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 10px;");
        userText.getChildren().addAll(name, role);

        profileRow.getChildren().addAll(avatar, userText);
        profileRow.setStyle("-fx-cursor: hand;");
        profileRow.setOnMouseClicked(e -> NavigationManager.getInstance().navigateTo(Screen.SETTINGS));

        box.getChildren().add(profileRow);
        return box;
    }

    public void setActiveScreen(Screen screen) {
        this.activeScreen = screen;
        navButtons.forEach((s, btn) -> {
            if (btn.getGraphic() instanceof HBox hbox) {
                boolean active = (s == screen);
                btn.setStyle(active
                    ? "-fx-background-color: rgba(73, 227, 180, 0.12); -fx-background-radius: 10;"
                    : "-fx-background-color: transparent;");

                for (Node child : hbox.getChildren()) {
                    if (child instanceof Label lbl && !lbl.getText().equals("TOUR")) {
                        lbl.setStyle(active
                            ? "-fx-text-fill: #49E3B4; -fx-font-size: 13px; -fx-font-weight: bold;"
                            : "-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
                    }
                    if (child instanceof SVGPath p) {
                        p.setStroke(active ? Color.web("#49E3B4") : Color.web("#9BA8BC"));
                    }
                }
            }
        });
    }

    public void updateUrgentCount() {
        if (urgentBadge == null) return;
        List<Invoice> active = AppContext.getInstance().getDataStore().getActiveInvoices();
        long count = active.stream().filter(i -> i.getDaysOverdue() > 0).count();
        urgentBadge.setText(String.valueOf(count));
        urgentBadge.setVisible(count > 0);
    }
}
