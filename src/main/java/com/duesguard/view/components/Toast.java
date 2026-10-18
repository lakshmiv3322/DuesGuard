package com.duesguard.view.components;

import com.duesguard.app.NavigationManager;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Animated toast notification that slides in from top-right and fades out.
 */
public class Toast {

    public static void show(Node context, String message, boolean isSuccess) {
        Pane parent = null;
        if (context instanceof Pane p) {
            parent = p;
        } else if (context != null && context.getScene() != null && context.getScene().getRoot() instanceof Pane p) {
            parent = p;
        } else {
            parent = NavigationManager.getInstance().getRootContainer();
        }

        if (parent == null) return;

        HBox toast = new HBox(12);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().add("toast");
        toast.setStyle(
            "-fx-background-color: #182338; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + (isSuccess ? "#49E3B4" : "#FF6B6B") + "; " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 12 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 18, 0, 0, 6);"
        );

        Circle indicator = new Circle(5);
        indicator.setFill(Color.web(isSuccess ? "#49E3B4" : "#FF6B6B"));

        Label text = new Label(message);
        text.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-font-weight: 500;");

        toast.getChildren().addAll(indicator, text);
        toast.setOpacity(0);
        toast.setTranslateY(-20);

        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new javafx.geometry.Insets(24, 28, 0, 0));

        final Pane targetParent = parent;
        targetParent.getChildren().add(toast);

        // Entrance animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(250), toast);
        slideDown.setFromY(-20);
        slideDown.setToY(0);

        ParallelTransition enter = new ParallelTransition(fadeIn, slideDown);

        // Pause
        PauseTransition pause = new PauseTransition(Duration.millis(2800));

        // Exit animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition seq = new SequentialTransition(enter, pause, fadeOut);
        seq.setOnFinished(e -> targetParent.getChildren().remove(toast));
        seq.play();
    }

    public static void show(String message, boolean isSuccess) {
        show(NavigationManager.getInstance().getRootContainer(), message, isSuccess);
    }
}
