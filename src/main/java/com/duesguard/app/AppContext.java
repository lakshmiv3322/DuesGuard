package com.duesguard.app;

import com.duesguard.service.*;
import com.duesguard.storage.DataStore;

/**
 * Global application context holding singleton services and state.
 */
public class AppContext {
    private static AppContext instance;

    private final DataStore dataStore;
    private final PriorityScoreService priorityScoreService;
    private final ReadinessScoreService readinessScoreService;
    private final ReminderMessageService reminderMessageService;
    private final CashFlowService cashFlowService;

    // UI state flags
    private boolean darkTheme = true;
    private boolean reducedMotion = false;
    private boolean demoModeActive = false;

    private AppContext() {
        this.dataStore = DataStore.getInstance();
        this.priorityScoreService = new PriorityScoreService();
        this.readinessScoreService = new ReadinessScoreService();
        this.reminderMessageService = new ReminderMessageService();
        this.cashFlowService = new CashFlowService();
    }

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public DataStore getDataStore() { return dataStore; }
    public PriorityScoreService getPriorityScoreService() { return priorityScoreService; }
    public ReadinessScoreService getReadinessScoreService() { return readinessScoreService; }
    public ReminderMessageService getReminderMessageService() { return reminderMessageService; }
    public CashFlowService getCashFlowService() { return cashFlowService; }

    public boolean isDarkTheme() { return darkTheme; }
    public void setDarkTheme(boolean darkTheme) { this.darkTheme = darkTheme; }

    public boolean isReducedMotion() { return reducedMotion; }
    public void setReducedMotion(boolean reducedMotion) { this.reducedMotion = reducedMotion; }

    public boolean isDemoModeActive() { return demoModeActive; }
    public void setDemoModeActive(boolean demoModeActive) { this.demoModeActive = demoModeActive; }
}
