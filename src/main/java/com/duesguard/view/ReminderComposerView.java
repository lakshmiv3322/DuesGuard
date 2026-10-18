package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.*;
import com.duesguard.service.PriorityScoreService;
import com.duesguard.service.ReminderMessageService;
import com.duesguard.storage.DataStore;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;
import com.duesguard.view.components.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.List;

/**
 * Screen 16: Follow-Up & Reminder Composer.
 * Supports Tone Selection (Friendly, Professional, Firm),
 * Channel Selection (WhatsApp, Email, SMS), Escalation Levels (1st, 2nd, Final),
 * dynamic variable interpolation, and direct clipboard copying.
 */
public class ReminderComposerView extends ScrollPane {
    private final DataStore store;
    private final PriorityScoreService scoreService;
    private final ReminderMessageService msgService;
    private final Invoice invoice;
    private final Client client;

    // Controls
    private Reminder.Tone selectedTone = Reminder.Tone.FRIENDLY;
    private Reminder.Channel selectedChannel = Reminder.Channel.WHATSAPP;
    private Reminder.EscalationLevel selectedLevel = Reminder.EscalationLevel.FIRST;
    private final TextArea messageArea = new TextArea();

    public ReminderComposerView(Invoice inv) {
        this.store = AppContext.getInstance().getDataStore();
        this.scoreService = AppContext.getInstance().getPriorityScoreService();
        this.msgService = AppContext.getInstance().getReminderMessageService();

        if (inv != null) {
            this.invoice = inv;
        } else {
            List<Invoice> overdue = store.getActiveInvoices().stream().filter(i -> i.getDaysOverdue() > 0).toList();
            this.invoice = !overdue.isEmpty() ? overdue.get(0) : (!store.getActiveInvoices().isEmpty() ? store.getActiveInvoices().get(0) : null);
        }

        this.client = (this.invoice != null) ? store.findClientById(this.invoice.getClientId()).orElse(null) : null;

        setFitToWidth(true);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(22);
        content.setPadding(new Insets(28, 36, 48, 36));
        content.setMaxWidth(1440);

        if (this.invoice == null) {
            Label noInv = new Label("No invoice selected for reminder.");
            noInv.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 16px;");
            content.getChildren().add(noInv);
        } else {
            // Header
            content.getChildren().add(buildHeader());

            // Invoice context card
            content.getChildren().add(buildContextCard());

            // Two-column composer layout
            HBox twoCol = new HBox(24);
            twoCol.setAlignment(Pos.TOP_LEFT);

            // Left (40%): Selectors (Escalation, Tone, Channel)
            VBox leftSelectors = buildSelectorsColumn();
            leftSelectors.setPrefWidth(440);
            leftSelectors.setMinWidth(380);

            // Right (60%): Message Preview & Actions
            VBox rightMessage = buildMessageColumn();
            HBox.setHgrow(rightMessage, Priority.ALWAYS);

            twoCol.getChildren().addAll(leftSelectors, rightMessage);
            content.getChildren().add(twoCol);
        }

        setContent(content);
        updateGeneratedMessage();
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnBack = new Button("← Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0;");
        btnBack.setOnAction(e -> NavigationManager.getInstance().navigateTo(Screen.PRIORITY_QUEUE));

        VBox titleCol = new VBox(2);
        Label title = new Label("Prepare Your Follow-Up");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Generate respectful, context-aware payment reminders with dynamic variables");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");
        titleCol.getChildren().addAll(title, subtitle);

