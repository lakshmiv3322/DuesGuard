package com.duesguard.service;

import com.duesguard.model.Client;
import com.duesguard.model.Invoice;
import com.duesguard.model.Reminder;
import com.duesguard.util.CurrencyFormatter;
import com.duesguard.util.DateUtil;

public class ReminderMessageService {

    public String generateMessage(Invoice invoice, Client client, Reminder.Tone tone, Reminder.EscalationLevel level) {
        String clientName = client != null ? client.getName().split(" ")[0] : "there";
        String amount = CurrencyFormatter.format(invoice.getBalance());
        String invoiceNum = invoice.getInvoiceNumber();
        String dueDate = DateUtil.formatDisplay(invoice.getDueDate());
        long daysOverdue = invoice.getDaysOverdue();
        String service = invoice.getServiceTitle();

        return switch (tone) {
            case FRIENDLY -> generateFriendly(clientName, amount, invoiceNum, dueDate, daysOverdue, service, level);
            case PROFESSIONAL -> generateProfessional(clientName, amount, invoiceNum, dueDate, daysOverdue, service, level);
            case FIRM -> generateFirm(clientName, amount, invoiceNum, dueDate, daysOverdue, service, level);
        };
    }

    private String generateFriendly(String name, String amount, String invNum, String dueDate, long daysOverdue, String service, Reminder.EscalationLevel level) {
        if (level == Reminder.EscalationLevel.FIRST) {
            return "Hi " + name + ", hope you are doing well! This is a gentle reminder regarding " + invNum +
                " for " + amount + " related to " + service + ", which was due on " + dueDate +
                ". Please let me know if you need any additional documents from our side. Thank you! \uD83D\uDE0A";
        } else {
            return "Hi " + name + ", just checking in on " + invNum + " for " + amount +
                ". It's been " + daysOverdue + " days since the due date. Please share an update when you can. Thank you!";
        }
    }

    private String generateProfessional(String name, String amount, String invNum, String dueDate, long daysOverdue, String service, Reminder.EscalationLevel level) {
        return "Dear " + name + ",\n\nI am writing to follow up on " + invNum + " for " + amount +
            " regarding " + service + ", which was due on " + dueDate + "." +
            (daysOverdue > 0 ? " This invoice is now " + daysOverdue + " days past due." : "") +
            "\n\nI would appreciate your prompt attention to this matter. Please feel free to contact me if you have any questions or require any supporting documents." +
            "\n\nThank you for your time and cooperation.";
    }

    private String generateFirm(String name, String amount, String invNum, String dueDate, long daysOverdue, String service, Reminder.EscalationLevel level) {
        return "Dear " + name + ",\n\nThis is a follow-up regarding overdue " + invNum + " for " + amount +
            " (" + service + "), due on " + dueDate + "." +
            (daysOverdue > 0 ? " Payment is now " + daysOverdue + " days overdue." : "") +
            "\n\nWe kindly request that you arrange for payment at the earliest convenience. If there is an issue with the invoice or services, please let us know immediately so we can resolve it." +
            "\n\nIf we do not hear from you within 3 business days, we may need to escalate this matter internally." +
            "\n\nThank you.";
    }
}
