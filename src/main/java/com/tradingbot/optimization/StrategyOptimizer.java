package com.tradingbot.optimization;

import com.tradingbot.engine.BacktestEngine;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.strategy.Strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class StrategyOptimizer {

    private final BacktestEngine engine;

    public StrategyOptimizer(BacktestEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    public List<RankedResult> rank(PriceSeries series, List<Strategy> candidates, RankingCriterion criterion) {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("At least one candidate strategy is required");
        }
        return candidates.parallelStream()
                .map(strategy -> new RankedResult(strategy, engine.run(series, strategy)))
                .sorted(Comparator.comparing(ranked -> ranked.result().metrics(), criterion.bestFirst()))
                .toList();
    }

    public WalkForwardResult walkForward(PriceSeries series, List<Strategy> candidates,
                                         RankingCriterion criterion, double trainRatio) {
        if (trainRatio <= 0 || trainRatio >= 1) {
            throw new IllegalArgumentException("trainRatio must be between 0 and 1");
        }
        int split = (int) Math.round(series.size() * trainRatio);
        if (split < 2 || series.size() - split < 2) {
            throw new IllegalArgumentException("Not enough data to split into train and test periods");
        }

        PriceSeries train = series.slice(0, split);
        PriceSeries test = series.slice(split, series.size());

        List<RankedResult> ranking = rank(train, candidates, criterion);
        Strategy best = ranking.get(0).strategy();
        return new WalkForwardResult(ranking, engine.run(test, best));
    }
}
