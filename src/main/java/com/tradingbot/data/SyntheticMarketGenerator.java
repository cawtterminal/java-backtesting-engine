package com.tradingbot.data;

import com.tradingbot.model.Candle;
import com.tradingbot.model.PriceSeries;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SyntheticMarketGenerator {

    private static final MarketRegime[] REGIMES = {
            new MarketRegime(0.0011, 0.014),
            new MarketRegime(-0.0009, 0.022),
            new MarketRegime(0.0001, 0.011),
            new MarketRegime(0.0006, 0.018)
    };

    private final Random random;
    private final int minRegimeLength;
    private final int maxRegimeLength;

    public SyntheticMarketGenerator(long seed) {
        this(seed, 60, 180);
    }

    public SyntheticMarketGenerator(long seed, int minRegimeLength, int maxRegimeLength) {
        if (minRegimeLength <= 0 || maxRegimeLength < minRegimeLength) {
            throw new IllegalArgumentException("Invalid regime length range");
        }
        this.random = new Random(seed);
        this.minRegimeLength = minRegimeLength;
        this.maxRegimeLength = maxRegimeLength;
    }

    public PriceSeries generate(String symbol, LocalDate startDate, int tradingDays, double startPrice) {
        if (tradingDays <= 0) {
            throw new IllegalArgumentException("tradingDays must be positive");
        }
        if (startPrice <= 0) {
            throw new IllegalArgumentException("startPrice must be positive");
        }

        List<Candle> candles = new ArrayList<>(tradingDays);
        LocalDate date = nextTradingDay(startDate.minusDays(1));
        double previousClose = startPrice;
        MarketRegime regime = randomRegime();
        int daysLeftInRegime = randomRegimeLength();

        for (int day = 0; day < tradingDays; day++) {
            if (daysLeftInRegime-- == 0) {
                regime = randomRegime();
                daysLeftInRegime = randomRegimeLength();
            }
            double volatility = regime.volatility();
            double open = previousClose * (1 + random.nextGaussian() * volatility * 0.25);
            double logReturn = regime.drift() - volatility * volatility / 2 + volatility * random.nextGaussian();
            double close = previousClose * Math.exp(logReturn);
            double high = Math.max(open, close) * (1 + Math.abs(random.nextGaussian()) * volatility * 0.5);
            double low = Math.min(open, close) * (1 - Math.abs(random.nextGaussian()) * volatility * 0.5);
            double volume = Math.round(1_000_000 * (1 + Math.abs(logReturn) * 40 + random.nextDouble() * 0.5));

            candles.add(new Candle(date, round(open), round(high), round(low), round(close), volume));
            previousClose = round(close);
            date = nextTradingDay(date);
        }
        return new PriceSeries(symbol, candles);
    }

    private MarketRegime randomRegime() {
        return REGIMES[random.nextInt(REGIMES.length)];
    }

    private int randomRegimeLength() {
        return minRegimeLength + random.nextInt(maxRegimeLength - minRegimeLength + 1);
    }

    private static LocalDate nextTradingDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (next.getDayOfWeek() == DayOfWeek.SATURDAY || next.getDayOfWeek() == DayOfWeek.SUNDAY) {
            next = next.plusDays(1);
        }
        return next;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record MarketRegime(double drift, double volatility) {
    }
}
