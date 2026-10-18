package com.duesguard.app;

import com.duesguard.view.components.HeaderBar;
import com.duesguard.view.components.Sidebar;
import javafx.application.Application;
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
        // Minimum dimensions: 1200x800
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
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
        Scene scene = new Scene(rootContainer, 1400, 900);

        // Load CSS
        URL cssUrl = getClass().getResource("/com/duesguard/css/duesguard-dark.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Global Escape key handler to close modals/dialogs
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
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
