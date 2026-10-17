package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;

public final class SimpleMovingAverage implements Indicator<double[]> {

    private final int period;

    public SimpleMovingAverage(int period) {
        IndicatorMath.requirePositive(period, "period");
        this.period = period;
    }

    @Override
    public double[] calculate(PriceSeries series) {
        return compute(series.closes(), period);
    }

    @Override
    public String name() {
        return "SMA(" + period + ")";
    }

    public static double[] compute(double[] values, int period) {
        IndicatorMath.requirePositive(period, "period");
        double[] result = IndicatorMath.emptySeries(values.length);
        int start = IndicatorMath.firstValidIndex(values);
        if (start < 0) {
            return result;
        }
        double windowSum = 0;
        for (int i = start; i < values.length; i++) {
            windowSum += values[i];
            if (i - start >= period) {
                windowSum -= values[i - period];
            }
            if (i - start >= period - 1) {
                result[i] = windowSum / period;
            }
        }
        return result;
    }
}
