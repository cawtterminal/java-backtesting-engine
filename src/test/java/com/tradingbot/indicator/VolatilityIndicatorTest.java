package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;
import com.tradingbot.support.TestSeries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VolatilityIndicatorTest {

    private static final double EPSILON = 1e-9;

    @Test
    void bollingerBandsUsePopulationStandardDeviation() {
        BollingerBands.Result bands = new BollingerBands(3, 2.0).compute(new double[]{1, 2, 3});

        double expectedDeviation = Math.sqrt(2.0 / 3.0);
        assertEquals(2.0, bands.middle()[2], EPSILON);
        assertEquals(2.0 + 2 * expectedDeviation, bands.upper()[2], EPSILON);
        assertEquals(2.0 - 2 * expectedDeviation, bands.lower()[2], EPSILON);
    }

    @Test
    void bollingerBandsCollapseWithoutVolatility() {
        BollingerBands.Result bands = new BollingerBands(5, 2.0).compute(new double[]{7, 7, 7, 7, 7, 7});

        assertEquals(bands.middle()[5], bands.upper()[5], EPSILON);
        assertEquals(bands.middle()[5], bands.lower()[5], EPSILON);
    }

    @Test
    void bollingerBandsRejectNonPositiveDeviation() {
        assertThrows(IllegalArgumentException.class, () -> new BollingerBands(20, 0));
    }

    @Test
    void atrEqualsRangeWhenThereAreNoGaps() {
        PriceSeries series = TestSeries.of(
                TestSeries.candle(0, 10, 11, 9, 10),
                TestSeries.candle(1, 10, 11, 9, 10),
                TestSeries.candle(2, 10, 11, 9, 10),
                TestSeries.candle(3, 10, 11, 9, 10));

        double[] atr = new AverageTrueRange(3).calculate(series);

        assertTrue(Double.isNaN(atr[1]));
        assertEquals(2.0, atr[2], EPSILON);
        assertEquals(2.0, atr[3], EPSILON);
    }

    @Test
    void atrAccountsForGapsAgainstPreviousClose() {
        PriceSeries series = TestSeries.of(
                TestSeries.candle(0, 10, 10, 10, 10),
                TestSeries.candle(1, 14.5, 15, 14, 14.5));

        double[] atr = new AverageTrueRange(2).calculate(series);

        assertEquals((0 + 5) / 2.0, atr[1], EPSILON);
    }
}
