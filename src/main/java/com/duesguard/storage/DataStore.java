package com.duesguard.storage;

import com.duesguard.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central data store. Persists all data as JSON files in the app data directory.
 * Acts as an in-memory cache + file persistence layer.
 */
public class DataStore {
    private static DataStore instance;

    // In-memory collections
    private final List<AppUser> users = new ArrayList<>();
    private final List<Client> clients = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();
    private final List<Reminder> reminders = new ArrayList<>();
    private final List<EvidenceDocument> documents = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final List<FollowUpLog> followUpLogs = new ArrayList<>();
    private Business business;
    private AppUser currentUser;

    // File paths
    private final Path dataDir;
    private final ObjectMapper mapper;

    private DataStore() {
        String appData = System.getProperty("user.home") + File.separator + ".duesguard";
        dataDir = Paths.get(appData);
        try {
            Files.createDirectories(dataDir);
        } catch (Exception e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // ---- Business ----
    public Business getBusiness() { return business; }
    public void setBusiness(Business b) {
        this.business = b;
        saveBusiness();
    }

    // ---- Users ----
    public List<AppUser> getUsers() { return Collections.unmodifiableList(users); }
    public Optional<AppUser> findUserByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }
    public Optional<AppUser> findUserByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }
    public void addUser(AppUser u) {
        users.removeIf(existing -> existing.getId().equals(u.getId()));
        users.add(u);
        saveUsers();
    }
    public void updateUser(AppUser u) { addUser(u); }

    // ---- Current user ----
    public AppUser getCurrentUser() { return currentUser; }
    public void setCurrentUser(AppUser u) { this.currentUser = u; }

    // ---- Clients ----
    public List<Client> getClients() { return Collections.unmodifiableList(clients); }
    public Optional<Client> findClientById(String id) {
        return clients.stream().filter(c -> c.getId().equals(id)).findFirst();
    }
    public void addClient(Client c) {
        clients.removeIf(existing -> existing.getId().equals(c.getId()));
        clients.add(c);
        saveClients();
    }
    public void updateClient(Client c) { addClient(c); }
    public void deleteClient(String id) {
        clients.removeIf(c -> c.getId().equals(id));
        saveClients();
    }

    // ---- Invoices ----
    public List<Invoice> getInvoices() { return Collections.unmodifiableList(invoices); }
    public List<Invoice> getActiveInvoices() {
        return invoices.stream().filter(i -> !i.isArchived()).collect(Collectors.toList());
    }
    public Optional<Invoice> findInvoiceById(String id) {
        return invoices.stream().filter(i -> i.getId().equals(id)).findFirst();
    }
    public Optional<Invoice> findByInvoiceNumber(String num) {
        return invoices.stream().filter(i -> num.equals(i.getInvoiceNumber())).findFirst();
    }
    public boolean isInvoiceNumberUnique(String num, String excludeId) {
        return invoices.stream()
            .filter(i -> !i.getId().equals(excludeId))
            .noneMatch(i -> num.equals(i.getInvoiceNumber()));
    }
    public void addInvoice(Invoice inv) {
        invoices.removeIf(existing -> existing.getId().equals(inv.getId()));
        invoices.add(inv);
        saveInvoices();
    }
    public void updateInvoice(Invoice inv) { addInvoice(inv); }
    public void deleteInvoice(String id) {
        invoices.removeIf(i -> i.getId().equals(id));
        saveInvoices();
    }

    // ---- Payments ----
    public List<Payment> getPayments() { return Collections.unmodifiableList(payments); }
    public List<Payment> getPaymentsForInvoice(String invoiceId) {
        return payments.stream().filter(p -> invoiceId.equals(p.getInvoiceId())).collect(Collectors.toList());
    }
    public void addPayment(Payment p) {
        payments.removeIf(existing -> existing.getId().equals(p.getId()));
        payments.add(p);
        savePayments();
    }

    // ---- Reminders ----
    public List<Reminder> getReminders() { return Collections.unmodifiableList(reminders); }
    public List<Reminder> getRemindersForInvoice(String invoiceId) {
        return reminders.stream().filter(r -> invoiceId.equals(r.getInvoiceId())).collect(Collectors.toList());
    }
    public void addReminder(Reminder r) {
        reminders.add(r);
        saveReminders();
    }

    // ---- Documents ----
    public List<EvidenceDocument> getDocuments() { return Collections.unmodifiableList(documents); }
    public List<EvidenceDocument> getDocumentsForInvoice(String invoiceId) {
        return documents.stream().filter(d -> invoiceId.equals(d.getInvoiceId())).collect(Collectors.toList());
    }
    public void addDocument(EvidenceDocument d) {
        documents.removeIf(existing -> existing.getId().equals(d.getId()));
        documents.add(d);
        saveDocuments();
    }
    public void removeDocument(String id) {
        documents.removeIf(d -> d.getId().equals(id));
        saveDocuments();
    }

    // ---- Expenses ----
    public List<Expense> getExpenses() { return Collections.unmodifiableList(expenses); }
    public void addExpense(Expense e) {
        expenses.removeIf(existing -> existing.getId().equals(e.getId()));
        expenses.add(e);
        saveExpenses();
    }
    public void updateExpense(Expense e) { addExpense(e); }
    public void deleteExpense(String id) {
        expenses.removeIf(e -> e.getId().equals(id));
        saveExpenses();
    }

    // ---- Follow-up logs ----
    public List<FollowUpLog> getFollowUpLogs() { return Collections.unmodifiableList(followUpLogs); }
    public List<FollowUpLog> getFollowUpLogsForInvoice(String invoiceId) {
        return followUpLogs.stream().filter(f -> invoiceId.equals(f.getInvoiceId())).collect(Collectors.toList());
    }
    public void addFollowUpLog(FollowUpLog f) {
        followUpLogs.add(f);
        saveFollowUpLogs();
    }

