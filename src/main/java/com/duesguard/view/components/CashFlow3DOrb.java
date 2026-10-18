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

        // Central cash-flow orb
        Sphere orb = new Sphere(28);
        PhongMaterial orbMat = new PhongMaterial();
        orbMat.setDiffuseColor(Color.web("#49E3B4"));
        orbMat.setSpecularColor(Color.web("#FFFFFF"));
        orbMat.setSpecularPower(32.0);
        orb.setMaterial(orbMat);

        // Orbit ring around the orb
        Cylinder ring = new Cylinder(38, 2);
        ring.setRotationAxis(Rotate.X_AXIS);
        ring.setRotate(65);
        PhongMaterial ringMat = new PhongMaterial();
        ringMat.setDiffuseColor(Color.web("#7C83FF"));
        ringMat.setSpecularColor(Color.web("#B0B5FF"));
        ring.setMaterial(ringMat);

        // Secondary small satellite node
        Sphere satellite = new Sphere(6);
        satellite.setTranslateX(42);
        satellite.setTranslateY(-10);
        PhongMaterial satMat = new PhongMaterial();
        satMat.setDiffuseColor(Color.web("#FFB84D"));
        satellite.setMaterial(satMat);

        // Lighting
        AmbientLight ambient = new AmbientLight(Color.color(0.3, 0.35, 0.45));
        PointLight point = new PointLight(Color.web("#49E3B4"));
        point.setTranslateX(-60);
        point.setTranslateY(-60);
        point.setTranslateZ(-120);

        PointLight point2 = new PointLight(Color.web("#7C83FF"));
        point2.setTranslateX(60);
        point2.setTranslateY(60);
        point2.setTranslateZ(-80);

        Group orbGroup = new Group(orb, ring, satellite);
        root3D.getChildren().addAll(ambient, point, point2, orbGroup);

        // SubScene with camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(-160);

        SubScene subScene = new SubScene(root3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);
        subScene.setCamera(camera);

        // Gentle floating animation
        TranslateTransition floatTrans = new TranslateTransition(Duration.seconds(2.5), orbGroup);
        floatTrans.setFromY(-6);
        floatTrans.setToY(6);
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

        setOnMouseClicked(e -> {
            if (onClickAction != null) onClickAction.run();
        });
    }
}
