package com.tradingbot;

import com.tradingbot.cli.CliOptions;
import com.tradingbot.data.CsvCandleLoader;
import com.tradingbot.data.CsvCandleWriter;
import com.tradingbot.data.SyntheticMarketGenerator;
import com.tradingbot.engine.BacktestEngine;
import com.tradingbot.engine.BacktestResult;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.optimization.RankingCriterion;
import com.tradingbot.optimization.StrategyOptimizer;
import com.tradingbot.optimization.WalkForwardResult;
import com.tradingbot.report.ConsoleReport;
import com.tradingbot.report.CsvReportWriter;
import com.tradingbot.strategy.Strategy;
import com.tradingbot.strategy.StrategyFactory;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class TradingBotApplication {

    private static final int[] FAST_PERIODS = {5, 10, 15, 20, 30, 40};
    private static final int[] SLOW_PERIODS = {30, 50, 75, 100, 150, 200};
    private static final double TRAIN_RATIO = 0.7;

    private final PrintStream out;
    private final ConsoleReport report;

    public TradingBotApplication(PrintStream out) {
        this.out = out;
        this.report = new ConsoleReport(out);
    }

    public static void main(String[] args) {
        try {
            new TradingBotApplication(System.out).run(CliOptions.parse(args));
        } catch (IllegalArgumentException | UncheckedIOException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            CliOptions.printUsage(System.err);
            System.exit(1);
        }
    }

    public void run(CliOptions options) {
        if (options.mode() == CliOptions.Mode.HELP) {
            CliOptions.printUsage(out);
            return;
        }

        PriceSeries series = loadSeries(options);
        if (options.exportPath() != null) {
            CsvCandleWriter.write(series, options.exportPath());
            out.println("Price series saved to " + options.exportPath());
        }

        BacktestEngine engine = new BacktestEngine(options.config());
        switch (options.mode()) {
            case COMPARE -> compare(engine, series);
            case OPTIMIZE -> optimize(engine, series, options.criterion());
            default -> single(engine, series, StrategyFactory.create(options.strategyId()), options.outputDirectory());
        }
    }

    private PriceSeries loadSeries(CliOptions options) {
        if (options.dataPath() != null) {
            return CsvCandleLoader.load(options.dataPath(), options.symbol());
        }
        return new SyntheticMarketGenerator(options.seed())
                .generate(options.symbol(), LocalDate.of(2021, 1, 4), options.syntheticDays(), 100.0);
    }

    private void single(BacktestEngine engine, PriceSeries series, Strategy strategy, Path outputDirectory) {
        BacktestResult result = engine.run(series, strategy);
        report.printSummary(result);
        report.printEquityChart(result);
        report.printTrades(result, 10);

        if (outputDirectory != null) {
            CsvReportWriter writer = new CsvReportWriter(outputDirectory);
            Path trades = writer.writeTrades(result);
            Path equity = writer.writeEquityCurve(result);
            out.println();
            out.println("Reports written to:");
            out.println("  " + trades);
            out.println("  " + equity);
        }
    }

    private void compare(BacktestEngine engine, PriceSeries series) {
        List<BacktestResult> results = StrategyFactory.allPresets().stream()
                .map(strategy -> engine.run(series, strategy))
                .toList();
        report.printComparison(results);
    }

    private void optimize(BacktestEngine engine, PriceSeries series, RankingCriterion criterion) {
        List<Strategy> candidates = StrategyFactory.movingAverageGrid(FAST_PERIODS, SLOW_PERIODS);
        WalkForwardResult walkForward = new StrategyOptimizer(engine)
                .walkForward(series, candidates, criterion, TRAIN_RATIO);

        report.printRanking("In-sample ranking by " + criterion.name().toLowerCase()
                + " (" + candidates.size() + " combinations, first "
                + Math.round(TRAIN_RATIO * 100) + "% of data)", walkForward.inSampleRanking(), 10);
        out.println();
        out.println("  Out-of-sample validation of the best combination");
        report.printSummary(walkForward.outOfSample());
    }
}
