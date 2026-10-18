package com.duesguard.view;

import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Screen 18: Help & Onboarding Checklist.
 * Comprehensive guide, keyboard shortcuts, FAQ, and interactive dispute-readiness checklist.
 */
public class HelpView extends ScrollPane {

    public HelpView() {
        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1000);

        // Header
        Label title = new Label("Help Centre & Getting Dispute-Ready");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Step-by-step checklist, keyboard shortcuts, and payment recovery guidelines");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        // 1. Getting Dispute-Ready Checklist
        VBox checklistCard = buildChecklistCard();

        // 2. Core Product Principles (Why DuesGuard)
        VBox principlesCard = buildPrinciplesCard();

        // 3. Keyboard Shortcuts Reference
        VBox shortcutsCard = buildShortcutsCard();

        content.getChildren().addAll(new VBox(2, title, subtitle), checklistCard, principlesCard, shortcutsCard);
        setContent(content);
    }

    private VBox buildChecklistCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(73, 227, 180, 0.2); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Dispute-Ready Small Business Checklist");
        title.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox items = new VBox(10);
        items.getChildren().addAll(
            createCheckRow("Complete business profile and default payment terms", true),
            createCheckRow("Seed or import your client directory with communication preferences", true),
            createCheckRow("Attach approved quotations before issuing invoices", true),
            createCheckRow("Upload deliverable / completion proof as soon as service is rendered", false),
            createCheckRow("Send respectful, progressive reminders via WhatsApp / Email", true),
            createCheckRow("Record partial settlements immediately to keep priority queue accurate", true)
        );

        card.getChildren().addAll(title, items);
        return card;
    }

    private HBox createCheckRow(String text, boolean checked) {
        HBox r = new HBox(12);
        r.setAlignment(Pos.CENTER_LEFT);
        CheckBox cb = new CheckBox(text);
        cb.setSelected(checked);
        cb.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-cursor: hand;");
        r.getChildren().add(cb);
        return r;
    }

    private VBox buildPrinciplesCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Product Principles: Why DuesGuard?");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox points = new VBox(8);
        points.getChildren().addAll(
            createPrinciple("Not Another Accounting App", "DuesGuard does not clutter you with balance sheets or depreciation. It answers one question: 'Who owes me money and what do I do next?'"),
            createPrinciple("Action-Oriented Follow-Ups", "Instead of static lists, invoices are ranked by a transparent 5-factor priority formula with generated copy-ready messages."),
            createPrinciple("Dispute-Proof Readiness", "Helps freelancers and micro-businesses collect work completion proofs and delivery sign-offs before disputes arise.")
        );

        card.getChildren().addAll(title, points);
        return card;
    }

    private VBox createPrinciple(String heading, String desc) {
        VBox b = new VBox(2);
        Label h = new Label(heading);
        h.setStyle("-fx-text-fill: #7C83FF; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label d = new Label(desc);
        d.setWrapText(true);
        d.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        b.getChildren().addAll(h, d);
        return b;
    }

    private VBox buildShortcutsCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Keyboard Navigation & Shortcuts");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(8);

        grid.add(createShortcut("Esc", "Close any active modal dialog or drawer"), 0, 0);
        grid.add(createShortcut("Tab / Shift+Tab", "Navigate between inputs and interactive buttons"), 1, 0);
        grid.add(createShortcut("Enter", "Submit focused action or confirm payment"), 0, 1);
        grid.add(createShortcut("Space", "Toggle checkboxes and period selectors"), 1, 1);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private HBox createShortcut(String key, String desc) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);
        Label k = new Label(key);
        k.setStyle("-fx-background-color: #182338; -fx-text-fill: #49E3B4; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 8; -fx-font-size: 11px;");
        Label d = new Label(desc);
        d.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        r.getChildren().addAll(k, d);
        return r;
    }
}
