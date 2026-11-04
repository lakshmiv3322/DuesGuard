package com.duesguard.view.components;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * JavaFX 3D Cash-Flow Orb.
 * Uses built-in JavaFX 3D (Sphere, PhongMaterial, PointLight, AmbientLight).
 * Displays a glowing electric-mint 3D sphere with an orbiting ring, gently floating.
 */
public class CashFlow3DOrb extends StackPane {
    private final Runnable onClickAction;

    public CashFlow3DOrb(double width, double height, Runnable onClick) {
        this.onClickAction = onClick;

        // 3D root group
        Group root3D = new Group();

        // Central cash-flow orb (mint glow)
        Sphere orb = new Sphere(22);
        PhongMaterial orbMat = new PhongMaterial();
        orbMat.setDiffuseColor(Color.web("#49E3B4"));
        orbMat.setSpecularColor(Color.web("#FFFFFF"));
        orbMat.setSpecularPower(36.0);
        orb.setMaterial(orbMat);

        // Orbit ring around the orb (violet tilted disk)
        Cylinder ring = new Cylinder(32, 1.8);
        ring.setRotationAxis(Rotate.X_AXIS);
        ring.setRotate(65);
        PhongMaterial ringMat = new PhongMaterial();
        ringMat.setDiffuseColor(Color.web("#7C83FF"));
        ringMat.setSpecularColor(Color.web("#B0B5FF"));
        ring.setMaterial(ringMat);

        // Satellite 1: Inflow node (amber)
        Sphere satellite1 = new Sphere(5);
        satellite1.setTranslateX(34);
        satellite1.setTranslateY(-8);
        satellite1.setTranslateZ(6);
        PhongMaterial sat1Mat = new PhongMaterial();
        sat1Mat.setDiffuseColor(Color.web("#FFB84D"));
        sat1Mat.setSpecularColor(Color.web("#FFF3CC"));
        satellite1.setMaterial(sat1Mat);

        // Satellite 2: Recovered inflow node (violet-mint)
        Sphere satellite2 = new Sphere(4);
        satellite2.setTranslateX(-30);
        satellite2.setTranslateY(8);
        satellite2.setTranslateZ(-8);
        PhongMaterial sat2Mat = new PhongMaterial();
        sat2Mat.setDiffuseColor(Color.web("#5AEFBE"));
        sat2Mat.setSpecularColor(Color.web("#FFFFFF"));
        satellite2.setMaterial(sat2Mat);

        // Lighting
        AmbientLight ambient = new AmbientLight(Color.color(0.35, 0.4, 0.5));
        PointLight point = new PointLight(Color.web("#49E3B4"));
        point.setTranslateX(-60);
        point.setTranslateY(-60);
        point.setTranslateZ(-120);

        PointLight point2 = new PointLight(Color.web("#7C83FF"));
        point2.setTranslateX(60);
        point2.setTranslateY(60);
        point2.setTranslateZ(-80);

        Group orbGroup = new Group(orb, ring, satellite1, satellite2);
        root3D.getChildren().addAll(ambient, point, point2, orbGroup);

        // SubScene with camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(-155);

        SubScene subScene = new SubScene(root3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);
        subScene.setCamera(camera);

        // Gentle floating animation
        TranslateTransition floatTrans = new TranslateTransition(Duration.seconds(2.5), orbGroup);
        floatTrans.setFromY(-5);
        floatTrans.setToY(5);
        floatTrans.setAutoReverse(true);
        floatTrans.setCycleCount(Animation.INDEFINITE);
        floatTrans.setInterpolator(Interpolator.EASE_BOTH);
        floatTrans.play();

        // Slow rotation animation
        RotateTransition rotTrans = new RotateTransition(Duration.seconds(12), orbGroup);
        rotTrans.setAxis(Rotate.Y_AXIS);
        rotTrans.setByAngle(360);
        rotTrans.setCycleCount(Animation.INDEFINITE);
        rotTrans.setInterpolator(Interpolator.LINEAR);
        rotTrans.play();

        getChildren().add(subScene);
        setStyle("-fx-cursor: hand;");

        // Interactive hover feedback
        setOnMouseEntered(e -> {
            rotTrans.setRate(2.0);
            subScene.setScaleX(1.08);
            subScene.setScaleY(1.08);
        });
        setOnMouseExited(e -> {
            rotTrans.setRate(1.0);
            subScene.setScaleX(1.0);
            subScene.setScaleY(1.0);
        });

        setOnMouseClicked(e -> {
            if (onClickAction != null) onClickAction.run();
        });
    }
}
