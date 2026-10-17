package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;

public final class RelativeStrengthIndex implements Indicator<double[]> {

    private final int period;

    public RelativeStrengthIndex(int period) {
        IndicatorMath.requirePositive(period, "period");
        this.period = period;
    }

    @Override
    public double[] calculate(PriceSeries series) {
        return compute(series.closes(), period);
    }

    @Override
    public String name() {
        return "RSI(" + period + ")";
    }

    public static double[] compute(double[] closes, int period) {
        IndicatorMath.requirePositive(period, "period");
        double[] result = IndicatorMath.emptySeries(closes.length);
        if (closes.length <= period) {
            return result;
        }

        double averageGain = 0;
        double averageLoss = 0;
        for (int i = 1; i <= period; i++) {
            double change = closes[i] - closes[i - 1];
            averageGain += Math.max(change, 0);
            averageLoss += Math.max(-change, 0);
        }
        averageGain /= period;
        averageLoss /= period;
        result[period] = toRsi(averageGain, averageLoss);

        for (int i = period + 1; i < closes.length; i++) {
            double change = closes[i] - closes[i - 1];
            averageGain = (averageGain * (period - 1) + Math.max(change, 0)) / period;
            averageLoss = (averageLoss * (period - 1) + Math.max(-change, 0)) / period;
            result[i] = toRsi(averageGain, averageLoss);
        }
        return result;
    }

    private static double toRsi(double averageGain, double averageLoss) {
        if (averageLoss == 0) {
            return averageGain == 0 ? 50 : 100;
        }
        double relativeStrength = averageGain / averageLoss;
        return 100 - 100 / (1 + relativeStrength);
    }
}
