package com.tradingbot.optimization;

import com.tradingbot.data.SyntheticMarketGenerator;
import com.tradingbot.engine.BacktestConfig;
import com.tradingbot.engine.BacktestEngine;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.strategy.Strategy;
import com.tradingbot.strategy.StrategyFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyOptimizerTest {

    private final PriceSeries series = new SyntheticMarketGenerator(11)
            .generate("OPT", LocalDate.of(2020, 1, 1), 600, 100);
    private final StrategyOptimizer optimizer = new StrategyOptimizer(new BacktestEngine(BacktestConfig.defaults()));
    private final List<Strategy> candidates = StrategyFactory.movingAverageGrid(new int[]{5, 10, 20}, new int[]{30, 60});

    @Test
    void rankingIsSortedBestFirst() {
        List<RankedResult> ranking = optimizer.rank(series, candidates, RankingCriterion.SHARPE);

        assertEquals(candidates.size(), ranking.size());
        for (int i = 1; i < ranking.size(); i++) {
            double previous = ranking.get(i - 1).result().metrics().sharpeRatio();
            double current = ranking.get(i).result().metrics().sharpeRatio();
            assertTrue(previous >= current);
        }
    }

    @Test
    void walkForwardValidatesTheBestCandidateOnUnseenData() {
        WalkForwardResult result = optimizer.walkForward(series, candidates, RankingCriterion.TOTAL_RETURN, 0.7);

        assertEquals(result.bestInSample().result().strategyName(), result.outOfSample().strategyName());
        assertEquals(180, result.outOfSample().equityCurve().size());
        assertTrue(result.outOfSample().startDate().isAfter(result.bestInSample().result().endDate()));
    }

    @Test
    void walkForwardRejectsInvalidSplits() {
        assertThrows(IllegalArgumentException.class,
                () -> optimizer.walkForward(series, candidates, RankingCriterion.SHARPE, 1.0));
    }

    @Test
    void criterionIsParsedFromCommandLineNames() {
        assertEquals(RankingCriterion.TOTAL_RETURN, RankingCriterion.parse("total-return"));
        assertThrows(IllegalArgumentException.class, () -> RankingCriterion.parse("luck"));
    }
}
