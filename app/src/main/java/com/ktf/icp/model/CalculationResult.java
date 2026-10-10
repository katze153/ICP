package com.ktf.icp.model;

public class CalculationResult {
    private final double money;
    private final int days;
    private final double zins;
    private final int methode;
    private final double finalAmount;
    private final String startDate;
    private final String endDate;

    public CalculationResult(double money, int days, double zins, int methode, double finalAmount, String startDate, String endDate) {
        this.money = money;
        this.days = days;
        this.zins = zins;
        this.methode = methode;
        this.finalAmount = finalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public double getMoney() {
        return money;
    }

    public int getDays() {
        return days;
    }

    public double getZins() {
        return zins;
    }

    public int getMethode() {
        return methode;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public double getInterest() {
        return finalAmount - money;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
