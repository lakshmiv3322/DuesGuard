package com.duesguard.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    private static final Locale INDIA = new Locale("en", "IN");

    public static String format(double amount) {
        // Indian number format: ₹1,24,500
        NumberFormat nf = NumberFormat.getCurrencyInstance(INDIA);
        String s = nf.format(amount);
        // Replace the currency symbol if needed
        if (!s.startsWith("₹")) {
            s = "₹" + s.replaceAll("^[^0-9]", "");
        }
        return s;
    }

    public static String formatPlain(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(INDIA);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount);
    }

    public static String formatShort(double amount) {
        if (amount >= 100000) {
            return String.format("₹%.1fL", amount / 100000);
        } else if (amount >= 1000) {
            return String.format("₹%.1fK", amount / 1000);
        }
        return format(amount);
    }
}
