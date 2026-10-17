package com.tradingbot.strategy;

import com.tradingbot.indicator.ExponentialMovingAverage;
import com.tradingbot.indicator.SimpleMovingAverage;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;

import java.util.Arrays;

public final class MovingAverageCrossStrategy implements Strategy {

    public enum AverageType {
        SIMPLE("SMA"),
        EXPONENTIAL("EMA");

        private final String label;

        AverageType(String label) {
            this.label = label;
        }
    }

    private final AverageType type;
    private final int fastPeriod;
    private final int slowPeriod;

    public MovingAverageCrossStrategy(AverageType type, int fastPeriod, int slowPeriod) {
        if (fastPeriod <= 0 || slowPeriod <= 0) {
            throw new IllegalArgumentException("Periods must be positive");
        }
        if (fastPeriod >= slowPeriod) {
            throw new IllegalArgumentException("fastPeriod must be lower than slowPeriod");
        }
        this.type = type;
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
    }

    @Override
    public String name() {
        return type.label + " Cross (" + fastPeriod + "/" + slowPeriod + ")";
    }

    @Override
    public Signal[] generateSignals(PriceSeries series) {
        double[] closes = series.closes();
        double[] fast = average(closes, fastPeriod);
        double[] slow = average(closes, slowPeriod);

        Signal[] signals = new Signal[closes.length];
        Arrays.fill(signals, Signal.HOLD);
        for (int i = 1; i < closes.length; i++) {
            if (Crossovers.crossedAbove(fast, slow, i)) {
                signals[i] = Signal.BUY;
            } else if (Crossovers.crossedBelow(fast, slow, i)) {
                signals[i] = Signal.SELL;
            }
        }
        return signals;
    }

    private double[] average(double[] values, int period) {
        return type == AverageType.SIMPLE
                ? SimpleMovingAverage.compute(values, period)
                : ExponentialMovingAverage.compute(values, period);
    }
}
