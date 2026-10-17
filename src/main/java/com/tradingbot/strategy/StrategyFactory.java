package com.tradingbot.strategy;

import com.tradingbot.strategy.MovingAverageCrossStrategy.AverageType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class StrategyFactory {

    private static final Map<String, Supplier<Strategy>> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("sma-cross", () -> new MovingAverageCrossStrategy(AverageType.SIMPLE, 20, 50));
        PRESETS.put("ema-cross", () -> new MovingAverageCrossStrategy(AverageType.EXPONENTIAL, 12, 26));
        PRESETS.put("rsi", () -> new RsiStrategy(14, 30, 70));
        PRESETS.put("macd", () -> new MacdStrategy(12, 26, 9));
        PRESETS.put("bollinger", () -> new BollingerReversionStrategy(20, 2.0));
        PRESETS.put("rsi-trend", () -> new TrendFilteredStrategy(new RsiStrategy(14, 40, 70), 100));
    }

    private StrategyFactory() {
    }

    public static Strategy create(String id) {
        Supplier<Strategy> supplier = PRESETS.get(id);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown strategy '" + id + "'. Available: " + availableIds());
        }
        return supplier.get();
    }

    public static List<String> availableIds() {
        return List.copyOf(PRESETS.keySet());
    }

    public static List<Strategy> allPresets() {
        return PRESETS.values().stream().map(Supplier::get).toList();
    }

    public static List<Strategy> movingAverageGrid(int[] fastPeriods, int[] slowPeriods) {
        List<Strategy> candidates = new ArrayList<>();
        for (int fast : fastPeriods) {
            for (int slow : slowPeriods) {
                if (fast < slow) {
                    candidates.add(new MovingAverageCrossStrategy(AverageType.SIMPLE, fast, slow));
                }
            }
        }
        return candidates;
    }
}
