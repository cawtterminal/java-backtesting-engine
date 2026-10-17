package com.tradingbot.strategy;

import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;
import com.tradingbot.strategy.MovingAverageCrossStrategy.AverageType;
import com.tradingbot.support.FixedSignalStrategy;
import com.tradingbot.support.TestSeries;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyTest {

    @Test
    void movingAverageCrossSignalsOnCrossovers() {
        PriceSeries series = TestSeries.fromCloses(5, 4, 3, 2, 3, 4, 5, 6, 5, 4, 3, 2);

        Signal[] signals = new MovingAverageCrossStrategy(AverageType.SIMPLE, 2, 3).generateSignals(series);

        assertEquals(Signal.BUY, signals[5]);
        assertEquals(Signal.SELL, signals[9]);
        assertEquals(10, countOf(signals, Signal.HOLD));
    }

    @Test
    void movingAverageCrossRejectsInvertedPeriods() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovingAverageCrossStrategy(AverageType.EXPONENTIAL, 50, 20));
    }

    @Test
    void rsiStrategyBuysWhenLeavingOversoldZone() {
        double[] closes = new double[40];
        for (int i = 0; i < 30; i++) {
            closes[i] = 100 - i;
        }
        for (int i = 30; i < 40; i++) {
            closes[i] = closes[i - 1] + 3;
        }

        Signal[] signals = new RsiStrategy(14, 30, 70).generateSignals(TestSeries.fromCloses(closes));

        assertEquals(1, countOf(signals, Signal.BUY));
        int buyIndex = Arrays.asList(signals).indexOf(Signal.BUY);
        assertTrue(buyIndex >= 30);
    }

    @Test
    void trendFilterBlocksBuysInDowntrends() {
        PriceSeries falling = TestSeries.fromCloses(TestSeries.linear(10, 100, -1));
        Strategy base = new FixedSignalStrategy(signalsWith(10, 9, Signal.BUY));

        Signal[] signals = new TrendFilteredStrategy(base, 5).generateSignals(falling);

        assertEquals(Signal.HOLD, signals[9]);
    }

    @Test
    void trendFilterKeepsBuysInUptrendsAndAlwaysKeepsSells() {
        PriceSeries rising = TestSeries.fromCloses(TestSeries.linear(10, 100, 1));
        Signal[] base = signalsWith(10, 9, Signal.BUY);
        base[2] = Signal.SELL;

        Signal[] signals = new TrendFilteredStrategy(new FixedSignalStrategy(base), 5).generateSignals(rising);

        assertEquals(Signal.BUY, signals[9]);
        assertEquals(Signal.SELL, signals[2]);
    }

    @Test
    void everyPresetProducesOneSignalPerCandle() {
        PriceSeries series = TestSeries.fromCloses(TestSeries.linear(250, 50, 0.3));

        for (Strategy strategy : StrategyFactory.allPresets()) {
            assertEquals(series.size(), strategy.generateSignals(series).length, strategy.name());
        }
    }

    @Test
    void factoryRejectsUnknownStrategies() {
        assertThrows(IllegalArgumentException.class, () -> StrategyFactory.create("martingale"));
    }

    @Test
    void movingAverageGridSkipsInvalidCombinations() {
        assertEquals(4, StrategyFactory.movingAverageGrid(new int[]{10, 50}, new int[]{20, 50, 100}).size());
    }

    private static Signal[] signalsWith(int length, int index, Signal signal) {
        Signal[] signals = new Signal[length];
        Arrays.fill(signals, Signal.HOLD);
        signals[index] = signal;
        return signals;
    }

    private static long countOf(Signal[] signals, Signal target) {
        return Arrays.stream(signals).filter(signal -> signal == target).count();
    }
}
