package com.duesguard.view.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Animated circular score ring displaying priority score (0-100).
 * Animates from 0 to target score on load.
 */
public class ScoreRing extends StackPane {
    private final Arc progressArc;
    private final Label scoreLabel;
    private final DoubleProperty animatedScore = new SimpleDoubleProperty(0);
    private final double targetScore;
    private final double radius;

    public ScoreRing(double targetScore, double radius) {
        this.targetScore = Math.max(0, Math.min(100, targetScore));
        this.radius = radius;

        double strokeWidth = radius * 0.18;

        // Background track circle
        Circle track = new Circle(radius);
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.web("rgba(255,255,255,0.08)"));
        track.setStrokeWidth(strokeWidth);

        // Foreground arc
        progressArc = new Arc(0, 0, radius, radius, 90, 0);
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(getColorForScore(targetScore));
        progressArc.setStrokeWidth(strokeWidth);

        // Inner label
        scoreLabel = new Label("0");
        scoreLabel.setStyle(String.format(
            "-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #F6F8FC;",
            radius * 0.65
        ));

        setAlignment(Pos.CENTER);
        getChildren().addAll(track, progressArc, scoreLabel);
        setPrefSize((radius + strokeWidth) * 2, (radius + strokeWidth) * 2);

        // Bind animation
        animatedScore.addListener((obs, oldVal, newVal) -> {
            double current = newVal.doubleValue();
            // -360 degrees = full circle clockwise from top (90 deg)
            double length = -(current / 100.0) * 360.0;
            progressArc.setLength(length);
            scoreLabel.setText(String.format("%.0f", current));
        });

        playAnimation();
    }

    public void playAnimation() {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(animatedScore, 0.0)),
            new KeyFrame(Duration.millis(750), new KeyValue(animatedScore, targetScore))
        );
        tl.play();
    }

    private Color getColorForScore(double score) {
        if (score >= 80) return Color.web("#FF6B6B"); // Critical coral-red
        if (score >= 60) return Color.web("#FFB84D"); // High amber
        if (score >= 40) return Color.web("#7C83FF"); // Medium violet-blue
        return Color.web("#49E3B4");                 // Low mint
    }

    public double getTargetScore() { return targetScore; }
}
