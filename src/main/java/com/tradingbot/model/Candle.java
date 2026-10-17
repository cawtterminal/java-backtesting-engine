package com.tradingbot.model;

import java.time.LocalDate;
import java.util.Objects;

public record Candle(LocalDate date, double open, double high, double low, double close, double volume) {

    public Candle {
        Objects.requireNonNull(date, "date");
        if (low <= 0) {
            throw new IllegalArgumentException("Prices must be positive at " + date);
        }
        if (high < low) {
            throw new IllegalArgumentException("High is lower than low at " + date);
        }
        if (open < low || open > high || close < low || close > high) {
            throw new IllegalArgumentException("Open and close must be within the high-low range at " + date);
        }
        if (volume < 0) {
            throw new IllegalArgumentException("Volume cannot be negative at " + date);
        }
    }
}
