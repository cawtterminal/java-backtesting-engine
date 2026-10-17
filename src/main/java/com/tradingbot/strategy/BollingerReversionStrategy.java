package com.tradingbot.strategy;

import com.tradingbot.indicator.BollingerBands;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;

import java.util.Arrays;

public final class BollingerReversionStrategy implements Strategy {

    private final BollingerBands bands;

    public BollingerReversionStrategy(int period, double deviations) {
        this.bands = new BollingerBands(period, deviations);
    }

    @Override
    public String name() {
        return "Bollinger Reversion " + bands.name();
    }

    @Override
    public Signal[] generateSignals(PriceSeries series) {
        double[] closes = series.closes();
        BollingerBands.Result result = bands.compute(closes);

        Signal[] signals = new Signal[closes.length];
        Arrays.fill(signals, Signal.HOLD);
        for (int i = 1; i < closes.length; i++) {
            if (Crossovers.crossedAbove(closes, result.lower(), i)) {
                signals[i] = Signal.BUY;
            } else if (Crossovers.crossedAbove(closes, result.middle(), i)) {
                signals[i] = Signal.SELL;
            }
        }
        return signals;
    }
}
