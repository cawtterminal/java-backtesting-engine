package com.tradingbot.indicator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovingAverageTest {

    private static final double EPSILON = 1e-9;

    @Test
    void smaIsUndefinedDuringWarmUpAndAveragesTheWindowAfterwards() {
        double[] sma = SimpleMovingAverage.compute(new double[]{1, 2, 3, 4, 5}, 3);

        assertTrue(Double.isNaN(sma[0]));
        assertTrue(Double.isNaN(sma[1]));
        assertEquals(2.0, sma[2], EPSILON);
        assertEquals(3.0, sma[3], EPSILON);
        assertEquals(4.0, sma[4], EPSILON);
    }

    @Test
    void smaSkipsLeadingUndefinedValues() {
        double[] sma = SimpleMovingAverage.compute(new double[]{Double.NaN, Double.NaN, 1, 2, 3}, 2);

        assertTrue(Double.isNaN(sma[2]));
        assertEquals(1.5, sma[3], EPSILON);
        assertEquals(2.5, sma[4], EPSILON);
    }

    @Test
    void emaIsSeededWithSmaAndThenSmoothed() {
        double[] ema = ExponentialMovingAverage.compute(new double[]{1, 2, 3, 4, 5}, 3);

        assertTrue(Double.isNaN(ema[1]));
        assertEquals(2.0, ema[2], EPSILON);
        assertEquals(3.0, ema[3], EPSILON);
        assertEquals(4.0, ema[4], EPSILON);
    }

    @Test
    void emaOfConstantSeriesIsConstant() {
        double[] values = new double[30];
        java.util.Arrays.fill(values, 42.0);

        double[] ema = ExponentialMovingAverage.compute(values, 10);

        for (int i = 9; i < values.length; i++) {
            assertEquals(42.0, ema[i], EPSILON);
        }
    }

    @Test
    void emaReactsFasterThanSmaToNewPrices() {
        double[] values = {10, 10, 10, 10, 10, 20};

        double ema = ExponentialMovingAverage.compute(values, 5)[5];
        double sma = SimpleMovingAverage.compute(values, 5)[5];

        assertTrue(ema > sma);
    }

    @Test
    void rejectsNonPositivePeriods() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleMovingAverage(0));
        assertThrows(IllegalArgumentException.class, () -> new ExponentialMovingAverage(-3));
    }
}
