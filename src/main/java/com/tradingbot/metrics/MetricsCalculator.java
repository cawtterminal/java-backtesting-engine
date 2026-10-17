package com.tradingbot.metrics;

import com.tradingbot.model.EquityPoint;
import com.tradingbot.model.Trade;

import java.util.List;

public final class MetricsCalculator {

    private static final int DEFAULT_PERIODS_PER_YEAR = 252;

    private final int periodsPerYear;
    private final double annualRiskFreeRate;

    public MetricsCalculator() {
        this(DEFAULT_PERIODS_PER_YEAR, 0.0);
    }

    public MetricsCalculator(int periodsPerYear, double annualRiskFreeRate) {
        if (periodsPerYear <= 0) {
            throw new IllegalArgumentException("periodsPerYear must be positive");
        }
        this.periodsPerYear = periodsPerYear;
        this.annualRiskFreeRate = annualRiskFreeRate;
    }

    public PerformanceMetrics calculate(double initialCapital, List<EquityPoint> equityCurve, List<Trade> trades,
                                        double buyAndHoldReturn, double exposure) {
        if (equityCurve.isEmpty()) {
            throw new IllegalArgumentException("Equity curve is empty");
        }
        double[] equity = new double[equityCurve.size() + 1];
        equity[0] = initialCapital;
        for (int i = 0; i < equityCurve.size(); i++) {
            equity[i + 1] = equityCurve.get(i).equity();
        }

        double[] returns = periodReturns(equity);
        double finalEquity = equity[equity.length - 1];
        double totalReturn = finalEquity / initialCapital - 1;
        double annualizedReturn = annualize(totalReturn, equityCurve.size());
        double maxDrawdown = maxDrawdown(equity);
        double volatility = standardDeviation(returns) * Math.sqrt(periodsPerYear);

        TradeStats tradeStats = TradeStats.of(trades);

        return new PerformanceMetrics(
                totalReturn,
                annualizedReturn,
                volatility,
                maxDrawdown,
                sharpeRatio(returns),
                sortinoRatio(returns),
                maxDrawdown > 0 ? annualizedReturn / maxDrawdown : 0,
                trades.size(),
                tradeStats.winRate(),
                tradeStats.profitFactor(),
                tradeStats.averageReturn(),
                tradeStats.bestReturn(),
                tradeStats.worstReturn(),
                tradeStats.averageHoldingDays(),
                exposure,
                buyAndHoldReturn);
    }

    public static double maxDrawdown(double[] equity) {
        double peak = equity[0];
        double worst = 0;
        for (double value : equity) {
            peak = Math.max(peak, value);
            worst = Math.max(worst, (peak - value) / peak);
        }
        return worst;
    }

    private double annualize(double totalReturn, int periods) {
        if (totalReturn <= -1) {
            return -1;
        }
        double years = (double) periods / periodsPerYear;
        return Math.pow(1 + totalReturn, 1 / years) - 1;
    }

    private double sharpeRatio(double[] returns) {
        double deviation = standardDeviation(returns);
        if (deviation == 0) {
            return 0;
        }
        return (mean(returns) - periodRiskFreeRate()) / deviation * Math.sqrt(periodsPerYear);
    }

    private double sortinoRatio(double[] returns) {
        double target = periodRiskFreeRate();
        double downsideSquares = 0;
        for (double r : returns) {
            double shortfall = Math.min(r - target, 0);
            downsideSquares += shortfall * shortfall;
        }
        double downsideDeviation = Math.sqrt(downsideSquares / returns.length);
        if (downsideDeviation == 0) {
            return 0;
        }
        return (mean(returns) - target) / downsideDeviation * Math.sqrt(periodsPerYear);
    }

    private double periodRiskFreeRate() {
        return Math.pow(1 + annualRiskFreeRate, 1.0 / periodsPerYear) - 1;
    }

    private static double[] periodReturns(double[] equity) {
        double[] returns = new double[equity.length - 1];
        for (int i = 1; i < equity.length; i++) {
            returns[i - 1] = equity[i] / equity[i - 1] - 1;
        }
        return returns;
    }

    private static double mean(double[] values) {
        if (values.length == 0) {
            return 0;
        }
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static double standardDeviation(double[] values) {
        if (values.length < 2) {
            return 0;
        }
        double average = mean(values);
        double sumOfSquares = 0;
        for (double value : values) {
            sumOfSquares += (value - average) * (value - average);
        }
        return Math.sqrt(sumOfSquares / (values.length - 1));
    }

    private record TradeStats(double winRate, double profitFactor, double averageReturn,
                              double bestReturn, double worstReturn, double averageHoldingDays) {

        static TradeStats of(List<Trade> trades) {
            if (trades.isEmpty()) {
                return new TradeStats(0, 0, 0, 0, 0, 0);
            }
            double grossProfit = 0;
            double grossLoss = 0;
            int winners = 0;
            double returnSum = 0;
            double best = Double.NEGATIVE_INFINITY;
            double worst = Double.POSITIVE_INFINITY;
            double holdingDays = 0;

            for (Trade trade : trades) {
                double pnl = trade.netPnl();
                if (pnl > 0) {
                    grossProfit += pnl;
                    winners++;
                } else {
                    grossLoss -= pnl;
                }
                double tradeReturn = trade.returnPct();
                returnSum += tradeReturn;
                best = Math.max(best, tradeReturn);
                worst = Math.min(worst, tradeReturn);
                holdingDays += trade.holdingDays();
            }

            double profitFactor = grossLoss == 0
                    ? (grossProfit > 0 ? Double.POSITIVE_INFINITY : 0)
                    : grossProfit / grossLoss;

            int count = trades.size();
            return new TradeStats((double) winners / count, profitFactor, returnSum / count,
                    best, worst, holdingDays / count);
        }
    }
}
