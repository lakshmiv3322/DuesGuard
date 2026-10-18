package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.service.DemoDataService;
import com.duesguard.view.components.DuesGuardLogo;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Screen 1: Splash / Loading screen.
 * Displays DuesGuard brand logo with 3D rotation/pulse animation,
 * loading progress bar, seeds demo data, and auto-navigates.
 */
public class SplashView extends StackPane {

    public SplashView() {
        setStyle("-fx-background-color: #0B1020;");

        VBox content = new VBox(24);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(460);

        // Logo lockup
        VBox logoLockup = DuesGuardLogo.createSplashLockup();

        // 3D rotation / pulse effect on logo glyph
        Node glyph = logoLockup.getChildren().get(0);
        RotateTransition rt = new RotateTransition(Duration.millis(1600), glyph);
        rt.setAxis(Rotate.Y_AXIS);
        rt.setFromAngle(-25);
        rt.setToAngle(25);
        rt.setAutoReverse(true);
        rt.setCycleCount(2);
        rt.setInterpolator(Interpolator.EASE_BOTH);
        rt.play();

        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), glyph);
        pulse.setFromX(0.92);
        pulse.setFromY(0.92);
        pulse.setToX(1.04);
        pulse.setToY(1.04);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();

        // Loading text
        Label loadingLabel = new Label("Initializing payment recovery centre...");
        loadingLabel.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        // Progress bar
        ProgressBar bar = new ProgressBar(0);
        bar.setPrefWidth(280);
        bar.setPrefHeight(6);
        bar.setStyle("-fx-accent: #49E3B4;");

        Timeline progressTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(bar.progressProperty(), 0.0)),
            new KeyFrame(Duration.millis(400), new KeyValue(bar.progressProperty(), 0.35)),
            new KeyFrame(Duration.millis(900), new KeyValue(bar.progressProperty(), 0.75)),
            new KeyFrame(Duration.millis(1400), new KeyValue(bar.progressProperty(), 1.0))
        );

        content.getChildren().addAll(logoLockup, loadingLabel, bar);
        getChildren().add(content);

        // Seed demo data on splash
        DemoDataService.seedDemoData(AppContext.getInstance().getDataStore());

        progressTimeline.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(250));
            pause.setOnFinished(ev -> {
                // If user is already loaded, go to Dashboard
                if (AppContext.getInstance().getDataStore().getCurrentUser() != null) {
                    NavigationManager.getInstance().navigateTo(Screen.DASHBOARD);
                } else {
                    NavigationManager.getInstance().navigateTo(Screen.LOGIN);
                }
            });
            pause.play();
        });

        progressTimeline.play();
    }
}
