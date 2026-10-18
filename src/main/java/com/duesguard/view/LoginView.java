package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.AppUser;
import com.duesguard.storage.DataStore;
import com.duesguard.util.PasswordUtil;
import com.duesguard.view.components.DuesGuardLogo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * Screen 2: Login and Sign-up with local file-based auth and Guest Demo mode.
 */
public class LoginView extends StackPane {
    private final TextField usernameField;
    private final TextField emailField;
    private final PasswordField passwordField;
    private final Label errorLabel;
    private boolean isSignUpMode = false;
    private final VBox emailGroup;
    private final Button actionBtn;
    private final Hyperlink toggleLink;

    public LoginView() {
        setStyle("-fx-background-color: #0B1020;");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.setPadding(new Insets(40));
        card.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.08); " +
            "-fx-border-radius: 20; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 8);"
        );

        // Logo + title
        HBox logo = DuesGuardLogo.createBrandLockup(false);
        logo.setAlignment(Pos.CENTER);

        Label title = new Label("Welcome back");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Your money is waiting. Sign in to recover.");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        VBox header = new VBox(6, logo, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // Error message label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        // Username
        VBox userGroup = new VBox(4);
        Label userLbl = new Label("Username");
        userLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        usernameField = new TextField("lakshmi");
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10; -fx-padding: 10;");
        userGroup.getChildren().addAll(userLbl, usernameField);

        // Email (only for signup)
        emailGroup = new VBox(4);
        Label emailLbl = new Label("Email address");
        emailLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10; -fx-padding: 10;");
        emailGroup.getChildren().addAll(emailLbl, emailField);
        emailGroup.setVisible(false);
        emailGroup.setManaged(false);

        // Password
        VBox passGroup = new VBox(4);
        Label passLbl = new Label("Password");
        passLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setText("demo123");
        passwordField.setPromptText("At least 6 characters");
        passwordField.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10; -fx-padding: 10;");
        passGroup.getChildren().addAll(passLbl, passwordField);

        // Sign In / Sign Up button
        actionBtn = new Button("Sign In");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle(
            "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; " +
            "-fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;"
        );
        actionBtn.setOnAction(e -> handleAuth());

        // Guest Demo button
        Button demoBtn = new Button("Continue as Guest (Demo Mode)");
        demoBtn.setMaxWidth(Double.MAX_VALUE);
        demoBtn.setStyle(
            "-fx-background-color: rgba(124, 131, 255, 0.15); -fx-text-fill: #7C83FF; -fx-font-weight: bold; " +
            "-fx-font-size: 13px; -fx-background-radius: 10; -fx-border-color: rgba(124,131,255,0.3); " +
            "-fx-border-radius: 10; -fx-padding: 10; -fx-cursor: hand;"
        );
        demoBtn.setOnAction(e -> {
            AppContext.getInstance().setDemoModeActive(true);
            NavigationManager.getInstance().navigateTo(Screen.DASHBOARD);
        });

        // Switch mode link
        toggleLink = new Hyperlink("Don't have an account? Create Account");
        toggleLink.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 12px; -fx-border-color: transparent;");
        toggleLink.setOnAction(e -> toggleMode());

        card.getChildren().addAll(header, errorLabel, userGroup, emailGroup, passGroup, actionBtn, demoBtn, toggleLink);
        getChildren().add(card);
    }

    private void toggleMode() {
        isSignUpMode = !isSignUpMode;
        emailGroup.setVisible(isSignUpMode);
        emailGroup.setManaged(isSignUpMode);
        actionBtn.setText(isSignUpMode ? "Create Account" : "Sign In");
        toggleLink.setText(isSignUpMode ? "Already have an account? Sign In" : "Don't have an account? Create Account");
        errorLabel.setVisible(false);
    }

    private void handleAuth() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        DataStore store = AppContext.getInstance().getDataStore();

        if (username.isEmpty()) {
            showError("Username is required.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (isSignUpMode) {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showError("Email is required.");
                return;
            }
            if (store.findUserByUsername(username).isPresent()) {
                showError("Username already exists. Please choose another.");
                return;
            }
            // Create user
            AppUser newUser = new AppUser(username, email, PasswordUtil.hash(password));
            store.addUser(newUser);
            store.setCurrentUser(newUser);
            NavigationManager.getInstance().navigateTo(Screen.ONBOARDING);
        } else {
            // Sign in
            Optional<AppUser> userOpt = store.findUserByUsername(username);
            if (userOpt.isEmpty()) {
                // If demo user doesn't exist, create it
                AppUser demoUser = new AppUser(username, username + "@example.com", PasswordUtil.hash(password));
                demoUser.setOnboardingCompleted(true);
                store.addUser(demoUser);
                store.setCurrentUser(demoUser);
                NavigationManager.getInstance().navigateTo(Screen.DASHBOARD);
                return;
            }

            AppUser user = userOpt.get();
            if (!PasswordUtil.verify(password, user.getPasswordHash())) {
                showError("Incorrect password. Please try again.");
                return;
            }

            store.setCurrentUser(user);
            if (!user.isOnboardingCompleted()) {
                NavigationManager.getInstance().navigateTo(Screen.ONBOARDING);
            } else {
                NavigationManager.getInstance().navigateTo(Screen.DASHBOARD);
            }
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
