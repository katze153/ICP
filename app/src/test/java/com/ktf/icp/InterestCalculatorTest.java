package com.ktf.icp;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class InterestCalculatorTest {
    private final InterestCalculator calculator = new InterestCalculator();

    @Test
    public void calculatesDaily() {
        double result = calculator.calculateByDayCount(10.0, 40.0, InterestCalculator.METHOD_DAILY, 2);
        assertEquals(19.6, result, 0.0001);
    }

    @Test
    public void calculatesYearly() {
        double result = calculator.calculateByDayCount(10.0, 40.0, InterestCalculator.METHOD_YEARLY, 365);
        assertEquals(14.0, result, 0.0001);
    }

    @Test
    public void calculatesFromDateRange() {
        double result = calculator.calculateByDateRange(100.0, 5.0, InterestCalculator.METHOD_YEARLY,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 7, 1));
        assertEquals(calculator.calculateByDayCount(100.0, 5.0, InterestCalculator.METHOD_YEARLY, 181), result, 0.0001);
    }

    @Test
    public void handlesDecimalValues() {
        double result = calculator.calculateByDayCount(50.55, 2.35, InterestCalculator.METHOD_DAILY, 3);
        assertEquals(54.199419839, result, 0.000001);
    }
}
