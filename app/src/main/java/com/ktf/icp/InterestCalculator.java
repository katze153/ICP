package com.ktf.icp;

import com.ktf.icp.util.DateUtils;

import java.time.LocalDate;

public class InterestCalculator {
    public static final int METHOD_DAILY = 1;
    public static final int METHOD_YEARLY = 2;

    public double calculateFinalAmount(double money, double zinsPercent, int methode, int days) {
        return switch (methode) {
            case METHOD_DAILY -> money * Math.pow(1 + zinsPercent / 100, days);
            case METHOD_YEARLY -> money * Math.pow(1 + zinsPercent / 100, days / 365.0);
            default -> money;
        };
    }

    public double calculateByDayCount(double money, double zinsPercent, int methode, int days) {
        return calculateFinalAmount(money, zinsPercent, methode, days);
    }

    public double calculateByDateRange(double money, double zinsPercent, int methode, LocalDate startDate, LocalDate endDate) {
        int days = DateUtils.daysBetween(startDate, endDate);
        return calculateFinalAmount(money, zinsPercent, methode, days);
    }
}
