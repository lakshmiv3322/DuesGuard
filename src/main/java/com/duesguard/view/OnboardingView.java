package com.duesguard.view;

import com.duesguard.app.AppContext;
import com.duesguard.app.NavigationManager;
import com.duesguard.app.NavigationManager.Screen;
import com.duesguard.model.*;
import com.duesguard.storage.DataStore;
import com.duesguard.view.components.CelebrationOverlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

/**
 * Screen 3: 4-step Onboarding Wizard.
 * Guides new users through Business Profile, Business Type, First Client, and First Invoice.
 * Plays celebration confetti on completion.
 */
public class OnboardingView extends StackPane {
    private int currentStep = 1;
    private final VBox stepContainer;
    private final Label stepIndicator;
    private final ProgressBar progressBar;
    private final Button nextBtn;
    private final Button backBtn;

    // Form data collectors
    private final TextField bizName = new TextField("Lakshmi Creative Studio");
    private final TextField ownerName = new TextField("Lakshmi V.");
    private final TextField bizEmail = new TextField("lakshmi@creative.in");
    private final TextField bizPhone = new TextField("+91 98765 43210");
    private final TextField bizCity = new TextField("Chennai");
    private final ComboBox<Integer> paymentTerms = new ComboBox<>();
    private String selectedBizType = "Agency";

    private final TextField clientName = new TextField("Arun Design Studio");
    private final TextField clientContact = new TextField("Arun Kumar");
    private final TextField clientPhone = new TextField("+91 98765 11111");
    private final TextField clientEmail = new TextField("arun@arundesign.in");
    private final ComboBox<Client.PreferredChannel> clientChannel = new ComboBox<>();
    private final Slider delaySlider = new Slider(0, 30, 9);

    private final TextField invNumber = new TextField("INV-101");
    private final TextField invService = new TextField("Brand Identity Design");
    private final TextField invAmount = new TextField("24500");
    private final DatePicker invDueDate = new DatePicker(LocalDate.now().plusDays(15));
    private File selectedDocFile;
    private final Label attachedDocLabel = new Label("No document attached");

    public OnboardingView() {
        setStyle("-fx-background-color: #0B1020;");

        VBox mainCard = new VBox(24);
        mainCard.setMaxWidth(600);
        mainCard.setMaxHeight(680);
        mainCard.setAlignment(Pos.TOP_CENTER);
        mainCard.setPadding(new Insets(36, 44, 36, 44));
        mainCard.setStyle(
            "-fx-background-color: #121A2B; " +
            "-fx-background-radius: 24; " +
            "-fx-border-color: rgba(255,255,255,0.08); " +
            "-fx-border-radius: 24; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 8);"
        );

        // Header
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        stepIndicator = new Label("STEP 1 OF 4");
        stepIndicator.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label brand = new Label("DUESGUARD ONBOARDING");
        brand.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px; -fx-font-weight: bold;");

        topRow.getChildren().addAll(stepIndicator, topSpacer, brand);

        // Progress bar
        progressBar = new ProgressBar(0.25);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(4);
        progressBar.setStyle("-fx-accent: #49E3B4;");

        // Dynamic step container
        stepContainer = new VBox(16);
        stepContainer.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(stepContainer, Priority.ALWAYS);

        // Bottom nav buttons
        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        backBtn = new Button("Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9BA8BC; -fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;");
        backBtn.setVisible(false);
        backBtn.setOnAction(e -> prevStep());

        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);

        nextBtn = new Button("Continue →");
        nextBtn.setStyle("-fx-background-color: #49E3B4; -fx-text-fill: #0B1020; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 10 24; -fx-cursor: hand;");
        nextBtn.setOnAction(e -> nextStep());

        buttonRow.getChildren().addAll(backBtn, btnSpacer, nextBtn);

        mainCard.getChildren().addAll(topRow, progressBar, stepContainer, buttonRow);
        getChildren().add(mainCard);

