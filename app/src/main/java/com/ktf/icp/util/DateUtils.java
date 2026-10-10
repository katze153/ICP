package com.ktf.icp.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtils {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DateUtils() {
    }

    public static LocalDate parseDate(String value) {
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    public static String formatDate(LocalDate value) {
        return value.format(DATE_FORMATTER);
    }

    public static int daysBetween(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.DAYS.between(start, end);
    }
}
