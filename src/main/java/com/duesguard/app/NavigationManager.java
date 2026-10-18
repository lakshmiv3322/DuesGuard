package com.duesguard.app;

import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.view.*;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Central navigation manager for DuesGuard.
 * Manages view switching within the main application window.
 */
public class NavigationManager {
    public enum Screen {
        SPLASH,
        LOGIN,
        ONBOARDING,
        DASHBOARD,
        PRIORITY_QUEUE,
        INVOICES,
        CREATE_INVOICE,
        INVOICE_DETAILS,
        CLIENTS,
        CLIENT_DETAILS,
        PAYMENTS,
        CASH_FLOW,
        REPORTS,
        DOCUMENTS,
        REMINDER_COMPOSER,
        SETTINGS,
        HELP,
        JURY_DEMO
    }

    private static NavigationManager instance;
    private Stage primaryStage;
    private StackPane rootContainer;
    private BorderPane mainAppLayout;
    private StackPane contentArea;

    // View registry
    private final Map<Screen, Node> views = new HashMap<>();
    private Screen currentScreen;

    // Parameter passing
    private Invoice selectedInvoice;
    private Client selectedClient;

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void init(Stage stage, StackPane root) {
        this.primaryStage = stage;
        this.rootContainer = root;
    }

    public void setMainAppLayout(BorderPane layout, StackPane content) {
        this.mainAppLayout = layout;
        this.contentArea = content;
    }

    public Stage getPrimaryStage() { return primaryStage; }
    public StackPane getRootContainer() { return rootContainer; }
    public StackPane getContentArea() { return contentArea; }
    public Screen getCurrentScreen() { return currentScreen; }

    public Invoice getSelectedInvoice() { return selectedInvoice; }
    public void setSelectedInvoice(Invoice invoice) { this.selectedInvoice = invoice; }

    public Client getSelectedClient() { return selectedClient; }
    public void setSelectedClient(Client client) { this.selectedClient = client; }

    /**
     * Navigate to a screen with transition.
     */
    public void navigateTo(Screen screen) {
        this.currentScreen = screen;

        // Full-screen views (Splash, Login, Onboarding) replace the root
        if (screen == Screen.SPLASH || screen == Screen.LOGIN || screen == Screen.ONBOARDING) {
            Node fullView = createView(screen);
            rootContainer.getChildren().setAll(fullView);
            animateViewEntrance(fullView);
            return;
        }

        // Inside main app shell:
        if (!rootContainer.getChildren().contains(mainAppLayout)) {
            rootContainer.getChildren().setAll(mainAppLayout);
        }

        Node view = createView(screen);
        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
            animateViewEntrance(view);
        }

        // Update sidebar active button
        if (mainAppLayout != null && mainAppLayout.getLeft() instanceof com.duesguard.view.components.Sidebar sidebar) {
            sidebar.setActiveScreen(screen);
        }
    }

    private Node createView(Screen screen) {
        return switch (screen) {
            case SPLASH -> new SplashView();
            case LOGIN -> new LoginView();
            case ONBOARDING -> new OnboardingView();
            case DASHBOARD -> new DashboardView();
            case PRIORITY_QUEUE -> new PriorityQueueView();
            case INVOICES -> new InvoicesView();
            case CREATE_INVOICE -> new CreateInvoiceView(selectedInvoice);
            case INVOICE_DETAILS -> new InvoiceDetailView(selectedInvoice);
            case CLIENTS -> new ClientsView();
            case CLIENT_DETAILS -> new ClientDetailView(selectedClient);
            case PAYMENTS -> new PaymentsView();
            case CASH_FLOW -> new CashFlowView();
            case REPORTS -> new ReportsView();
            case DOCUMENTS -> new DocumentsView();
            case REMINDER_COMPOSER -> new ReminderComposerView(selectedInvoice);
            case SETTINGS -> new SettingsView();
            case HELP -> new HelpView();
            case JURY_DEMO -> new JuryDemoView();
        };
    }

    private void animateViewEntrance(Node node) {
        if (AppContext.getInstance().isReducedMotion()) return;
        node.setOpacity(0);
        node.setTranslateY(8);

        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(200), node);
        tt.setFromY(8);
        tt.setToY(0);

        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
        pt.play();
    }
}
