package com.example.tool.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * General-purpose date utilities for compliance due-date calculations.
 * All methods are stateless and thread-safe.
 */
public final class DateUtil {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // Prevent instantiation
    private DateUtil() {}

    /**
     * Returns {@code true} if the given due date is strictly before today.
     *
     * @param dueDate the compliance due date
     * @return {@code true} when overdue
     */
    public static boolean isOverdue(LocalDate dueDate) {
        if (dueDate == null) return false;
        return dueDate.isBefore(LocalDate.now());
    }

    /**
     * Returns the number of days until (positive) or since (negative) the due date.
     * Returns 0 if {@code dueDate} is today.
     *
     * @param dueDate the compliance due date
     * @return signed day count
     */
    public static long daysUntilDue(LocalDate dueDate) {
        if (dueDate == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Formats a {@link LocalDate} as {@code "dd MMM yyyy"} (e.g. "31 Dec 2025").
     * Returns {@code "N/A"} for {@code null} input.
     *
     * @param date the date to format
     * @return formatted string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DISPLAY_FORMAT);
    }

    /**
     * Returns {@code true} if the due date falls within the next {@code days} days
     * (inclusive of today).
     *
     * @param dueDate the due date to check
     * @param days    the look-ahead window in days
     * @return {@code true} when due within the window
     */
    public static boolean isDueWithin(LocalDate dueDate, int days) {
        if (dueDate == null) return false;
        LocalDate today = LocalDate.now();
        return !dueDate.isBefore(today) && !dueDate.isAfter(today.plusDays(days));
    }
}
