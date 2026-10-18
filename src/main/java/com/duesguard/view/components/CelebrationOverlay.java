package com.duesguard.view.components;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.Random;

/**
 * Recovery celebration overlay and onboarding confetti animation.
 * Features a rising glowing checkmark shield, particle burst, and recovery banner.
 */
public class CelebrationOverlay {

    /**
     * Recovery celebration: shown when an overdue invoice is marked paid.
     * Displays "₹X recovered. Your expected cash flow improved." with glowing particle burst.
     */
    public static void showRecoveryCelebration(Pane root, String recoveredAmount) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.75);");
        overlay.setPrefSize(root.getWidth(), root.getHeight());

        // Card container
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 24; " +
            "-fx-border-color: rgba(73, 227, 180, 0.4); " +
            "-fx-border-radius: 24; " +
            "-fx-border-width: 1.5; " +
            "-fx-padding: 36 48; " +
            "-fx-effect: dropshadow(gaussian, rgba(73, 227, 180, 0.3), 32, 0, 0, 0);"
        );
        card.setMaxWidth(480);
        card.setMaxHeight(300);

        // Rising checkmark shield glyph
        Group glyphGroup = new Group();

        Circle outerGlow = new Circle(44);
        outerGlow.setFill(Color.web("rgba(73, 227, 180, 0.15)"));
        outerGlow.setStroke(Color.web("#49E3B4"));
        outerGlow.setStrokeWidth(2);

        SVGPath check = new SVGPath();
        check.setContent("M -14 0 L -4 10 L 14 -8");
        check.setFill(Color.TRANSPARENT);
        check.setStroke(Color.web("#49E3B4"));
        check.setStrokeWidth(4);
        check.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        check.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        glyphGroup.getChildren().addAll(outerGlow, check);

        Label headline = new Label("Payment Recovered!");
        headline.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #49E3B4;");

        Label amountLabel = new Label(recoveredAmount + " recovered");
        amountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F6F8FC;");

        Label subtext = new Label("Your expected cash flow just improved.");
        subtext.setStyle("-fx-font-size: 13px; -fx-text-fill: #9BA8BC;");

        card.getChildren().addAll(glyphGroup, headline, amountLabel, subtext);
        overlay.getChildren().add(card);

        // Particle burst
        Pane particlePane = new Pane();
        particlePane.setMouseTransparent(true);
        overlay.getChildren().add(particlePane);

        Random rng = new Random();
        Color[] colors = {Color.web("#49E3B4"), Color.web("#7C83FF"), Color.web("#5AEFBE"), Color.web("#FFFFFF")};
        for (int i = 0; i < 28; i++) {
            Circle p = new Circle(rng.nextDouble() * 3 + 2);
            p.setFill(colors[rng.nextInt(colors.length)]);
            p.setTranslateX(root.getWidth() / 2.0);
            p.setTranslateY(root.getHeight() / 2.0);
            particlePane.getChildren().add(p);

            double angle = rng.nextDouble() * 2 * Math.PI;
            double dist = rng.nextDouble() * 160 + 60;
            double destX = (root.getWidth() / 2.0) + Math.cos(angle) * dist;
            double destY = (root.getHeight() / 2.0) + Math.sin(angle) * dist;

            TranslateTransition tt = new TranslateTransition(Duration.millis(900), p);
            tt.setToX(destX);
            tt.setToY(destY);

            FadeTransition ft = new FadeTransition(Duration.millis(900), p);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(tt, ft);
            pt.play();
        }

        // Card entrance
        card.setScaleX(0.7);
        card.setScaleY(0.7);
        card.setOpacity(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(300), card);
        st.setToX(1.0);
        st.setToY(1.0);

        FadeTransition cft = new FadeTransition(Duration.millis(300), card);
        cft.setFromValue(0);
        cft.setToValue(1);

        new ParallelTransition(st, cft).play();

        root.getChildren().add(overlay);

        // Auto dismiss after 2.8 seconds
        PauseTransition wait = new PauseTransition(Duration.millis(2600));
        FadeTransition out = new FadeTransition(Duration.millis(350), overlay);
        out.setFromValue(1.0);
        out.setToValue(0.0);

        SequentialTransition seq = new SequentialTransition(wait, out);
        seq.setOnFinished(e -> root.getChildren().remove(overlay));
        seq.play();

        overlay.setOnMouseClicked(e -> root.getChildren().remove(overlay));
    }

    /**
     * Confetti animation played once after completing the onboarding wizard.
     */
    public static void showOnboardingConfetti(Pane root) {
        Pane confettiPane = new Pane();
        confettiPane.setMouseTransparent(true);
        confettiPane.setPrefSize(root.getWidth(), root.getHeight());
        root.getChildren().add(confettiPane);

        Random rng = new Random();
        Color[] palette = {
            Color.web("#49E3B4"), Color.web("#7C83FF"), Color.web("#FFB84D"),
            Color.web("#FF6B6B"), Color.web("#5AEFBE"), Color.web("#FFFFFF")
        };

        for (int i = 0; i < 50; i++) {
            Circle c = new Circle(rng.nextDouble() * 4 + 3);
            c.setFill(palette[rng.nextInt(palette.length)]);
            c.setTranslateX(rng.nextDouble() * Math.max(800, root.getWidth()));
            c.setTranslateY(-10);
            confettiPane.getChildren().add(c);

            double fallDist = Math.max(600, root.getHeight()) + 20;
            double drift = (rng.nextDouble() - 0.5) * 120;

            TranslateTransition fall = new TranslateTransition(Duration.millis(1200 + rng.nextInt(1000)), c);
            fall.setToY(fallDist);
            fall.setByX(drift);

            FadeTransition fade = new FadeTransition(Duration.millis(1800), c);
            fade.setFromValue(1.0);
            fade.setToValue(0.2);

            ParallelTransition pt = new ParallelTransition(fall, fade);
            pt.setDelay(Duration.millis(rng.nextInt(400)));
            pt.play();
        }

        PauseTransition cleanup = new PauseTransition(Duration.millis(3000));
        cleanup.setOnFinished(e -> root.getChildren().remove(confettiPane));
        cleanup.play();
    }
}
