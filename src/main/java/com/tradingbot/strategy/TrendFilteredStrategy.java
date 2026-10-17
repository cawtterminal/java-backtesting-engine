package com.tradingbot.strategy;

import com.tradingbot.indicator.SimpleMovingAverage;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;

public final class TrendFilteredStrategy implements Strategy {

    private final Strategy delegate;
    private final int trendPeriod;

    public TrendFilteredStrategy(Strategy delegate, int trendPeriod) {
        if (trendPeriod <= 0) {
            throw new IllegalArgumentException("trendPeriod must be positive");
        }
        this.delegate = delegate;
        this.trendPeriod = trendPeriod;
    }

    @Override
    public String name() {
        return delegate.name() + " + Trend(" + trendPeriod + ")";
    }

    @Override
    public Signal[] generateSignals(PriceSeries series) {
        Signal[] signals = delegate.generateSignals(series);
        double[] closes = series.closes();
        double[] trend = SimpleMovingAverage.compute(closes, trendPeriod);

        for (int i = 0; i < signals.length; i++) {
            boolean inUptrend = Double.isFinite(trend[i]) && closes[i] > trend[i];
            if (signals[i] == Signal.BUY && !inUptrend) {
                signals[i] = Signal.HOLD;
            }
        }
        return signals;
    }
}