        header.getChildren().addAll(btnBack, titleCol);
        return header;
    }

    private HBox buildContextCard() {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 24, 18, 24));
        card.setStyle(
            "-fx-background-color: #121A2B; -fx-background-radius: 18; " +
            "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 18; -fx-border-width: 1;"
        );

        double score = scoreService.calculate(invoice, client);

        card.getChildren().addAll(
            createContextItem("Client", client != null ? client.getName() : "Unknown", "#F6F8FC"),
            createContextItem("Invoice Number", invoice.getInvoiceNumber(), "#7C83FF"),
            createContextItem("Balance Due", CurrencyFormatter.format(invoice.getBalance()), "#FF6B6B"),
            createContextItem("Overdue Status", invoice.getDaysOverdue() > 0 ? invoice.getDaysOverdue() + " days overdue" : "Due in " + invoice.getDaysUntilDue() + "d", invoice.getDaysOverdue() > 0 ? "#FF6B6B" : "#49E3B4"),
            createContextItem("Priority Score", String.format("%.0f/100", score), scoreService.getPriorityColor(score)),
            createContextItem("Readiness", String.format("%.0f%%", invoice.getReadinessScore()), invoice.getReadinessScore() >= 80 ? "#4CAF50" : "#FFB84D")
        );

        return card;
    }

    private VBox createContextItem(String label, String value, String colorHex) {
        VBox b = new VBox(3);
        b.setPrefWidth(160);
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        b.getChildren().addAll(l, v);
        return b;
    }

    private VBox buildSelectorsColumn() {
        VBox col = new VBox(18);
        col.setPadding(new Insets(24));
        col.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        // 1. Escalation Level
        Label escTitle = new Label("1. Escalation Stage");
        escTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox escBox = new VBox(6);
        ToggleGroup escGroup = new ToggleGroup();
        escBox.getChildren().addAll(
            createEscalationRadio("First Follow-Up (Gentle reminder)", Reminder.EscalationLevel.FIRST, escGroup, true),
            createEscalationRadio("Second Follow-Up (Direct inquiry)", Reminder.EscalationLevel.SECOND, escGroup, false),
            createEscalationRadio("Final Follow-Up (Firm deadline)", Reminder.EscalationLevel.FINAL, escGroup, false)
        );

        // 2. Tone Selector
        Label toneTitle = new Label("2. Tone & Warmth");
        toneTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox toneButtons = new HBox(8);
        Button btnFriendly = createToneBtn("Friendly 😊", Reminder.Tone.FRIENDLY, true);
        Button btnProf = createToneBtn("Professional 💼", Reminder.Tone.PROFESSIONAL, false);
        Button btnFirm = createToneBtn("Firm ⚖", Reminder.Tone.FIRM, false);
        toneButtons.getChildren().addAll(btnFriendly, btnProf, btnFirm);

        // 3. Preferred Communication Channel
        Label chanTitle = new Label("3. Delivery Channel");
        chanTitle.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox chanButtons = new HBox(8);
        Button btnWa = createChanBtn("WhatsApp", Reminder.Channel.WHATSAPP, true);
        Button btnEmail = createChanBtn("Email", Reminder.Channel.EMAIL, false);
        Button btnSms = createChanBtn("SMS", Reminder.Channel.SMS, false);
        chanButtons.getChildren().addAll(btnWa, btnEmail, btnSms);

        col.getChildren().addAll(escTitle, escBox, new Separator(), toneTitle, toneButtons, new Separator(), chanTitle, chanButtons);
        return col;
    }

    private RadioButton createEscalationRadio(String text, Reminder.EscalationLevel level, ToggleGroup tg, boolean sel) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(tg);
        rb.setSelected(sel);
        rb.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 13px; -fx-cursor: hand;");
        rb.setOnAction(e -> {
            selectedLevel = level;
            updateGeneratedMessage();
        });
        return rb;
    }

    private Button createToneBtn(String text, Reminder.Tone tone, boolean active) {
        Button b = new Button(text);
        b.setStyle(active
            ? "-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;"
            : "-fx-background-color: #182338; -fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
        b.setOnAction(e -> {
            selectedTone = tone;
            updateGeneratedMessage();
        });
        return b;
    }

    private Button createChanBtn(String text, Reminder.Channel chan, boolean active) {
        Button b = new Button(text);
        b.setStyle(active
            ? "-fx-background-color: #7C83FF; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;"
            : "-fx-background-color: #182338; -fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
        b.setOnAction(e -> {
            selectedChannel = chan;
            updateGeneratedMessage();
        });
        return b;
    }

    private VBox buildMessageColumn() {
        VBox col = new VBox(16);
        col.setPadding(new Insets(24));
        col.setStyle("-fx-background-color: #121A2B; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 20; -fx-border-width: 1;");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Generated Follow-Up Message");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label charCount = new Label();
        charCount.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");

        top.getChildren().addAll(title, sp, charCount);

        messageArea.setPrefRowCount(9);
        messageArea.setWrapText(true);
        messageArea.setStyle(
            "-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-background-radius: 12; " +
            "-fx-border-color: rgba(73, 227, 180, 0.25); -fx-border-radius: 12; -fx-border-width: 1; -fx-font-size: 13px; -fx-padding: 12;"
        );
        messageArea.textProperty().addListener((o, ov, nv) -> charCount.setText(nv.length() + " chars"));

        // Action Buttons Row
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnSchedule = new Button("⏰ Schedule for Tomorrow");
        btnSchedule.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnSchedule.setOnAction(e -> {
            Reminder r = new Reminder();
            r.setInvoiceId(invoice.getId());
            r.setClientId(invoice.getClientId());
            r.setScheduledDate(LocalDate.now().plusDays(1));
            r.setMessage(messageArea.getText());
            store.addReminder(r);
            Toast.show((Pane) getScene().getRoot(), "Reminder scheduled for tomorrow!", true);
        });

        Button btnCopy = new Button("📋 Copy Message");
        btnCopy.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #F6F8FC; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnCopy.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(messageArea.getText());
            Clipboard.getSystemClipboard().setContent(cc);
            Toast.show((Pane) getScene().getRoot(), "Message copied to clipboard!", true);
        });

        Button btnMarkSent = new Button("✓ Mark as Sent");
        btnMarkSent.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        btnMarkSent.setOnAction(e -> {
            FollowUpLog log = new FollowUpLog(invoice.getId(), selectedChannel.name(), "Sent " + selectedTone.name() + " reminder");
            store.addFollowUpLog(log);
            Toast.show((Pane) getScene().getRoot(), "Follow-up logged successfully!", true);
        });

        btnRow.getChildren().addAll(btnSchedule, btnCopy, btnMarkSent);

        col.getChildren().addAll(top, messageArea, btnRow);
        return col;
    }

    private void updateGeneratedMessage() {
        if (invoice == null) return;
        String msg = msgService.generateMessage(invoice, client, selectedTone, selectedLevel);
        messageArea.setText(msg);
    }
}
