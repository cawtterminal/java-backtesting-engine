package com.tradingbot.support;

import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;
import com.tradingbot.strategy.Strategy;

public final class FixedSignalStrategy implements Strategy {

    private final Signal[] signals;

    public FixedSignalStrategy(Signal... signals) {
        this.signals = signals.clone();
    }

    @Override
    public String name() {
        return "Fixed";
    }

    @Override
    public Signal[] generateSignals(PriceSeries series) {
        return signals.clone();
    }
}
