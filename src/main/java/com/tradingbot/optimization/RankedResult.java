package com.tradingbot.optimization;

import com.tradingbot.engine.BacktestResult;
import com.tradingbot.strategy.Strategy;

public record RankedResult(Strategy strategy, BacktestResult result) {
}
