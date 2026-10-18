package com.duesguard.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter SHORT_FORMAT = DateTimeFormatter.ofPattern("dd MMM");
    public static final DateTimeFormatter STORAGE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public static String formatDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMAT);
    }

    public static String formatShort(LocalDate date) {
        if (date == null) return "";
        return date.format(SHORT_FORMAT);
    }

    public static LocalDate parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), STORAGE_FORMAT);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr.trim(), DISPLAY_FORMAT);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public static String overdueDaysLabel(long days) {
        if (days <= 0) return "On time";
        if (days == 1) return "1 day overdue";
        return days + " days overdue";
    }

    public static String dueInLabel(long days) {
        if (days == 0) return "Due today";
        if (days == 1) return "Due tomorrow";
        return "Due in " + days + " days";
    }
}
