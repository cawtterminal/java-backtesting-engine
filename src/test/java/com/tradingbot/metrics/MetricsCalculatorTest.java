package com.tradingbot.metrics;

import com.tradingbot.model.EquityPoint;
import com.tradingbot.model.ExitReason;
import com.tradingbot.model.Trade;
import com.tradingbot.support.TestSeries;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsCalculatorTest {

    private static final double EPSILON = 1e-9;

    private final MetricsCalculator calculator = new MetricsCalculator();

    @Test
    void maxDrawdownIsTheDeepestPeakToTroughDecline() {
        assertEquals(0.5, MetricsCalculator.maxDrawdown(new double[]{100, 120, 90, 130, 65}), EPSILON);
    }

    @Test
    void maxDrawdownIsZeroForMonotonicGrowth() {
        assertEquals(0.0, MetricsCalculator.maxDrawdown(new double[]{100, 101, 105, 110}), EPSILON);
    }

    @Test
    void annualizedReturnMatchesTotalReturnOverOneYear() {
        List<EquityPoint> curve = curve(252, 100, 121);

        PerformanceMetrics metrics = calculator.calculate(100, curve, List.of(), 0, 1);

        assertEquals(0.21, metrics.totalReturn(), EPSILON);
        assertEquals(0.21, metrics.annualizedReturn(), 1e-6);
    }

    @Test
    void flatEquityHasNeutralRiskRatios() {
        PerformanceMetrics metrics = calculator.calculate(100, curve(50, 100, 100), List.of(), 0, 0);

        assertEquals(0.0, metrics.totalReturn(), EPSILON);
        assertEquals(0.0, metrics.sharpeRatio(), EPSILON);
        assertEquals(0.0, metrics.sortinoRatio(), EPSILON);
        assertEquals(0.0, metrics.maxDrawdown(), EPSILON);
    }

    @Test
    void steadyGainsProducePositiveSharpe() {
        List<EquityPoint> points = new ArrayList<>();
        double equity = 100;
        for (int i = 0; i < 100; i++) {
            equity *= i % 2 == 0 ? 1.01 : 0.998;
            points.add(new EquityPoint(TestSeries.START.plusDays(i), equity));
        }

        PerformanceMetrics metrics = calculator.calculate(100, points, List.of(), 0, 1);

        assertTrue(metrics.sharpeRatio() > 0);
        assertTrue(metrics.sortinoRatio() > metrics.sharpeRatio());
    }

    @Test
    void tradeStatisticsAreAggregated() {
        List<Trade> trades = List.of(trade(100, 200), trade(100, 300), trade(100, 0.5 * 100));

        PerformanceMetrics metrics = calculator.calculate(100, curve(10, 100, 100), trades, 0, 0.5);

        assertEquals(3, metrics.totalTrades());
        assertEquals(2.0 / 3.0, metrics.winRate(), EPSILON);
        assertEquals(300.0 / 50.0, metrics.profitFactor(), EPSILON);
        assertEquals(2.0, metrics.bestTradeReturn(), EPSILON);
        assertEquals(-0.5, metrics.worstTradeReturn(), EPSILON);
        assertEquals((1.0 + 2.0 - 0.5) / 3, metrics.averageTradeReturn(), EPSILON);
    }

    @Test
    void profitFactorIsInfiniteWithoutLosingTrades() {
        PerformanceMetrics metrics = calculator.calculate(100, curve(10, 100, 100),
                List.of(trade(100, 110)), 0, 0.5);

        assertTrue(Double.isInfinite(metrics.profitFactor()));
    }

    private static Trade trade(double entry, double exit) {
        return new Trade(TestSeries.START, entry, TestSeries.START.plusDays(5), exit, 1, 0, ExitReason.SIGNAL);
    }

    private static List<EquityPoint> curve(int length, double start, double end) {
        List<EquityPoint> points = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            double value = i == length - 1 ? end : start + (end - start) * i / (length - 1);
            points.add(new EquityPoint(TestSeries.START.plusDays(i), value));
        }
        return points;
    }
}