        initInputs();
        renderCurrentStep();
    }

    private void initInputs() {
        paymentTerms.getItems().addAll(7, 15, 30, 45, 60);
        paymentTerms.setValue(30);

        clientChannel.getItems().addAll(Client.PreferredChannel.values());
        clientChannel.setValue(Client.PreferredChannel.WHATSAPP);

        delaySlider.setShowTickLabels(true);
        delaySlider.setShowTickMarks(true);
        delaySlider.setMajorTickUnit(10);
        delaySlider.setBlockIncrement(1);
    }

    private void renderCurrentStep() {
        stepContainer.getChildren().clear();
        stepIndicator.setText("STEP " + currentStep + " OF 4");
        progressBar.setProgress(currentStep / 4.0);
        backBtn.setVisible(currentStep > 1);
        nextBtn.setText(currentStep == 4 ? "Complete Setup ✓" : "Continue →");

        switch (currentStep) {
            case 1 -> renderStep1();
            case 2 -> renderStep2();
            case 3 -> renderStep3();
            case 4 -> renderStep4();
        }
    }

    private void renderStep1() {
        Label title = new Label("Your Business Profile");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Tell us about your business so DuesGuard can personalize your follow-ups.");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        grid.add(createField("Business Name", bizName), 0, 0);
        grid.add(createField("Owner Name", ownerName), 1, 0);
        grid.add(createField("Email", bizEmail), 0, 1);
        grid.add(createField("Phone Number", bizPhone), 1, 1);
        grid.add(createField("City", bizCity), 0, 2);

        VBox termsBox = new VBox(4);
        Label tLbl = new Label("Default Payment Terms (Days)");
        tLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        termsBox.getChildren().addAll(tLbl, paymentTerms);
        grid.add(termsBox, 1, 2);

        stepContainer.getChildren().addAll(title, subtitle, grid);
    }

    private void renderStep2() {
        Label title = new Label("What describes your business best?");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("This tailors your payment-readiness checklists and follow-up templates.");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        String[][] types = {
            {"Freelancer", "Design, writing, coding, consulting"},
            {"Tuition centre", "Coaching, education, tutoring classes"},
            {"Agency", "Marketing, creative studio, development"},
            {"Home services", "Repairs, maintenance, catering, events"},
            {"Retail/vendor", "Suppliers, trade, local distributors"},
            {"Other", "Any small service or goods provider"}
        };

        for (int i = 0; i < types.length; i++) {
            String name = types[i][0];
            String desc = types[i][1];
            VBox card = createTypeCard(name, desc);
            grid.add(card, i % 2, i / 2);
        }

        stepContainer.getChildren().addAll(title, subtitle, grid);
    }

    private VBox createTypeCard(String name, String desc) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(14));
        card.setPrefWidth(240);
        card.setStyle(
            "-fx-background-color: " + (selectedBizType.equals(name) ? "rgba(73,227,180,0.12)" : "#182338") + "; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + (selectedBizType.equals(name) ? "#49E3B4" : "rgba(255,255,255,0.08)") + "; " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1.5; " +
            "-fx-cursor: hand;"
        );

        Label title = new Label(name);
        title.setStyle("-fx-text-fill: " + (selectedBizType.equals(name) ? "#49E3B4" : "#F6F8FC") + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label sub = new Label(desc);
        sub.setWrapText(true);
        sub.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 11px;");

        card.getChildren().addAll(title, sub);

        card.setOnMouseClicked(e -> {
            selectedBizType = name;
            renderStep2();
        });

        return card;
    }

    private void renderStep3() {
        Label title = new Label("Add Your First Client");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("DuesGuard tracks client behaviour to predict payment delays and recommend terms.");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        grid.add(createField("Client / Business Name", clientName), 0, 0);
        grid.add(createField("Contact Person", clientContact), 1, 0);
        grid.add(createField("Phone Number", clientPhone), 0, 1);
        grid.add(createField("Email Address", clientEmail), 1, 1);

        VBox chanBox = new VBox(4);
        Label cLbl = new Label("Preferred Channel");
        cLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        chanBox.getChildren().addAll(cLbl, clientChannel);
        grid.add(chanBox, 0, 2);

        VBox sliderBox = new VBox(4);
        Label sLbl = new Label("Typical Payment Delay: 9 days");
        sLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        delaySlider.valueProperty().addListener((obs, o, n) ->
            sLbl.setText("Typical Payment Delay: " + n.intValue() + " days"));
        sliderBox.getChildren().addAll(sLbl, delaySlider);
        grid.add(sliderBox, 1, 2);

        stepContainer.getChildren().addAll(title, subtitle, grid);
    }

    private void renderStep4() {
        Label title = new Label("Add Your First Invoice");
        title.setStyle("-fx-text-fill: #F6F8FC; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Enter an active or overdue invoice to test the Priority Score engine.");
        subtitle.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 13px;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        grid.add(createField("Invoice Number", invNumber), 0, 0);
        grid.add(createField("Service / Deliverable Title", invService), 1, 0);
        grid.add(createField("Total Amount (₹)", invAmount), 0, 1);

        VBox dueBox = new VBox(4);
        Label dLbl = new Label("Due Date");
        dLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        dueBox.getChildren().addAll(dLbl, invDueDate);
        grid.add(dueBox, 1, 1);

        // Document attachment simulation
        VBox docBox = new VBox(6);
        Label docLbl = new Label("Supporting Document (Quotation / Proof)");
        docLbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox docRow = new HBox(10);
        docRow.setAlignment(Pos.CENTER_LEFT);

        Button chooseBtn = new Button("Choose File...");
        chooseBtn.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;");
        chooseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Supporting Document");
            File f = fc.showOpenDialog(getScene().getWindow());
            if (f != null) {
                selectedDocFile = f;
                attachedDocLabel.setText(f.getName());
                attachedDocLabel.setStyle("-fx-text-fill: #49E3B4; -fx-font-size: 12px;");
            }
        });

        attachedDocLabel.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px;");
        docRow.getChildren().addAll(chooseBtn, attachedDocLabel);
        docBox.getChildren().addAll(docLbl, docRow);

        grid.add(docBox, 0, 2, 2, 1);

        stepContainer.getChildren().addAll(title, subtitle, grid);
    }

    private VBox createField(String label, TextField tf) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #9BA8BC; -fx-font-size: 12px; -fx-font-weight: bold;");
        tf.setStyle("-fx-background-color: #182338; -fx-text-fill: #F6F8FC; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9;");
        box.getChildren().addAll(lbl, tf);
        return box;
    }

    private void prevStep() {
        if (currentStep > 1) {
            currentStep--;
            renderCurrentStep();
        }
    }

    private void nextStep() {
        if (currentStep < 4) {
            currentStep++;
            renderCurrentStep();
        } else {
            completeOnboarding();
        }
    }

    private void completeOnboarding() {
        DataStore store = AppContext.getInstance().getDataStore();

        // Save business
        Business biz = new Business(
            bizName.getText(), ownerName.getText(), selectedBizType,
            bizEmail.getText(), bizPhone.getText(), bizCity.getText()
        );
        biz.setDefaultPaymentTerms(paymentTerms.getValue() != null ? paymentTerms.getValue() : 30);
        store.setBusiness(biz);

        // Save client
        Client c = new Client(clientName.getText(), clientName.getText(), clientEmail.getText(), clientPhone.getText());
        c.setPreferredChannel(clientChannel.getValue());
        c.setTypicalDelayDays((int) delaySlider.getValue());
        store.addClient(c);

        // Save invoice
        try {
            double amt = Double.parseDouble(invAmount.getText().trim());
            ServiceInvoice inv = new ServiceInvoice(invNumber.getText().trim(), c.getId(), invService.getText().trim());
            inv.getLineItems().add(new LineItem(invService.getText().trim(), 1, amt));
            inv.setDueDate(invDueDate.getValue());
            inv.setPaymentTerms(biz.getDefaultPaymentTerms());
            inv.updateStatusAutomatically();

            if (selectedDocFile != null) {
                EvidenceDocument doc = new EvidenceDocument(
                    inv.getId(), EvidenceDocument.DocType.QUOTATION,
                    selectedDocFile.getName(), selectedDocFile.getAbsolutePath()
                );
                store.addDocument(doc);
                inv.setReadinessScore(40.0);
            }
            store.addInvoice(inv);
        } catch (Exception ignored) {}

        // Mark user as onboarded
        AppUser u = store.getCurrentUser();
        if (u != null) {
            u.setOnboardingCompleted(true);
            store.updateUser(u);
        }

        // Show celebration confetti
        CelebrationOverlay.showOnboardingConfetti(this);

        // Transition to Dashboard
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1600));
        pt.setOnFinished(e -> NavigationManager.getInstance().navigateTo(Screen.DASHBOARD));
        pt.play();
    }
}
