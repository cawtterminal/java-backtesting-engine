package com.tradingbot.engine;

public record BacktestConfig(
        double initialCapital,
        double commissionRate,
        double slippageRate,
        double positionSizing,
        double stopLossPct,
        double takeProfitPct) {

    public BacktestConfig {
        if (initialCapital <= 0) {
            throw new IllegalArgumentException("initialCapital must be positive");
        }
        requireRange(commissionRate, 0, 0.1, "commissionRate");
        requireRange(slippageRate, 0, 0.1, "slippageRate");
        requireRange(stopLossPct, 0, 1, "stopLossPct");
        requireRange(takeProfitPct, 0, 10, "takeProfitPct");
        if (positionSizing <= 0 || positionSizing > 1) {
            throw new IllegalArgumentException("positionSizing must be in (0, 1]");
        }
    }

    public static BacktestConfig defaults() {
        return new BacktestConfig(10_000, 0.001, 0.0005, 1.0, 0, 0);
    }

    public BacktestConfig withInitialCapital(double value) {
        return new BacktestConfig(value, commissionRate, slippageRate, positionSizing, stopLossPct, takeProfitPct);
    }

    public BacktestConfig withCommissionRate(double value) {
        return new BacktestConfig(initialCapital, value, slippageRate, positionSizing, stopLossPct, takeProfitPct);
    }

    public BacktestConfig withSlippageRate(double value) {
        return new BacktestConfig(initialCapital, commissionRate, value, positionSizing, stopLossPct, takeProfitPct);
    }

    public BacktestConfig withPositionSizing(double value) {
        return new BacktestConfig(initialCapital, commissionRate, slippageRate, value, stopLossPct, takeProfitPct);
    }

    public BacktestConfig withStopLoss(double value) {
        return new BacktestConfig(initialCapital, commissionRate, slippageRate, positionSizing, value, takeProfitPct);
    }

    public BacktestConfig withTakeProfit(double value) {
        return new BacktestConfig(initialCapital, commissionRate, slippageRate, positionSizing, stopLossPct, value);
    }

    public boolean hasStopLoss() {
        return stopLossPct > 0;
    }

    public boolean hasTakeProfit() {
        return takeProfitPct > 0;
    }

    private static void requireRange(double value, double min, double max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
    }
}
