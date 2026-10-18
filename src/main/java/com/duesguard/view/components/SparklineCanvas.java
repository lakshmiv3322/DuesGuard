package com.duesguard.view.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Lightweight sparkline canvas for KPI cards.
 * Renders smooth trend lines with subtle gradient fills.
 */
public class SparklineCanvas extends Canvas {

    public SparklineCanvas(double width, double height, double[] dataPoints, String strokeHex, boolean positiveTrend) {
        super(width, height);
        draw(dataPoints, strokeHex, positiveTrend);
    }

    public void draw(double[] points, String strokeHex, boolean positive) {
        if (points == null || points.length < 2) return;

        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        gc.clearRect(0, 0, w, h);

        double min = points[0];
        double max = points[0];
        for (double p : points) {
            if (p < min) min = p;
            if (p > max) max = p;
        }
        double range = (max - min == 0) ? 1.0 : (max - min);

        double padY = 4;
        double usableH = h - (padY * 2);
        double stepX = w / (points.length - 1);

        double[] xs = new double[points.length];
        double[] ys = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            xs[i] = i * stepX;
            ys[i] = h - padY - ((points[i] - min) / range * usableH);
        }

        // Area gradient under line
        Color strokeCol = Color.web(strokeHex);
        Color fillTop = Color.color(strokeCol.getRed(), strokeCol.getGreen(), strokeCol.getBlue(), 0.25);
        Color fillBottom = Color.color(strokeCol.getRed(), strokeCol.getGreen(), strokeCol.getBlue(), 0.0);

        LinearGradient areaGrad = new LinearGradient(
            0, 0, 0, h, false, CycleMethod.NO_CYCLE,
            new Stop(0, fillTop), new Stop(1, fillBottom)
        );

        gc.beginPath();
        gc.moveTo(xs[0], h);
        gc.lineTo(xs[0], ys[0]);
        for (int i = 1; i < points.length; i++) {
            gc.lineTo(xs[i], ys[i]);
        }
        gc.lineTo(xs[points.length - 1], h);
        gc.closePath();
        gc.setFill(areaGrad);
        gc.fill();

        // Stroke line
        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < points.length; i++) {
            gc.lineTo(xs[i], ys[i]);
        }
        gc.setStroke(strokeCol);
        gc.setLineWidth(2.0);
        gc.stroke();

        // End circle dot
        gc.setFill(strokeCol);
        gc.fillOval(xs[points.length - 1] - 3, ys[points.length - 1] - 3, 6, 6);
    }
}
