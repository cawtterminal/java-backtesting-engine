package com.tradingbot.strategy;

import com.tradingbot.indicator.RelativeStrengthIndex;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;

import java.util.Arrays;

public final class RsiStrategy implements Strategy {

    private final int period;
    private final double oversold;
    private final double overbought;

    public RsiStrategy(int period, double oversold, double overbought) {
        if (period <= 0) {
            throw new IllegalArgumentException("period must be positive");
        }
        if (oversold <= 0 || overbought >= 100 || oversold >= overbought) {
            throw new IllegalArgumentException("Levels must satisfy 0 < oversold < overbought < 100");
        }
        this.period = period;
        this.oversold = oversold;
        this.overbought = overbought;
    }

    @Override
    public String name() {
        return "RSI(" + period + ") " + (int) oversold + "/" + (int) overbought;
    }

    @Override
    public Signal[] generateSignals(PriceSeries series) {
        double[] rsi = RelativeStrengthIndex.compute(series.closes(), period);
        Signal[] signals = new Signal[rsi.length];
        Arrays.fill(signals, Signal.HOLD);
        for (int i = 1; i < rsi.length; i++) {
            if (Crossovers.crossedAbove(rsi, oversold, i)) {
                signals[i] = Signal.BUY;
            } else if (Crossovers.crossedAbove(rsi, overbought, i)) {
                signals[i] = Signal.SELL;
            }
        }
        return signals;
    }
}
