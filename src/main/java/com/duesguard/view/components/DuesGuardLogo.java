package com.duesguard.view.components;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;

/**
 * DuesGuard brand logo built using pure JavaFX shapes.
 * Combines an abstract payment-protection shield with a cash-flow circular arrow / checkmark.
 */
public class DuesGuardLogo {

    /**
     * Creates the abstract shield + circular arrow glyph icon at a given size.
     */
    public static Node createGlyph(double size) {
        double scale = size / 40.0;
        Group group = new Group();

        // Linear gradient: mint to violet
        LinearGradient shieldGrad = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#49E3B4")),
            new Stop(1, Color.web("#7C83FF"))
        );

        // Shield outer path (40x44 coordinate space)
        SVGPath shield = new SVGPath();
        shield.setContent("M 20 2 C 28 2, 36 6, 38 12 C 38 28, 28 38, 20 42 C 12 38, 2 28, 2 12 C 4 6, 12 2, 20 2 Z");
        shield.setFill(Color.web("#121A2B"));
        shield.setStroke(shieldGrad);
        shield.setStrokeWidth(2.5 * scale);
        shield.setScaleX(scale);
        shield.setScaleY(scale);

        // Circular flow arrow inside shield
        SVGPath arrow = new SVGPath();
        arrow.setContent("M 20 10 A 10 10 0 1 1 12 15 L 12 11 M 12 15 L 16 15");
        arrow.setFill(Color.TRANSPARENT);
        arrow.setStroke(Color.web("#49E3B4"));
        arrow.setStrokeWidth(2.0 * scale);
        arrow.setStrokeLineCap(StrokeLineCap.ROUND);
        arrow.setStrokeLineJoin(StrokeLineJoin.ROUND);
        arrow.setScaleX(scale);
        arrow.setScaleY(scale);

        // Inner glowing dot / check accent
        Circle dot = new Circle(20 * scale, 22 * scale, 3 * scale);
        dot.setFill(Color.web("#49E3B4"));

        group.getChildren().addAll(shield, arrow, dot);
        return group;
    }

    /**
     * Sidebar logo brand lockup: glyph + wordmark + tagline.
     */
    public static HBox createBrandLockup(boolean showTagline) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Node glyph = createGlyph(36);

        VBox textCol = new VBox(2);
        textCol.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("DuesGuard");
        nameLabel.getStyleClass().add("sidebar-brand-text");
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F6F8FC; -fx-font-family: 'Segoe UI', sans-serif;");

        textCol.getChildren().add(nameLabel);

        if (showTagline) {
            Label tagline = new Label("Payment recovery centre");
            tagline.setStyle("-fx-font-size: 10px; -fx-text-fill: #9BA8BC;");
            textCol.getChildren().add(tagline);
        }

        box.getChildren().addAll(glyph, textCol);
        return box;
    }

    /**
     * Large splash screen logo lockup.
     */
    public static VBox createSplashLockup() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);

        Node glyph = createGlyph(80);

        Label title = new Label("DUESGUARD");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #F6F8FC; -fx-letter-spacing: 2px;");

        Label tagline = new Label("Know who owes you. Know what to do next.");
        tagline.setStyle("-fx-font-size: 14px; -fx-text-fill: #49E3B4; -fx-font-style: italic;");

        Label desc = new Label("Payment recovery command centre for small businesses");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #9BA8BC;");

        box.getChildren().addAll(glyph, title, tagline, desc);
        return box;
    }
}
