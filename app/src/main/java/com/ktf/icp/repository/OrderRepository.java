package com.ktf.icp.repository;

import android.content.Context;

import com.ktf.config.Config;
import com.ktf.icp.InterestCalculator;
import com.ktf.icp.model.CalculationResult;
import com.ktf.icp.model.OrderData;
import com.ktf.icp.util.DateUtils;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderRepository {
    public static final String SAVE_FILE = "save.toml";
    private final Config config;

    public OrderRepository(Context context) {
        this(context.getFilesDir().toPath().resolve(SAVE_FILE));
    }

    public OrderRepository(Path savePath) {
        this.config = new Config(savePath.toString());
    }

    public String getSavePath() {
        return config.getConfigPath();
    }

    public boolean exists(String name) {
        return config.sectionExists(name);
    }

    public boolean save(OrderData orderData) {
        config.createSection(orderData.getName());
        boolean ok = true;
        ok &= config.addKeyToSection("money", Double.toString(orderData.getMoney()), orderData.getName());
        ok &= config.addKeyToSection("zins", Double.toString(orderData.getZins()), orderData.getName());
        ok &= config.addKeyToSection("methode", Integer.toString(orderData.getMethode()), orderData.getName());
        ok &= config.addKeyToSection("date", orderData.getDate(), orderData.getName());
        return ok;
    }

    public OrderData get(String name) {
        if (!config.sectionExists(name)) {
            return null;
        }
        String money = config.getValueFromSection("money", name);
        String zins = config.getValueFromSection("zins", name);
        String methode = config.getValueFromSection("methode", name);
        String date = config.getValueFromSection("date", name);
        if (money == null || zins == null || methode == null || date == null) {
            return null;
        }
        return new OrderData(name, Double.parseDouble(money), Double.parseDouble(zins), Integer.parseInt(methode), date);
    }

    public List<String> getAllOrderNames() {
        return new ArrayList<>(Arrays.asList(config.getAllSections()));
    }

    public boolean delete(String name) {
        return config.deleteSection(name);
    }

    public CalculationResult calculateCurrentResult(OrderData order, LocalDate now, InterestCalculator calculator) {
        LocalDate start = DateUtils.parseDate(order.getDate());
        int days = DateUtils.daysBetween(start, now);
        double finalAmount = calculator.calculateFinalAmount(order.getMoney(), order.getZins(), order.getMethode(), days);
        return new CalculationResult(order.getMoney(), days, order.getZins(), order.getMethode(), finalAmount, order.getDate(), DateUtils.formatDate(now));
    }

    public static File getSaveFile(Context context) {
        return new File(context.getFilesDir(), SAVE_FILE);
    }
}
