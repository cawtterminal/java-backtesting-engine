package com.tradingbot.report;

import com.tradingbot.engine.BacktestResult;
import com.tradingbot.metrics.PerformanceMetrics;
import com.tradingbot.model.Trade;
import com.tradingbot.optimization.RankedResult;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

import static com.tradingbot.report.Formats.decimal;
import static com.tradingbot.report.Formats.money;
import static com.tradingbot.report.Formats.percent;
import static com.tradingbot.report.Formats.ratio;
import static com.tradingbot.report.Formats.signedPercent;

public final class ConsoleReport {

    private static final int WIDTH = 92;
    private static final String COMPARISON_ROW = "  %-38s %9s %9s %9s %7s %7s %6s %8s %6s%n";

    private final PrintStream out;

    public ConsoleReport(PrintStream out) {
        this.out = out;
    }

    public void printSummary(BacktestResult result) {
        PerformanceMetrics m = result.metrics();
        out.println("=".repeat(WIDTH));
        out.printf("  %s  |  %s  |  %s -> %s  (%d bars)%n", result.strategyName(), result.symbol(),
                result.startDate(), result.endDate(), result.equityCurve().size());
        out.println("=".repeat(WIDTH));
        row("Initial capital", money(result.config().initialCapital()), "Final equity", money(result.finalEquity()));
        row("Total return", signedPercent(m.totalReturn()), "Buy & Hold", signedPercent(m.buyAndHoldReturn()));
        row("CAGR", signedPercent(m.annualizedReturn()), "Volatility", percent(m.annualizedVolatility()));
        row("Max drawdown", signedPercent(-m.maxDrawdown()), "Exposure", percent(m.exposure()));
        row("Sharpe ratio", ratio(m.sharpeRatio()), "Sortino ratio", ratio(m.sortinoRatio()));
        row("Calmar ratio", ratio(m.calmarRatio()), "Alpha vs B&H", signedPercent(m.excessReturnVsBuyAndHold()));
        out.println("-".repeat(WIDTH));
        row("Trades", String.valueOf(m.totalTrades()), "Win rate", percent(m.winRate()));
        row("Profit factor", ratio(m.profitFactor()), "Avg trade", signedPercent(m.averageTradeReturn()));
        row("Best trade", signedPercent(m.bestTradeReturn()), "Worst trade", signedPercent(m.worstTradeReturn()));
        row("Avg holding", decimal(m.averageHoldingDays(), 1) + " days", "", "");
        out.println("=".repeat(WIDTH));
    }

    public void printEquityChart(BacktestResult result) {
        out.println();
        out.println("  Equity curve");
        out.print(AsciiChart.render(result.equityValues(), 76, 14));
    }

    public void printTrades(BacktestResult result, int limit) {
        List<Trade> trades = result.trades();
        if (trades.isEmpty()) {
            out.println();
            out.println("  No trades were executed.");
            return;
        }
        int from = Math.max(0, trades.size() - limit);
        out.println();
        out.printf("  Last %d of %d trades%n", trades.size() - from, trades.size());
        out.printf("  %-12s %-12s %11s %11s %12s %9s  %s%n",
                "Entry", "Exit", "Entry px", "Exit px", "Net P&L", "Return", "Exit reason");
        out.println("  " + "-".repeat(WIDTH - 2));
        for (Trade trade : trades.subList(from, trades.size())) {
            out.printf(Locale.US, "  %-12s %-12s %11.2f %11.2f %12s %9s  %s%n",
                    trade.entryDate(), trade.exitDate(), trade.entryPrice(), trade.exitPrice(),
                    money(trade.netPnl()), signedPercent(trade.returnPct()), trade.exitReason());
        }
    }

    public void printComparison(List<BacktestResult> results) {
        if (results.isEmpty()) {
            return;
        }
        BacktestResult first = results.get(0);
        out.println("=".repeat(WIDTH));
        out.printf("  Strategy comparison  |  %s  |  %s -> %s%n", first.symbol(), first.startDate(), first.endDate());
        out.println("=".repeat(WIDTH));
        out.printf(COMPARISON_ROW, "Strategy", "Return", "CAGR", "Max DD", "Sharpe", "Sortino", "Trades",
                "Win rate", "PF");
        out.println("  " + "-".repeat(WIDTH - 2));
        for (BacktestResult result : results) {
            comparisonRow(result.strategyName(), result.metrics());
        }
        out.println("  " + "-".repeat(WIDTH - 2));
        out.printf(COMPARISON_ROW, "Buy & Hold (benchmark)", signedPercent(first.metrics().buyAndHoldReturn()),
                "", "", "", "", "", "", "");
        out.println("=".repeat(WIDTH));
    }

    public void printRanking(String title, List<RankedResult> ranking, int limit) {
        out.println("=".repeat(WIDTH));
        out.println("  " + title);
        out.println("=".repeat(WIDTH));
        out.printf(COMPARISON_ROW, "Strategy", "Return", "CAGR", "Max DD", "Sharpe", "Sortino", "Trades",
                "Win rate", "PF");
        out.println("  " + "-".repeat(WIDTH - 2));
        ranking.stream()
                .limit(limit)
                .forEach(ranked -> comparisonRow(ranked.result().strategyName(), ranked.result().metrics()));
        out.println("=".repeat(WIDTH));
    }

    private void comparisonRow(String name, PerformanceMetrics m) {
        out.printf(COMPARISON_ROW, name,
                signedPercent(m.totalReturn()),
                signedPercent(m.annualizedReturn()),
                signedPercent(-m.maxDrawdown()),
                ratio(m.sharpeRatio()),
                ratio(m.sortinoRatio()),
                m.totalTrades(),
                percent(m.winRate()),
                ratio(m.profitFactor()));
    }

    private void row(String leftLabel, String leftValue, String rightLabel, String rightValue) {
        out.printf("  %-18s %-22s %-18s %s%n", leftLabel, leftValue, rightLabel, rightValue);
    }
}