    // ---- Clear all ----
    public void clearAll() {
        users.clear();
        clients.clear();
        invoices.clear();
        payments.clear();
        reminders.clear();
        documents.clear();
        expenses.clear();
        followUpLogs.clear();
        business = null;
        currentUser = null;
    }

    // ---- Persistence ----
    public void saveAll() {
        saveBusiness();
        saveUsers();
        saveClients();
        saveInvoices();
        savePayments();
        saveReminders();
        saveDocuments();
        saveExpenses();
        saveFollowUpLogs();
    }

    public void loadAll() {
        loadBusiness();
        loadUsers();
        loadClients();
        loadInvoices();
        loadPayments();
        loadReminders();
        loadDocuments();
        loadExpenses();
        loadFollowUpLogs();
    }

    private void saveBusiness() {
        if (business == null) return;
        writeJson("business.json", business);
    }

    private void loadBusiness() {
        business = readJson("business.json", Business.class);
    }

    private void saveUsers() {
        writeJsonList("users.json", users, AppUser[].class);
    }

    private void loadUsers() {
        List<AppUser> loaded = readJsonList("users.json", AppUser[].class);
        users.clear();
        users.addAll(loaded);
    }

    private void saveClients() {
        writeJsonList("clients.json", clients, Client[].class);
    }

    private void loadClients() {
        List<Client> loaded = readJsonList("clients.json", Client[].class);
        clients.clear();
        clients.addAll(loaded);
    }

    private void saveInvoices() {
        // Save as mixed type list using type info
        try {
            List<Map<String, Object>> withType = new ArrayList<>();
            for (Invoice inv : invoices) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = mapper.convertValue(inv, Map.class);
                map.put("_type", inv instanceof ServiceInvoice ? "SERVICE" : "PRODUCT");
                withType.add(map);
            }
            Path file = dataDir.resolve("invoices.json");
            mapper.writeValue(file.toFile(), withType);
        } catch (Exception e) {
            System.err.println("Error saving invoices: " + e.getMessage());
        }
    }

    private void loadInvoices() {
        try {
            Path file = dataDir.resolve("invoices.json");
            if (!Files.exists(file)) return;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = mapper.readValue(file.toFile(), List.class);
            invoices.clear();
            for (Map<String, Object> map : list) {
                String type = (String) map.getOrDefault("_type", "SERVICE");
                map.remove("_type");
                Invoice inv;
                if ("PRODUCT".equals(type)) {
                    inv = mapper.convertValue(map, ProductInvoice.class);
                } else {
                    inv = mapper.convertValue(map, ServiceInvoice.class);
                }
                invoices.add(inv);
            }
        } catch (Exception e) {
            System.err.println("Error loading invoices: " + e.getMessage());
        }
    }

    private void savePayments() {
        writeJsonList("payments.json", payments, Payment[].class);
    }

    private void loadPayments() {
        List<Payment> loaded = readJsonList("payments.json", Payment[].class);
        payments.clear();
        payments.addAll(loaded);
    }

    private void saveReminders() {
        writeJsonList("reminders.json", reminders, Reminder[].class);
    }

    private void loadReminders() {
        List<Reminder> loaded = readJsonList("reminders.json", Reminder[].class);
        reminders.clear();
        reminders.addAll(loaded);
    }

    private void saveDocuments() {
        writeJsonList("documents.json", documents, EvidenceDocument[].class);
    }

    private void loadDocuments() {
        List<EvidenceDocument> loaded = readJsonList("documents.json", EvidenceDocument[].class);
        documents.clear();
        documents.addAll(loaded);
    }

    private void saveExpenses() {
        writeJsonList("expenses.json", expenses, Expense[].class);
    }

    private void loadExpenses() {
        List<Expense> loaded = readJsonList("expenses.json", Expense[].class);
        expenses.clear();
        expenses.addAll(loaded);
    }

    private void saveFollowUpLogs() {
        writeJsonList("followuplogs.json", followUpLogs, FollowUpLog[].class);
    }

    private void loadFollowUpLogs() {
        List<FollowUpLog> loaded = readJsonList("followuplogs.json", FollowUpLog[].class);
        followUpLogs.clear();
        followUpLogs.addAll(loaded);
    }

    private <T> void writeJson(String filename, T obj) {
        try {
            Path file = dataDir.resolve(filename);
            mapper.writeValue(file.toFile(), obj);
        } catch (Exception e) {
            System.err.println("Error saving " + filename + ": " + e.getMessage());
        }
    }

    private <T> T readJson(String filename, Class<T> clazz) {
        try {
            Path file = dataDir.resolve(filename);
            if (!Files.exists(file)) return null;
            return mapper.readValue(file.toFile(), clazz);
        } catch (Exception e) {
            System.err.println("Error loading " + filename + ": " + e.getMessage());
            return null;
        }
    }

    private <T> void writeJsonList(String filename, List<?> list, Class<T[]> arrayClass) {
        try {
            Path file = dataDir.resolve(filename);
            mapper.writeValue(file.toFile(), list);
        } catch (Exception e) {
            System.err.println("Error saving " + filename + ": " + e.getMessage());
        }
    }

    private <T> List<T> readJsonList(String filename, Class<T[]> arrayClass) {
        try {
            Path file = dataDir.resolve(filename);
            if (!Files.exists(file)) return new ArrayList<>();
            T[] arr = mapper.readValue(file.toFile(), arrayClass);
            return new ArrayList<>(Arrays.asList(arr));
        } catch (Exception e) {
            System.err.println("Error loading " + filename + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
