package com.tradingbot.cli;

import com.tradingbot.engine.BacktestConfig;
import com.tradingbot.optimization.RankingCriterion;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public record CliOptions(
        Mode mode,
        Path dataPath,
        String symbol,
        String strategyId,
        int syntheticDays,
        long seed,
        BacktestConfig config,
        RankingCriterion criterion,
        Path outputDirectory,
        Path exportPath) {

    public enum Mode {
        SINGLE,
        COMPARE,
        OPTIMIZE,
        HELP
    }

    private static final Set<String> VALUE_OPTIONS = Set.of(
            "--data", "--symbol", "--strategy", "--days", "--seed", "--capital", "--commission",
            "--slippage", "--position-size", "--stop-loss", "--take-profit", "--rank-by", "--output",
            "--export-data");

    private static final Set<String> FLAG_OPTIONS = Set.of("--compare", "--optimize", "--help", "-h");

    public static CliOptions parse(String[] args) {
        Map<String, String> values = new HashMap<>();
        Set<String> flags = new HashSet<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (FLAG_OPTIONS.contains(arg)) {
                flags.add(arg);
            } else if (VALUE_OPTIONS.contains(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for " + arg);
                }
                values.put(arg, args[++i]);
            } else {
                throw new IllegalArgumentException("Unknown option: " + arg);
            }
        }
        Mode mode = resolveMode(flags);

        BacktestConfig config = BacktestConfig.defaults()
                .withInitialCapital(number(values, "--capital", 10_000))
                .withCommissionRate(number(values, "--commission", 0.001))
                .withSlippageRate(number(values, "--slippage", 0.0005))
                .withPositionSizing(number(values, "--position-size", 1.0))
                .withStopLoss(number(values, "--stop-loss", 0))
                .withTakeProfit(number(values, "--take-profit", 0));

        Path dataPath = values.containsKey("--data") ? Path.of(values.get("--data")) : null;
        String defaultSymbol = dataPath == null ? "SIM" : symbolFromFile(dataPath);

        return new CliOptions(
                mode,
                dataPath,
                values.getOrDefault("--symbol", defaultSymbol),
                values.getOrDefault("--strategy", "sma-cross"),
                (int) number(values, "--days", 1000),
                (long) number(values, "--seed", 42),
                config,
                RankingCriterion.parse(values.getOrDefault("--rank-by", "sharpe")),
                values.containsKey("--output") ? Path.of(values.get("--output")) : null,
                values.containsKey("--export-data") ? Path.of(values.get("--export-data")) : null);
    }

    public static void printUsage(PrintStream out) {
        out.println("""
                Usage: java -jar trading-bot-simulator.jar [options]

                Data source (synthetic market data is used when --data is omitted):
                  --data <file.csv>        OHLCV file with columns Date,Open,High,Low,Close[,Volume]
                  --symbol <name>          Symbol shown in reports
                  --days <n>               Synthetic trading days (default 1000)
                  --seed <n>               Synthetic random seed (default 42)
                  --export-data <file>     Save the loaded or generated series as CSV

                Modes:
                  --strategy <id>          sma-cross | ema-cross | rsi | macd | bollinger | rsi-trend
                  --compare                Run every preset strategy and print a comparison table
                  --optimize               Grid search SMA cross with walk-forward validation (70/30)
                  --rank-by <metric>       sharpe | sortino | calmar | total-return (default sharpe)

                Execution model:
                  --capital <amount>       Initial capital (default 10000)
                  --commission <rate>      Commission per side, 0.001 = 0.1% (default 0.001)
                  --slippage <rate>        Slippage per fill (default 0.0005)
                  --position-size <frac>   Fraction of cash allocated per trade (default 1.0)
                  --stop-loss <frac>       Stop loss from entry, 0.05 = 5% (default off)
                  --take-profit <frac>     Take profit from entry, 0.15 = 15% (default off)

                Output:
                  --output <dir>           Write trades and equity curve CSV files
                  --help                   Show this message
                """);
    }

    private static Mode resolveMode(Set<String> flags) {
        if (flags.contains("--help") || flags.contains("-h")) {
            return Mode.HELP;
        }
        boolean compare = flags.contains("--compare");
        boolean optimize = flags.contains("--optimize");
        if (compare && optimize) {
            throw new IllegalArgumentException("--compare and --optimize cannot be combined");
        }
        if (compare) {
            return Mode.COMPARE;
        }
        return optimize ? Mode.OPTIMIZE : Mode.SINGLE;
    }

    private static double number(Map<String, String> values, String key, double defaultValue) {
        String raw = values.get(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for " + key + ": " + raw);
        }
    }

    private static String symbolFromFile(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return (dot > 0 ? fileName.substring(0, dot) : fileName).toUpperCase(Locale.ROOT);
    }
}
