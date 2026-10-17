package com.tradingbot.optimization;

import com.tradingbot.engine.BacktestResult;

import java.util.List;

public record WalkForwardResult(List<RankedResult> inSampleRanking, BacktestResult outOfSample) {

    public WalkForwardResult {
        inSampleRanking = List.copyOf(inSampleRanking);
    }

    public RankedResult bestInSample() {
        return inSampleRanking.get(0);
    }
}
