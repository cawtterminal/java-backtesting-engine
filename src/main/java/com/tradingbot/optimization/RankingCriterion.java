package com.tradingbot.optimization;

import com.tradingbot.metrics.PerformanceMetrics;

import java.util.Comparator;
import java.util.Locale;

public enum RankingCriterion {
    SHARPE(Comparator.comparingDouble(PerformanceMetrics::sharpeRatio)),
    SORTINO(Comparator.comparingDouble(PerformanceMetrics::sortinoRatio)),
    CALMAR(Comparator.comparingDouble(PerformanceMetrics::calmarRatio)),
    TOTAL_RETURN(Comparator.comparingDouble(PerformanceMetrics::totalReturn));

    private final Comparator<PerformanceMetrics> comparator;

    RankingCriterion(Comparator<PerformanceMetrics> comparator) {
        this.comparator = comparator;
    }

    public Comparator<PerformanceMetrics> bestFirst() {
        return comparator.reversed();
    }

    public static RankingCriterion parse(String value) {
        try {
            return valueOf(value.trim().replace('-', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ranking criterion '" + value
                    + "'. Use sharpe, sortino, calmar or total-return");
        }
    }
}
