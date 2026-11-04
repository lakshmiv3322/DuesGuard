package com.duesguard.view.components;

import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.util.CurrencyFormatter;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JavaFX 3D Cash-Flow Priority Constellation.
 * Renders clients as central hubs and invoices as orbiting 3D nodes.
 * Overdue invoices glow amber/red; paid invoices are green and closer to the centre.
 * Interactive: clicking a node invokes callback with that invoice.
 */
public class Priority3DConstellation extends StackPane {

    public Priority3DConstellation(
        double width,
        double height,
        List<Client> clients,
        List<Invoice> invoices,
        Map<String, Client> clientMap,
        Consumer<Invoice> onInvoiceClick
    ) {
        Group root3D = new Group();
        Group constellationGroup = new Group();

        // Ambient and point lighting
        AmbientLight ambient = new AmbientLight(Color.color(0.3, 0.35, 0.5));
        PointLight light1 = new PointLight(Color.web("#49E3B4"));
        light1.setTranslateX(-150);
        light1.setTranslateY(-150);
        light1.setTranslateZ(-300);

        PointLight light2 = new PointLight(Color.web("#7C83FF"));
        light2.setTranslateX(150);
        light2.setTranslateY(150);
        light2.setTranslateZ(-200);

        root3D.getChildren().addAll(ambient, light1, light2, constellationGroup);

        // Centre core: DuesGuard Hub sphere
        Sphere hub = new Sphere(22);
        PhongMaterial hubMat = new PhongMaterial();
        hubMat.setDiffuseColor(Color.web("#182338"));
        hubMat.setSpecularColor(Color.web("#49E3B4"));
        hubMat.setSpecularPower(20);
        hub.setMaterial(hubMat);
        Tooltip.install(hub, new Tooltip("DuesGuard Central Hub • Payment Recovery Command Centre"));
        constellationGroup.getChildren().add(hub);

        // Position clients in orbit
        int clientCount = Math.max(1, clients.size());
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            double angle = (2 * Math.PI * i) / clientCount;
            double clientRadius = 85;
            double cx = Math.cos(angle) * clientRadius;
            double cz = Math.sin(angle) * clientRadius;

            // Client node sphere
            Sphere clientSphere = new Sphere(14);
            clientSphere.setTranslateX(cx);
            clientSphere.setTranslateZ(cz);

            PhongMaterial cMat = new PhongMaterial();
            cMat.setDiffuseColor(Color.web("#7C83FF"));
            cMat.setSpecularColor(Color.web("#FFFFFF"));
            clientSphere.setMaterial(cMat);
            String relDisplay = (c.getReliability() != null) ? c.getReliability().getDisplay() : "Normal";
            Tooltip.install(clientSphere, new Tooltip("Client: " + c.getName() + " (" + relDisplay + ")\nAvg delay: " + c.getTypicalDelayDays() + " days"));
            constellationGroup.getChildren().add(clientSphere);

            // Connect client to hub with subtle cylinder beam.
            // A JavaFX Cylinder's default axis is Y (vertical).
            // To make it point from origin (0,0,0) toward (cx, 0, cz):
            //   1. Place the beam midpoint at (cx/2, 0, cz/2).
            //   2. Rotate 90° around X-axis → cylinder now lies flat in the XZ plane.
            //   3. Rotate by (90 - angle) around Y-axis → precisely aligns with the client direction vector.
            Cylinder beam = new Cylinder(1.2, clientRadius);
            beam.setTranslateX(cx / 2.0);
            beam.setTranslateZ(cz / 2.0);
            Rotate tiltFlat = new Rotate(90, Rotate.X_AXIS);
            Rotate alignDir = new Rotate(90.0 - Math.toDegrees(angle), Rotate.Y_AXIS);
            beam.getTransforms().addAll(alignDir, tiltFlat);
            PhongMaterial beamMat = new PhongMaterial();
            beamMat.setDiffuseColor(Color.color(0.49, 0.51, 1.0, 0.25));
            beamMat.setSpecularColor(Color.color(0.49, 0.51, 1.0, 0.1));
            beam.setMaterial(beamMat);
            constellationGroup.getChildren().add(beam);

            // Invoices belonging to this client
            List<Invoice> clientInvoices = invoices.stream()
                .filter(inv -> inv.getClientId().equals(c.getId()))
                .toList();

            for (int j = 0; j < clientInvoices.size(); j++) {
                Invoice inv = clientInvoices.get(j);
                // Position invoice node relative to its client sphere hub
                double invAngle = angle + ((j + 1) * (Math.PI / 3.5));
                double invDist = inv.getStatus() == Invoice.Status.PAID ? 36 : 50;
                double ix = cx + Math.cos(invAngle) * invDist;
                double iz = cz + Math.sin(invAngle) * invDist;
                double iy = (j % 2 == 0 ? 1 : -1) * (14 + j * 10);

                // Invoice node: Box or Sphere
                Node invNode;
                PhongMaterial invMat = new PhongMaterial();

                if (inv.getStatus() == Invoice.Status.PAID) {
                    Sphere s = new Sphere(8);
                    invMat.setDiffuseColor(Color.web("#4CAF50"));
                    s.setMaterial(invMat);
                    invNode = s;
                } else if (inv.getDaysOverdue() > 14) {
                    // Critical overdue: glowing coral Box
                    Box b = new Box(16, 16, 16);
                    invMat.setDiffuseColor(Color.web("#FF6B6B"));
                    invMat.setSpecularColor(Color.web("#FFAAAA"));
                    b.setMaterial(invMat);
                    invNode = b;
                } else if (inv.getDaysOverdue() > 0) {
                    // Warning overdue: amber sphere
                    Sphere s = new Sphere(10);
                    invMat.setDiffuseColor(Color.web("#FFB84D"));
                    invMat.setSpecularColor(Color.web("#FFF3CC"));
                    s.setMaterial(invMat);
                    invNode = s;
                } else {
                    // Pending/Sent: electric mint sphere
                    Sphere s = new Sphere(9);
                    invMat.setDiffuseColor(Color.web("#49E3B4"));
                    s.setMaterial(invMat);
                    invNode = s;
                }

                invNode.setTranslateX(ix);
                invNode.setTranslateY(iy);
                invNode.setTranslateZ(iz);
                invNode.setStyle("-fx-cursor: hand;");

                String tipText = inv.getInvoiceNumber() + " (" + c.getName() + ")\n" +
                    "Balance: " + CurrencyFormatter.format(inv.getBalance()) + "\n" +
                    (inv.getDaysOverdue() > 0 ? "⚠ " + inv.getDaysOverdue() + " days overdue" : "Status: " + inv.getStatus().getDisplay()) + "\n" +
                    "Click to inspect in drawer";
                Tooltip.install(invNode, new Tooltip(tipText));

                invNode.setOnMouseClicked(e -> {
                    if (onInvoiceClick != null) onInvoiceClick.accept(inv);
                });

                constellationGroup.getChildren().add(invNode);
            }
        }

