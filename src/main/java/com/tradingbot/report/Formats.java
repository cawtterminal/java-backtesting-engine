package com.tradingbot.report;

import java.util.Locale;

public final class Formats {

    private Formats() {
    }

    public static String signedPercent(double value) {
        return String.format(Locale.US, "%+.2f%%", normalize(value) * 100);
    }

    public static String percent(double value) {
        return String.format(Locale.US, "%.2f%%", normalize(value) * 100);
    }

    public static String money(double value) {
        double normalized = normalize(value);
        String amount = String.format(Locale.US, "$%,.2f", Math.abs(normalized));
        return normalized < 0 ? "-" + amount : amount;
    }

    public static String ratio(double value) {
        if (Double.isInfinite(value)) {
            return "inf";
        }
        return String.format(Locale.US, "%.2f", normalize(value));
    }

    public static String decimal(double value, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", normalize(value));
    }

    private static double normalize(double value) {
        return Math.abs(value) < 1e-12 ? 0.0 : value;
    }
}
