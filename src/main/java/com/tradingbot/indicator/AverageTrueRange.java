package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;

public final class AverageTrueRange implements Indicator<double[]> {

    private final int period;

    public AverageTrueRange(int period) {
        IndicatorMath.requirePositive(period, "period");
        this.period = period;
    }

    @Override
    public double[] calculate(PriceSeries series) {
        return compute(series.highs(), series.lows(), series.closes(), period);
    }

    @Override
    public String name() {
        return "ATR(" + period + ")";
    }

    public static double[] compute(double[] highs, double[] lows, double[] closes, int period) {
        IndicatorMath.requirePositive(period, "period");
        int length = closes.length;
        double[] result = IndicatorMath.emptySeries(length);
        if (length < period) {
            return result;
        }

        double[] trueRange = new double[length];
        trueRange[0] = highs[0] - lows[0];
        for (int i = 1; i < length; i++) {
            double range = highs[i] - lows[i];
            double gapUp = Math.abs(highs[i] - closes[i - 1]);
            double gapDown = Math.abs(lows[i] - closes[i - 1]);
            trueRange[i] = Math.max(range, Math.max(gapUp, gapDown));
        }

        double seed = 0;
        for (int i = 0; i < period; i++) {
            seed += trueRange[i];
        }
        result[period - 1] = seed / period;
        for (int i = period; i < length; i++) {
            result[i] = (result[i - 1] * (period - 1) + trueRange[i]) / period;
        }
        return result;
    }
}
