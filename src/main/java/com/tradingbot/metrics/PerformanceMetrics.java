package com.tradingbot.metrics;

public record PerformanceMetrics(
        double totalReturn,
        double annualizedReturn,
        double annualizedVolatility,
        double maxDrawdown,
        double sharpeRatio,
        double sortinoRatio,
        double calmarRatio,
        int totalTrades,
        double winRate,
        double profitFactor,
        double averageTradeReturn,
        double bestTradeReturn,
        double worstTradeReturn,
        double averageHoldingDays,
        double exposure,
        double buyAndHoldReturn) {

    public double excessReturnVsBuyAndHold() {
        return totalReturn - buyAndHoldReturn;
    }
}
