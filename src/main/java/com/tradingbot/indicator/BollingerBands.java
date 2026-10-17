package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;

public final class BollingerBands implements Indicator<BollingerBands.Result> {

    private final int period;
    private final double deviations;

    public BollingerBands(int period, double deviations) {
        IndicatorMath.requirePositive(period, "period");
        if (deviations <= 0) {
            throw new IllegalArgumentException("deviations must be positive");
        }
        this.period = period;
        this.deviations = deviations;
    }

    @Override
    public Result calculate(PriceSeries series) {
        return compute(series.closes());
    }

    @Override
    public String name() {
        return "BB(" + period + "," + deviations + ")";
    }

    public Result compute(double[] closes) {
        double[] middle = SimpleMovingAverage.compute(closes, period);
        double[] upper = IndicatorMath.emptySeries(closes.length);
        double[] lower = IndicatorMath.emptySeries(closes.length);

        for (int i = period - 1; i < closes.length; i++) {
            double sumOfSquares = 0;
            for (int j = i - period + 1; j <= i; j++) {
                double diff = closes[j] - middle[i];
                sumOfSquares += diff * diff;
            }
            double standardDeviation = Math.sqrt(sumOfSquares / period);
            upper[i] = middle[i] + deviations * standardDeviation;
            lower[i] = middle[i] - deviations * standardDeviation;
        }
        return new Result(upper, middle, lower);
    }

    public record Result(double[] upper, double[] middle, double[] lower) {
    }
}
