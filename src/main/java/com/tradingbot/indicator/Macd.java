package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;

public final class Macd implements Indicator<Macd.Result> {

    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;

    public Macd(int fastPeriod, int slowPeriod, int signalPeriod) {
        IndicatorMath.requirePositive(fastPeriod, "fastPeriod");
        IndicatorMath.requirePositive(slowPeriod, "slowPeriod");
        IndicatorMath.requirePositive(signalPeriod, "signalPeriod");
        if (fastPeriod >= slowPeriod) {
            throw new IllegalArgumentException("fastPeriod must be lower than slowPeriod");
        }
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
    }

    @Override
    public Result calculate(PriceSeries series) {
        return compute(series.closes());
    }

    @Override
    public String name() {
        return "MACD(" + fastPeriod + "," + slowPeriod + "," + signalPeriod + ")";
    }

    public Result compute(double[] closes) {
        double[] fast = ExponentialMovingAverage.compute(closes, fastPeriod);
        double[] slow = ExponentialMovingAverage.compute(closes, slowPeriod);

        double[] macdLine = new double[closes.length];
        for (int i = 0; i < closes.length; i++) {
            macdLine[i] = fast[i] - slow[i];
        }
        double[] signalLine = ExponentialMovingAverage.compute(macdLine, signalPeriod);

        double[] histogram = new double[closes.length];
        for (int i = 0; i < closes.length; i++) {
            histogram[i] = macdLine[i] - signalLine[i];
        }
        return new Result(macdLine, signalLine, histogram);
    }

    public record Result(double[] macdLine, double[] signalLine, double[] histogram) {
    }
}
