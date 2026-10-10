package com.ktf.icp.model;

public class OrderData {
    private final String name;
    private final double money;
    private final double zins;
    private final int methode;
    private final String date;

    public OrderData(String name, double money, double zins, int methode, String date) {
        this.name = name;
        this.money = money;
        this.zins = zins;
        this.methode = methode;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public double getMoney() {
        return money;
    }

    public double getZins() {
        return zins;
    }

    public int getMethode() {
        return methode;
    }

    public String getDate() {
        return date;
    }
}
