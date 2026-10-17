package com.tradingbot.indicator;

import com.tradingbot.support.TestSeries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OscillatorTest {

    private static final double EPSILON = 1e-9;

    @Test
    void rsiFollowsWilderSmoothing() {
        double[] rsi = RelativeStrengthIndex.compute(new double[]{10, 11, 10, 11, 10}, 2);

        assertTrue(Double.isNaN(rsi[1]));
        assertEquals(50.0, rsi[2], EPSILON);
        assertEquals(75.0, rsi[3], EPSILON);
        assertEquals(37.5, rsi[4], EPSILON);
    }

    @Test
    void rsiReachesBoundsOnOneDirectionalMarkets() {
        double[] rising = RelativeStrengthIndex.compute(TestSeries.linear(30, 100, 1), 14);
        double[] falling = RelativeStrengthIndex.compute(TestSeries.linear(30, 100, -1), 14);

        assertEquals(100.0, rising[29], EPSILON);
        assertEquals(0.0, falling[29], EPSILON);
    }

    @Test
    void rsiIsNeutralWhenPriceDoesNotMove() {
        double[] rsi = RelativeStrengthIndex.compute(new double[]{5, 5, 5, 5, 5}, 3);

        assertEquals(50.0, rsi[4], EPSILON);
    }

    @Test
    void macdIsZeroForConstantPrices() {
        double[] closes = new double[60];
        java.util.Arrays.fill(closes, 100.0);

        Macd.Result result = new Macd(12, 26, 9).compute(closes);

        assertEquals(0.0, result.macdLine()[59], EPSILON);
        assertEquals(0.0, result.signalLine()[59], EPSILON);
        assertEquals(0.0, result.histogram()[59], EPSILON);
    }

    @Test
    void macdSignalLineStartsAfterBothWarmUps() {
        Macd.Result result = new Macd(12, 26, 9).compute(TestSeries.linear(60, 100, 1));

        assertTrue(Double.isNaN(result.macdLine()[24]));
        assertTrue(Double.isFinite(result.macdLine()[25]));
        assertTrue(Double.isNaN(result.signalLine()[32]));
        assertTrue(Double.isFinite(result.signalLine()[33]));
    }

    @Test
    void macdIsPositiveInAnUptrend() {
        Macd.Result result = new Macd(12, 26, 9).compute(TestSeries.linear(60, 100, 1));

        assertTrue(result.macdLine()[59] > 0);
    }

    @Test
    void macdRequiresFastPeriodBelowSlowPeriod() {
        assertThrows(IllegalArgumentException.class, () -> new Macd(26, 12, 9));
    }
}
