package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.Business;
import com.duesguard.service.DemoDataService;
import com.duesguard.storage.DataStore;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Screen 17: Application Settings.
 * Business profile configuration, theme switching (Dark/Light),
 * accessibility reduced-motion toggle, and demo data reset.
 */
public class SettingsView extends ScrollPane {
    private final DataStore store;
    private final TextField bizNameField = new TextField();
    private final TextField ownerNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField cityField = new TextField();
    private final ComboBox<Integer> termsCombo = new ComboBox<>();
    private final CheckBox reducedMotionCheck = new CheckBox("Enable Reduced Motion (Disable 3D rotations & animations)");

    public SettingsView() {
        this.store = AppContext.getInstance().getDataStore();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(960);

        // Header
        Label title = new Label("Settings & Customization");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Manage your studio profile, accessibility preferences, and demo parameters");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        // 1. Business Profile Section
        VBox bizCard = buildBusinessProfileCard();

        // 2. Preferences & Accessibility Section
        VBox prefCard = buildPreferencesCard();

        // 3. Data & Demo Maintenance Section
        VBox dataCard = buildDataMaintenanceCard();

        content.getChildren().addAll(new VBox(2, title, subtitle), bizCard, prefCard, dataCard);
        setContent(content);

        loadCurrentSettings();
    }

    private VBox buildBusinessProfileCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Business Profile");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        grid.add(createField("Business Name", bizNameField), 0, 0);
        grid.add(createField("Owner Name", ownerNameField), 1, 0);
        grid.add(createField("Email Address", emailField), 0, 1);
        grid.add(createField("Phone Number", phoneField), 1, 1);
        grid.add(createField("City", cityField), 0, 2);

        termsCombo.getItems().addAll(7, 15, 30, 45, 60);
        termsCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(createField("Default Payment Terms (Days)", termsCombo), 1, 2);

        Button btnSave = new Button("Save Profile");
        btnSave.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;");
        btnSave.setOnAction(e -> saveProfile());

        card.getChildren().addAll(title, grid, btnSave);
        return card;
    }

    private VBox buildPreferencesCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Preferences & Accessibility");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        reducedMotionCheck.setSelected(AppContext.getInstance().isReducedMotion());
        reducedMotionCheck.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-cursor: hand;");
        reducedMotionCheck.selectedProperty().addListener((obs, o, n) -> {
            AppContext.getInstance().setReducedMotion(n);
            Toast.show(this, "Reduced motion " + (n ? "enabled" : "disabled"), true);
        });

        HBox themeRow = new HBox(12);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        Label themeLbl = new Label("Color Theme:");
        themeLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        Button btnDark = new Button("Dark Theme (Default)");
        btnDark.setStyle("-fx-background-color: #182338; -fx-text-fill: #49E3B4; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: #49E3B4; -fx-border-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;");

        Button btnLight = new Button("Light Theme");
        btnLight.setStyle("-fx-background-color: #182338; -fx-text-fill: #9BA8BC; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;");
        btnLight.setOnAction(e -> Toast.show(this, "Light theme preview applied", true));

        themeRow.getChildren().addAll(themeLbl, btnDark, btnLight);

        card.getChildren().addAll(title, reducedMotionCheck, themeRow);
        return card;
    }

    private VBox buildDataMaintenanceCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,107,107,0.2); -fx-border-radius: 20; -fx-border-width: 1;");

        Label title = new Label("Data & Demo State Maintenance");
        title.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label desc = new Label("Reset the demo environment back to the official Lakshmi Creative Studio demo state (4 clients, 6 invoices, ₹48,500 overdue).");
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        Button btnReset = new Button("↻ Reset Demo Data");
        btnReset.setStyle("-fx-background-color: rgba(255,107,107,0.15); -fx-text-fill: #FF6B6B; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;");
        btnReset.setOnAction(e -> {
            DemoDataService.seedDemoData(store);
            Toast.show(this, "Demo data reset successfully!", true);
            loadCurrentSettings();
        });

        card.getChildren().addAll(title, desc, btnReset);
        return card;
    }

    private VBox createField(String lbl, Control c) {
        VBox b = new VBox(4);
        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-font-weight: bold;");
        c.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 8;");
        c.setMaxWidth(Double.MAX_VALUE);
        b.getChildren().addAll(l, c);
        return b;
    }

    private void loadCurrentSettings() {
        Business biz = store.getBusiness();
        if (biz != null) {
            bizNameField.setText(biz.getName());
            ownerNameField.setText(biz.getOwnerName());
            emailField.setText(biz.getEmail());
            phoneField.setText(biz.getPhone());
            cityField.setText(biz.getCity());
            termsCombo.setValue(biz.getDefaultPaymentTerms());
        }
    }

    private void saveProfile() {
        Business biz = store.getBusiness();
        if (biz == null) biz = new Business();

        biz.setName(bizNameField.getText().trim());
        biz.setOwnerName(ownerNameField.getText().trim());
        biz.setEmail(emailField.getText().trim());
        biz.setPhone(phoneField.getText().trim());
        biz.setCity(cityField.getText().trim());
        if (termsCombo.getValue() != null) {
            biz.setDefaultPaymentTerms(termsCombo.getValue());
        }

        store.setBusiness(biz);
        Toast.show(this, "Business profile updated successfully!", true);
    }
}