        // Camera setup
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);
        camera.setTranslateZ(-340);
        camera.setTranslateY(-40);
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(12);

        SubScene subScene = new SubScene(root3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);
        subScene.setCamera(camera);

        // Slow continuous rotation around Y-axis
        RotateTransition rot = new RotateTransition(Duration.seconds(40), constellationGroup);
        rot.setAxis(Rotate.Y_AXIS);
        rot.setByAngle(360);
        rot.setCycleCount(Animation.INDEFINITE);
        rot.setInterpolator(Interpolator.LINEAR);
        rot.play();

        // Interactive mouse drag to rotate constellation
        final double[] anchorX = new double[1];
        final double[] anchorY = new double[1];
        final double[] anchorAngleX = new double[1];
        final double[] anchorAngleY = new double[1];
        Rotate mouseRotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate mouseRotateY = new Rotate(0, Rotate.Y_AXIS);
        constellationGroup.getTransforms().addAll(mouseRotateY, mouseRotateX);

        subScene.setOnMousePressed(event -> {
            anchorX[0] = event.getSceneX();
            anchorY[0] = event.getSceneY();
            anchorAngleX[0] = mouseRotateX.getAngle();
            anchorAngleY[0] = mouseRotateY.getAngle();
            rot.pause();
        });

        subScene.setOnMouseDragged(event -> {
            mouseRotateY.setAngle(anchorAngleY[0] + (event.getSceneX() - anchorX[0]) * 0.4);
            mouseRotateX.setAngle(anchorAngleX[0] - (event.getSceneY() - anchorY[0]) * 0.4);
        });

        subScene.setOnMouseReleased(event -> rot.play());

        getChildren().add(subScene);
    }
}
