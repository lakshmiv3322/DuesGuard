package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

/**
 * Screen 9: Clients Directory.
 * Displays all clients with their neutral reliability labels,
 * active invoices count, total outstanding balances, and quick actions.
 */
public class ClientsView extends ScrollPane {
    private final DataStore store;
    private final VBox clientsGridContainer;
    private String searchQuery = "";

    public ClientsView() {
        this.store = AppContext.getInstance().getDataStore();

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        // Header
        content.getChildren().add(buildHeader());

        // Grid Container
        clientsGridContainer = new VBox(12);
        content.getChildren().add(clientsGridContainer);

        setContent(content);
        refreshClients();
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(2);
        Label title = new Label("Clients & Payment Behaviour");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Neutral payment delay tracking based on historical transaction records");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search
        TextField search = new TextField();
        search.setPromptText("Search clients...");
        search.setStyle("-fx-background-color: #121A2B; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 7 12;");
        search.textProperty().addListener((obs, o, n) -> {
            searchQuery = n.trim().toLowerCase();
            refreshClients();
        });

        // + Add Client Button
        Button btnAdd = new Button("+ Add Client");
        btnAdd.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> openAddClientDialog());

        header.getChildren().addAll(titleCol, spacer, search, btnAdd);
        return header;
    }

    private void refreshClients() {
        clientsGridContainer.getChildren().clear();

        List<Client> clients = store.getClients().stream()
            .filter(c -> {
                if (searchQuery.isEmpty()) return true;
                return c.getName().toLowerCase().contains(searchQuery)
                    || (c.getCompany() != null && c.getCompany().toLowerCase().contains(searchQuery))
                    || (c.getEmail() != null && c.getEmail().toLowerCase().contains(searchQuery));
            })
            .toList();

        if (clients.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(48));
            Label emptyLbl = new Label("No clients found.");
            emptyLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
            empty.getChildren().add(emptyLbl);
            clientsGridContainer.getChildren().add(empty);
            return;
        }

        // 2-column grid of client cards
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            grid.add(createClientCard(c), i % 2, i / 2);
        }

        clientsGridContainer.getChildren().add(grid);
    }

    private VBox createClientCard(Client c) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 18; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 18; -fx-border-width: 1; -fx-cursor: hand;"
        );

        card.setOnMouseClicked(e -> {
            NavigationManager.getInstance().setSelectedClient(c);
            NavigationManager.getInstance().navigateTo(Screen.CLIENT_DETAILS);
        });

        // Top Row: Avatar + Name + Reliability Pill
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Circle av = new Circle(18);
        av.setFill(Color.web("#7C83FF"));
        Label initials = new Label(c.getName().substring(0, Math.min(2, c.getName().length())).toUpperCase());
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        StackPane avPane = new StackPane(av, initials);

        VBox nameCol = new VBox(2);
        Label nameLbl = new Label(c.getName());
        nameLbl.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label compLbl = new Label(c.getEmail() != null ? c.getEmail() : "No email");
        compLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        nameCol.getChildren().addAll(nameLbl, compLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Neutral Reliability Label
        Label relPill = new Label(c.getReliabilityLabel().getDisplay());
        relPill.setStyle(
            "-fx-background-color: " + getReliabilityBg(c.getReliabilityLabel()) + "; " +
            "-fx-text-fill: " + getReliabilityColor(c.getReliabilityLabel()) + "; " +
            "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 100; -fx-padding: 3 8;"
        );

        topRow.getChildren().addAll(avPane, nameCol, spacer, relPill);

        // Metrics Row
        List<Invoice> cInvoices = store.getActiveInvoices().stream()
            .filter(inv -> inv.getClientId().equals(c.getId()))
            .toList();

        double totalOutstanding = cInvoices.stream().filter(i -> i.getStatus() != Invoice.Status.PAID).mapToDouble(Invoice::getBalance).sum();
        long activeCount = cInvoices.stream().filter(i -> i.getStatus() != Invoice.Status.PAID).count();

        HBox metricsRow = new HBox(16);
        metricsRow.setPadding(new Insets(10, 14, 10, 14));
        metricsRow.setStyle("-fx-background-color: #182338; -fx-background-radius: 10;");

        metricsRow.getChildren().addAll(
            createMetricCell("Outstanding", CurrencyFormatter.format(totalOutstanding), totalOutstanding > 0 ? "#FF6B6B" : "#4CAF50"),
            createMetricCell("Active Invoices", activeCount + " invoices", "#F6F8FC"),
            createMetricCell("Avg Payment Delay", c.getTypicalDelayDays() + " days", "#FFB84D"),
            createMetricCell("Preferred Channel", c.getPreferredChannel().getDisplay(), "#49E3B4")
        );

        // Bottom Actions Row
        HBox botRow = new HBox(10);
        botRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnNewInv = new Button("+ Invoice");
        btnNewInv.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #F6F8FC; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        btnNewInv.setOnAction(e -> {
            e.consume();
            NavigationManager.getInstance().setSelectedClient(c);
            NavigationManager.getInstance().navigateTo(Screen.CREATE_INVOICE);
        });

        Button btnView = new Button("View Details →");
        btnView.setStyle("-fx-background-color: rgba(73, 227, 180, 0.15); -fx-text-fill: #49E3B4; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;");
        btnView.setOnAction(e -> {
            e.consume();
            NavigationManager.getInstance().setSelectedClient(c);
            NavigationManager.getInstance().navigateTo(Screen.CLIENT_DETAILS);
        });

        botRow.getChildren().addAll(btnNewInv, btnView);

        card.getChildren().addAll(topRow, metricsRow, botRow);
        return card;
    }

    private VBox createMetricCell(String label, String value, String colorHex) {
        VBox box = new VBox(2);
        box.setPrefWidth(120);

        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 10px;");

        Label v = new Label(value);
        v.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        box.getChildren().addAll(l, v);
        return box;
    }

    private String getReliabilityColor(Client.ReliabilityLabel rl) {
        return switch (rl) {
            case USUALLY_ON_TIME -> "#4CAF50";
            case OCCASIONALLY_DELAYED -> "#FFB84D";
            case FREQUENTLY_DELAYED -> "#FF6B6B";
            case NOT_ENOUGH_HISTORY -> "#9BA8BC";
        };
    }

    private String getReliabilityBg(Client.ReliabilityLabel rl) {
        return switch (rl) {
            case USUALLY_ON_TIME -> "rgba(76, 175, 80, 0.15)";
            case OCCASIONALLY_DELAYED -> "rgba(255, 184, 77, 0.15)";
            case FREQUENTLY_DELAYED -> "rgba(255, 107, 107, 0.15)";
            case NOT_ENOUGH_HISTORY -> "rgba(155, 168, 188, 0.15)";
        };
    }

    private void openAddClientDialog() {
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(11, 16, 32, 0.85);");
        modalOverlay.setPrefSize(getWidth(), getHeight());

        VBox card = new VBox(16);
        card.setMaxWidth(440);
        card.setPadding(new Insets(28));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 20; " +
            "-fx-border-color: rgba(73, 227, 180, 0.3); -fx-border-radius: 20; -fx-border-width: 1.5; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 8);"
        );

        Label title = new Label("Add New Client");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 18px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Client / Company Name *");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact Person");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        ComboBox<Client.PreferredChannel> chanCombo = new ComboBox<>();
        chanCombo.getItems().addAll(Client.PreferredChannel.values());
        chanCombo.setValue(Client.PreferredChannel.WHATSAPP);

        TextField delayField = new TextField("5");
        delayField.setPromptText("Typical Delay (Days)");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> ((Pane) getParent()).getChildren().remove(modalOverlay));

        Button btnSave = new Button("Save Client");
        btnSave.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) return;

            Client c = new Client(name, name, emailField.getText().trim(), phoneField.getText().trim());
            c.setPreferredChannel(chanCombo.getValue());
            try {
                c.setTypicalDelayDays(Integer.parseInt(delayField.getText().trim()));
            } catch (Exception ignored) {}

            store.addClient(c);
            ((Pane) getParent()).getChildren().remove(modalOverlay);
            Toast.show((Pane) getParent(), "Client " + c.getName() + " added successfully!", true);
            refreshClients();
        });

        btnRow.getChildren().addAll(btnCancel, btnSave);

        card.getChildren().addAll(
            title,
            createFormField("Client Name *", nameField),
            createFormField("Contact Person", contactField),
            createFormField("Phone Number", phoneField),
            createFormField("Email Address", emailField),
            createFormField("Preferred Channel", chanCombo),
            createFormField("Typical Delay (Days)", delayField),
            btnRow
        );

        modalOverlay.getChildren().add(card);
        ((Pane) getParent()).getChildren().add(modalOverlay);
    }

    private VBox createFormField(String lbl, Control c) {
        VBox b = new VBox(4);
        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-font-weight: bold;");
        c.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 8; -fx-padding: 8;");
        c.setMaxWidth(Double.MAX_VALUE);
        b.getChildren().addAll(l, c);
        return b;
    }
}
