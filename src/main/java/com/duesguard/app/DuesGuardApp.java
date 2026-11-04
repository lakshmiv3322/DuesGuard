package com.duesguard.app;

import com.duesguard.view.components.HeaderBar;
import com.duesguard.view.components.Sidebar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main application class for DuesGuard.
 * Sets up primary stage, application shell layout (Sidebar + Header + Content),
 * dark theme CSS, keyboard handlers, and navigates to the Splash screen.
 */
public class DuesGuardApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Query primary display visual bounds (taking Windows taskbar and scale into account)
        Rectangle2D visualBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double screenW = visualBounds.getWidth();
        double screenH = visualBounds.getHeight();

        // Responsive window sizing that always fits within user's physical screen
        double prefW = Math.min(1360, Math.max(960, screenW * 0.94));
        double prefH = Math.min(840, Math.max(620, screenH * 0.92));

        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(prefW);
        primaryStage.setHeight(prefH);
        primaryStage.setX(visualBounds.getMinX() + (screenW - prefW) / 2.0);
        primaryStage.setY(visualBounds.getMinY() + (screenH - prefH) / 2.0);
        primaryStage.setTitle("DuesGuard — Know who owes you. Know what to do next.");

        // Root container
        StackPane rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: #0B1020;");

        // Main app layout shell
        BorderPane mainAppLayout = new BorderPane();
        mainAppLayout.setStyle("-fx-background-color: #0B1020;");

        Sidebar sidebar = new Sidebar();
        HeaderBar headerBar = new HeaderBar();
        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #0B1020;");

        mainAppLayout.setLeft(sidebar);
        mainAppLayout.setTop(headerBar);
        mainAppLayout.setCenter(contentArea);

        // Initialize NavigationManager
        NavigationManager nav = NavigationManager.getInstance();
        nav.init(primaryStage, rootContainer);
        nav.setMainAppLayout(mainAppLayout, contentArea);

        // Scene
        Scene scene = new Scene(rootContainer, prefW, prefH);

        // Load CSS
        URL cssUrl = getClass().getResource("/com/duesguard/css/duesguard-dark.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Global shortcut handlers (Ctrl+Q to exit, Escape to close modals)
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Q) {
                Platform.exit();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                // If there are modal overlays on rootContainer, dismiss the topmost
                if (rootContainer.getChildren().size() > 1) {
                    rootContainer.getChildren().remove(rootContainer.getChildren().size() - 1);
                } else if (contentArea.getChildren().size() > 1) {
                    contentArea.getChildren().remove(contentArea.getChildren().size() - 1);
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        // Start at Splash Screen
        nav.navigateTo(NavigationManager.Screen.SPLASH);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
