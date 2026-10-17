package com.tradingbot.engine;

import com.tradingbot.model.ExitReason;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;
import com.tradingbot.model.Trade;
import com.tradingbot.support.FixedSignalStrategy;
import com.tradingbot.support.TestSeries;
import org.junit.jupiter.api.Test;

import static com.tradingbot.model.Signal.BUY;
import static com.tradingbot.model.Signal.HOLD;
import static com.tradingbot.model.Signal.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BacktestEngineTest {

    private static final double EPSILON = 1e-6;
    private static final BacktestConfig FRICTIONLESS = BacktestConfig.defaults()
            .withCommissionRate(0)
            .withSlippageRate(0);

    private static final PriceSeries RALLY = TestSeries.of(
            TestSeries.flat(0, 100),
            TestSeries.candle(1, 110, 120, 110, 120),
            TestSeries.flat(2, 130));

    @Test
    void ordersAreFilledAtTheNextOpenToAvoidLookAheadBias() {
        BacktestResult result = run(FRICTIONLESS, RALLY, BUY, SELL, HOLD);

        Trade trade = result.trades().get(0);
        assertEquals(110.0, trade.entryPrice(), EPSILON);
        assertEquals(130.0, trade.exitPrice(), EPSILON);
        assertEquals(ExitReason.SIGNAL, trade.exitReason());
        assertEquals(10_000 * 130.0 / 110.0, result.finalEquity(), EPSILON);
    }

    @Test
    void commissionsAreChargedOnBothSides() {
        BacktestConfig config = FRICTIONLESS.withCommissionRate(0.01);

        BacktestResult result = run(config, RALLY, BUY, SELL, HOLD);

        double expected = 10_000 / (110 * 1.01) * 130 * 0.99;
        assertEquals(expected, result.finalEquity(), EPSILON);
    }

    @Test
    void slippageWorsensBothFills() {
        BacktestResult result = run(FRICTIONLESS.withSlippageRate(0.01), RALLY, BUY, SELL, HOLD);

        Trade trade = result.trades().get(0);
        assertEquals(111.1, trade.entryPrice(), EPSILON);
        assertEquals(128.7, trade.exitPrice(), EPSILON);
    }

    @Test
    void positionSizingKeepsPartOfTheCapitalInCash() {
        BacktestResult result = run(FRICTIONLESS.withPositionSizing(0.5), RALLY, BUY, SELL, HOLD);

        assertEquals(5_000 + 5_000 * 130.0 / 110.0, result.finalEquity(), EPSILON);
    }

    @Test
    void stopLossExitsAtTheStopPrice() {
        PriceSeries series = TestSeries.of(
                TestSeries.flat(0, 100),
                TestSeries.flat(1, 100),
                TestSeries.candle(2, 98, 99, 80, 85));

        BacktestResult result = run(FRICTIONLESS.withStopLoss(0.10), series, BUY, HOLD, HOLD);

        Trade trade = result.trades().get(0);
        assertEquals(ExitReason.STOP_LOSS, trade.exitReason());
        assertEquals(90.0, trade.exitPrice(), EPSILON);
    }

    @Test
    void stopLossFillsAtTheOpenWhenPriceGapsThroughIt() {
        PriceSeries series = TestSeries.of(
                TestSeries.flat(0, 100),
                TestSeries.flat(1, 100),
                TestSeries.candle(2, 85, 86, 80, 82));

        BacktestResult result = run(FRICTIONLESS.withStopLoss(0.10), series, BUY, HOLD, HOLD);

        assertEquals(85.0, result.trades().get(0).exitPrice(), EPSILON);
    }

    @Test
    void takeProfitExitsAtTheTargetPrice() {
        PriceSeries series = TestSeries.of(
                TestSeries.flat(0, 100),
                TestSeries.flat(1, 100),
                TestSeries.candle(2, 104, 115, 103, 112));

        BacktestResult result = run(FRICTIONLESS.withTakeProfit(0.10), series, BUY, HOLD, HOLD);

        Trade trade = result.trades().get(0);
        assertEquals(ExitReason.TAKE_PROFIT, trade.exitReason());
        assertEquals(110.0, trade.exitPrice(), EPSILON);
    }

    @Test
    void openPositionIsClosedAtTheEndOfTheData() {
        BacktestResult result = run(FRICTIONLESS, RALLY, BUY, HOLD, HOLD);

        Trade trade = result.trades().get(0);
        assertEquals(ExitReason.END_OF_DATA, trade.exitReason());
        assertEquals(130.0, trade.exitPrice(), EPSILON);
    }

    @Test
    void withoutSignalsCapitalStaysIntact() {
        BacktestResult result = run(FRICTIONLESS, RALLY, HOLD, HOLD, HOLD);

        assertEquals(0, result.trades().size());
        assertEquals(10_000.0, result.finalEquity(), EPSILON);
        assertEquals(0.0, result.metrics().exposure(), EPSILON);
    }

    @Test
    void reportsBuyAndHoldBenchmark() {
        BacktestResult result = run(FRICTIONLESS, RALLY, HOLD, HOLD, HOLD);

        assertEquals(0.30, result.metrics().buyAndHoldReturn(), EPSILON);
    }

    @Test
    void rejectsStrategiesWithMismatchedSignalCount() {
        BacktestEngine engine = new BacktestEngine(FRICTIONLESS);

        assertThrows(IllegalStateException.class,
                () -> engine.run(RALLY, new FixedSignalStrategy(BUY, SELL)));
    }

    @Test
    void configValidatesItsParameters() {
        assertThrows(IllegalArgumentException.class, () -> BacktestConfig.defaults().withInitialCapital(0));
        assertThrows(IllegalArgumentException.class, () -> BacktestConfig.defaults().withPositionSizing(1.5));
        assertThrows(IllegalArgumentException.class, () -> BacktestConfig.defaults().withCommissionRate(-0.01));
    }

    private static BacktestResult run(BacktestConfig config, PriceSeries series, Signal... signals) {
        return new BacktestEngine(config).run(series, new FixedSignalStrategy(signals));
    }
}
