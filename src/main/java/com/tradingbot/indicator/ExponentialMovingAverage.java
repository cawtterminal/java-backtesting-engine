package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;

public final class ExponentialMovingAverage implements Indicator<double[]> {

    private final int period;

    public ExponentialMovingAverage(int period) {
        IndicatorMath.requirePositive(period, "period");
        this.period = period;
    }

    @Override
    public double[] calculate(PriceSeries series) {
        return compute(series.closes(), period);
    }

    @Override
    public String name() {
        return "EMA(" + period + ")";
    }

    public static double[] compute(double[] values, int period) {
        IndicatorMath.requirePositive(period, "period");
        double[] result = IndicatorMath.emptySeries(values.length);
        int start = IndicatorMath.firstValidIndex(values);
        if (start < 0 || values.length - start < period) {
            return result;
        }

        double seed = 0;
        for (int i = start; i < start + period; i++) {
            seed += values[i];
        }
        int seedIndex = start + period - 1;
        result[seedIndex] = seed / period;

        double alpha = 2.0 / (period + 1);
        for (int i = seedIndex + 1; i < values.length; i++) {
            result[i] = alpha * values[i] + (1 - alpha) * result[i - 1];
        }
        return result;
    }
}
