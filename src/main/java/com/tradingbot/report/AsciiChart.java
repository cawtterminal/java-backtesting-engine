package com.tradingbot.report;

import java.util.Locale;

public final class AsciiChart {

    private AsciiChart() {
    }

    public static String render(double[] values, int width, int height) {
        if (values.length == 0 || width < 2 || height < 2) {
            return "";
        }
        double[] sampled = resample(values, Math.min(width, values.length));
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double value : sampled) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double range = max - min == 0 ? 1 : max - min;

        int[] levels = new int[sampled.length];
        for (int i = 0; i < sampled.length; i++) {
            levels[i] = (int) Math.round((sampled[i] - min) / range * (height - 1));
        }

        StringBuilder chart = new StringBuilder();
        for (int row = height - 1; row >= 0; row--) {
            chart.append(axisLabel(row, height, min, max)).append(" |");
            for (int col = 0; col < sampled.length; col++) {
                chart.append(cell(levels, col, row));
            }
            chart.append(System.lineSeparator());
        }
        chart.append(" ".repeat(12)).append('+').append("-".repeat(sampled.length)).append(System.lineSeparator());
        return chart.toString();
    }

    private static char cell(int[] levels, int col, int row) {
        int current = levels[col];
        if (current == row) {
            return '*';
        }
        if (col > 0) {
            int previous = levels[col - 1];
            int low = Math.min(previous, current);
            int high = Math.max(previous, current);
            if (row > low && row < high) {
                return ':';
            }
        }
        return ' ';
    }

    private static String axisLabel(int row, int height, double min, double max) {
        boolean labeled = row == 0 || row == height - 1 || row == (height - 1) / 2;
        if (!labeled) {
            return " ".repeat(11);
        }
        double value = min + (max - min) * row / (height - 1);
        return String.format(Locale.US, "%11s", String.format(Locale.US, "%,.0f", value));
    }

    private static double[] resample(double[] values, int points) {
        if (points == values.length) {
            return values.clone();
        }
        double[] result = new double[points];
        for (int i = 0; i < points; i++) {
            int index = (int) Math.round((double) i * (values.length - 1) / (points - 1));
            result[i] = values[index];
        }
        return result;
    }
}
