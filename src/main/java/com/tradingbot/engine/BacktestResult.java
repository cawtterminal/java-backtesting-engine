package com.tradingbot.engine;

import com.tradingbot.metrics.PerformanceMetrics;
import com.tradingbot.model.EquityPoint;
import com.tradingbot.model.Trade;

import java.time.LocalDate;
import java.util.List;

public record BacktestResult(
        String strategyName,
        String symbol,
        BacktestConfig config,
        List<EquityPoint> equityCurve,
        List<Trade> trades,
        PerformanceMetrics metrics) {

    public BacktestResult {
        equityCurve = List.copyOf(equityCurve);
        trades = List.copyOf(trades);
    }

    public double finalEquity() {
        return equityCurve.get(equityCurve.size() - 1).equity();
    }

    public LocalDate startDate() {
        return equityCurve.get(0).date();
    }

    public LocalDate endDate() {
        return equityCurve.get(equityCurve.size() - 1).date();
    }

    public double[] equityValues() {
        return equityCurve.stream().mapToDouble(EquityPoint::equity).toArray();
    }
}
