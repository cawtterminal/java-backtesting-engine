package com.tradingbot.strategy;

import com.tradingbot.indicator.Macd;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;

import java.util.Arrays;

public final class MacdStrategy implements Strategy {

    private final Macd macd;

    public MacdStrategy(int fastPeriod, int slowPeriod, int signalPeriod) {
        this.macd = new Macd(fastPeriod, slowPeriod, signalPeriod);
    }

    @Override
    public String name() {
        return macd.name();
    }

    @Override
    public Signal[] generateSignals(PriceSeries series) {
        Macd.Result result = macd.calculate(series);
        double[] macdLine = result.macdLine();
        double[] signalLine = result.signalLine();

        Signal[] signals = new Signal[macdLine.length];
        Arrays.fill(signals, Signal.HOLD);
        for (int i = 1; i < macdLine.length; i++) {
            if (Crossovers.crossedAbove(macdLine, signalLine, i)) {
                signals[i] = Signal.BUY;
            } else if (Crossovers.crossedBelow(macdLine, signalLine, i)) {
                signals[i] = Signal.SELL;
            }
        }
        return signals;
    }
}
