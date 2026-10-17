package com.tradingbot.strategy;

final class Crossovers {

    private Crossovers() {
    }

    static boolean crossedAbove(double[] series, double[] reference, int index) {
        return isComparable(series, reference, index)
                && series[index - 1] <= reference[index - 1]
                && series[index] > reference[index];
    }

    static boolean crossedBelow(double[] series, double[] reference, int index) {
        return isComparable(series, reference, index)
                && series[index - 1] >= reference[index - 1]
                && series[index] < reference[index];
    }

    static boolean crossedAbove(double[] series, double level, int index) {
        return index > 0 && isFinite(series[index - 1]) && isFinite(series[index])
                && series[index - 1] <= level && series[index] > level;
    }

    static boolean crossedBelow(double[] series, double level, int index) {
        return index > 0 && isFinite(series[index - 1]) && isFinite(series[index])
                && series[index - 1] >= level && series[index] < level;
    }

    private static boolean isComparable(double[] series, double[] reference, int index) {
        return index > 0
                && isFinite(series[index - 1]) && isFinite(series[index])
                && isFinite(reference[index - 1]) && isFinite(reference[index]);
    }

    private static boolean isFinite(double value) {
        return Double.isFinite(value);
    }
}
