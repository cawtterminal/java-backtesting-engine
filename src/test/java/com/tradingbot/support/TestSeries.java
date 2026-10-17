package com.tradingbot.support;

import com.tradingbot.model.Candle;
import com.tradingbot.model.PriceSeries;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TestSeries {

    public static final LocalDate START = LocalDate.of(2024, 1, 1);

    private TestSeries() {
    }

    public static PriceSeries fromCloses(double... closes) {
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < closes.length; i++) {
            candles.add(candle(i, closes[i], closes[i], closes[i], closes[i]));
        }
        return new PriceSeries("TEST", candles);
    }

    public static PriceSeries of(Candle... candles) {
        return new PriceSeries("TEST", List.of(candles));
    }

    public static Candle candle(int day, double open, double high, double low, double close) {
        return new Candle(START.plusDays(day), open, high, low, close, 1_000);
    }

    public static Candle flat(int day, double price) {
        return candle(day, price, price, price, price);
    }

    public static double[] linear(int length, double start, double step) {
        double[] values = new double[length];
        for (int i = 0; i < length; i++) {
            values[i] = start + step * i;
        }
        return values;
    }
}
