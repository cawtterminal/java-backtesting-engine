package com.tradingbot.indicator;

import java.util.Arrays;

final class IndicatorMath {

    private IndicatorMath() {
    }

    static double[] emptySeries(int length) {
        double[] series = new double[length];
        Arrays.fill(series, Double.NaN);
        return series;
    }

    static int firstValidIndex(double[] values) {
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                return i;
            }
        }
        return -1;
    }

    static void requirePositive(int period, String parameter) {
        if (period <= 0) {
            throw new IllegalArgumentException(parameter + " must be positive, got " + period);
        }
    }
}
