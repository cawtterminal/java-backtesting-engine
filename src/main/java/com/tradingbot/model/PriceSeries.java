package com.tradingbot.model;

import java.util.List;
import java.util.Objects;

public final class PriceSeries {

    private final String symbol;
    private final List<Candle> candles;

    public PriceSeries(String symbol, List<Candle> candles) {
        this.symbol = Objects.requireNonNull(symbol, "symbol");
        if (candles == null || candles.isEmpty()) {
            throw new IllegalArgumentException("A price series needs at least one candle");
        }
        for (int i = 1; i < candles.size(); i++) {
            if (!candles.get(i).date().isAfter(candles.get(i - 1).date())) {
                throw new IllegalArgumentException(
                        "Candles must be sorted by date without duplicates near " + candles.get(i).date());
            }
        }
        this.candles = List.copyOf(candles);
    }

    public String symbol() {
        return symbol;
    }

    public int size() {
        return candles.size();
    }

    public Candle get(int index) {
        return candles.get(index);
    }

    public Candle first() {
        return candles.get(0);
    }

    public Candle last() {
        return candles.get(candles.size() - 1);
    }

    public List<Candle> candles() {
        return candles;
    }

    public PriceSeries slice(int fromInclusive, int toExclusive) {
        return new PriceSeries(symbol, candles.subList(fromInclusive, toExclusive));
    }

    public double[] closes() {
        return candles.stream().mapToDouble(Candle::close).toArray();
    }

    public double[] highs() {
        return candles.stream().mapToDouble(Candle::high).toArray();
    }

    public double[] lows() {
        return candles.stream().mapToDouble(Candle::low).toArray();
    }
}
