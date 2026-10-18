package com.duesguard.view.components;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.util.DateUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.time.LocalDate;

/**
 * Top header bar for DuesGuard (height 72px).
 * Displays greeting, search field, date indicator, Judge Demo mode badge,
 * notification bell, and "+ Create invoice" button.
 */
public class HeaderBar extends HBox {
    private final Label greetingLabel;
    private final Label subtitleLabel;
    private final TextField searchField;
    private final Label demoBadge;

    public HeaderBar() {
        setPrefHeight(72);
        setMinHeight(72);
        setPadding(new Insets(0, 28, 0, 28));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("top-header");
        setStyle("-fx-background-color: #0D1526; -fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 0 0 1 0;");

        // Left greeting & subtitle
        VBox greetingBox = new VBox(2);
        greetingBox.setAlignment(Pos.CENTER_LEFT);

        greetingLabel = new Label("Good morning, Lakshmi");
        greetingLabel.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        subtitleLabel = new Label("Here is what needs your attention today.");
        subtitleLabel.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        greetingBox.getChildren().addAll(greetingLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right actions
        HBox rightBox = new HBox(14);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        // Demo Mode Badge
        demoBadge = new Label("DEMO MODE");
        demoBadge.getStyleClass().add("demo-badge");
        demoBadge.setStyle(
            "-fx-background-color: rgba(255, 184, 77, 0.15); " +
            "-fx-border-color: rgba(255, 184, 77, 0.4); " +
            "-fx-border-radius: 100; " +
            "-fx-background-radius: 100; " +
            "-fx-border-width: 1; " +
            "-fx-text-fill: #FFB84D; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 4 10; " +
            "-fx-cursor: hand;"
        );
        demoBadge.setOnMouseClicked(e -> NavigationManager.getInstance().navigateTo(Screen.JURY_DEMO));

        // Date indicator pill
        HBox datePill = new HBox(6);
        datePill.setAlignment(Pos.CENTER);
        datePill.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04); " +
            "-fx-border-color: rgba(255,255,255,0.08); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 6 12;"
        );
        Label dateText = new Label(DateUtil.formatDisplay(LocalDate.now()));
        dateText.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        datePill.getChildren().add(dateText);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search invoices, clients...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(220);
        searchField.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (!q.isEmpty()) {
                NavigationManager.getInstance().navigateTo(Screen.INVOICES);
            }
        });

        // Notification bell with red dot
        StackPane notifBtn = new StackPane();
        notifBtn.setStyle("-fx-cursor: hand; -fx-padding: 6;");
        SVGPath bell = new SVGPath();
        bell.setContent("M 18 8 A 6 6 0 0 0 6 8 C 6 15 3 17 3 17 H 21 C 21 17 18 15 18 8 M 13.73 21 A 2 2 0 0 1 10.27 21");
        bell.setFill(Color.TRANSPARENT);
        bell.setStroke(Color.web("#9BA8BC"));
        bell.setStrokeWidth(1.8);
        bell.setScaleX(0.75);
        bell.setScaleY(0.75);

        Circle dot = new Circle(4);
        dot.setFill(Color.web("#FF6B6B"));
        StackPane.setAlignment(dot, Pos.TOP_RIGHT);

        notifBtn.getChildren().addAll(bell, dot);
        notifBtn.setOnMouseClicked(e -> NavigationManager.getInstance().navigateTo(Screen.PRIORITY_QUEUE));

        // + Create Invoice Button
        Button btnCreate = new Button("+ Create invoice");
        btnCreate.getStyleClass().add("btn-primary");
        btnCreate.setStyle(
            "-fx-background-color: #49E3B4; " +
            "-fx-text-fill: #0B1020; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 8 16; " +
            "-fx-cursor: hand;"
        );
        btnCreate.setOnAction(e -> {
            NavigationManager.getInstance().setSelectedInvoice(null);
            NavigationManager.getInstance().navigateTo(Screen.CREATE_INVOICE);
        });

        rightBox.getChildren().addAll(demoBadge, datePill, searchField, notifBtn, btnCreate);

        getChildren().addAll(greetingBox, spacer, rightBox);
    }

    public void setGreeting(String greeting, String subtitle) {
        greetingLabel.setText(greeting);
        subtitleLabel.setText(subtitle);
    }

    public String getSearchQuery() {
        return searchField.getText();
    }
}
