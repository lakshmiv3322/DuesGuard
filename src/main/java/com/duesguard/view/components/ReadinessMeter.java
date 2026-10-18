package com.duesguard.view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Payment-readiness horizontal segmented meter and breakdown card.
 * Shows Ready (green), Needs proof (amber), At risk (red).
 */
public class ReadinessMeter extends VBox {

    public ReadinessMeter(int readyCount, int needsDocsCount, int atRiskCount) {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);

        int total = readyCount + needsDocsCount + atRiskCount;
        if (total == 0) total = 1; // avoid divide by zero

        double readyPct = (double) readyCount / total;
        double needsDocsPct = (double) needsDocsCount / total;
        double atRiskPct = (double) atRiskCount / total;

        // Meter bar
        HBox bar = new HBox(3);
        bar.setPrefHeight(10);
        bar.setMaxHeight(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        Rectangle readySeg = new Rectangle();
        readySeg.setHeight(10);
        readySeg.setArcWidth(8);
        readySeg.setArcHeight(8);
        readySeg.setFill(Color.web("#4CAF50"));
        readySeg.widthProperty().bind(bar.widthProperty().multiply(readyPct).subtract(2));

        Rectangle needsDocsSeg = new Rectangle();
        needsDocsSeg.setHeight(10);
        needsDocsSeg.setArcWidth(8);
        needsDocsSeg.setArcHeight(8);
        needsDocsSeg.setFill(Color.web("#FFB84D"));
        needsDocsSeg.widthProperty().bind(bar.widthProperty().multiply(needsDocsPct).subtract(2));

        Rectangle atRiskSeg = new Rectangle();
        atRiskSeg.setHeight(10);
        atRiskSeg.setArcWidth(8);
        atRiskSeg.setArcHeight(8);
        atRiskSeg.setFill(Color.web("#FF6B6B"));
        atRiskSeg.widthProperty().bind(bar.widthProperty().multiply(atRiskPct).subtract(2));

        bar.getChildren().addAll(readySeg, needsDocsSeg, atRiskSeg);

        // Legend row
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);

        legend.getChildren().addAll(
            createLegendItem("#4CAF50", readyCount + " invoices ready"),
            createLegendItem("#FFB84D", needsDocsCount + " need documents"),
            createLegendItem("#FF6B6B", atRiskCount + " high-risk invoices")
        );

        getChildren().addAll(bar, legend);
    }

    private HBox createLegendItem(String colorHex, String labelText) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);

        Rectangle dot = new Rectangle(8, 8);
        dot.setArcWidth(4);
        dot.setArcHeight(4);
        dot.setFill(Color.web(colorHex));

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #9BA8BC;");

        item.getChildren().addAll(dot, label);
        return item;
    }
}
