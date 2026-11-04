package com.duesguard;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.service.DemoDataService;
import com.duesguard.storage.DataStore;
import com.duesguard.view.*;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewInstantiationTest {

    @BeforeAll
    public static void initJavaFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Already started
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Seed demo data
        DataStore store = AppContext.getInstance().getDataStore();
        DemoDataService.seedDemoData(store);
    }

    @Test
    public void testPriorityQueueViewInstantiation() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                // Initialize NavigationManager with dummy stage and containers
                Stage stage = new Stage();
                StackPane root = new StackPane();
                BorderPane layout = new BorderPane();
                StackPane contentArea = new StackPane();
                NavigationManager nav = NavigationManager.getInstance();
                nav.init(stage, root);
                nav.setMainAppLayout(layout, contentArea);

                // Now test PriorityQueueView
                PriorityQueueView pqView = new PriorityQueueView();
            } catch (Throwable t) {
                t.printStackTrace();
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
        assertNull(error.get(), "PriorityQueueView instantiation threw exception: " + (error.get() != null ? error.get().getMessage() : ""));
    }

    @Test
    public void testAllScreensInstantiation() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                new DashboardView();
                new PriorityQueueView();
                new InvoicesView();
                new ClientsView();
                new PaymentsView();
                new CashFlowView();
                new ReportsView();
                new DocumentsView();
                new SettingsView();
                new HelpView();
                new JuryDemoView();
            } catch (Throwable t) {
                t.printStackTrace();
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
        assertNull(error.get(), "Screen instantiation threw exception: " + (error.get() != null ? error.get().getMessage() : ""));
    }
}
