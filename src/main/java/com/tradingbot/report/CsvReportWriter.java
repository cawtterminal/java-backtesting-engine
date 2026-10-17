package com.tradingbot.report;

import com.tradingbot.engine.BacktestResult;
import com.tradingbot.model.EquityPoint;
import com.tradingbot.model.Trade;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CsvReportWriter {

    private final Path outputDirectory;

    public CsvReportWriter(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Path writeTrades(BacktestResult result) {
        Path file = outputDirectory.resolve(fileName(result, "trades"));
        try (BufferedWriter writer = open(file)) {
            writer.write("entry_date,entry_price,exit_date,exit_price,quantity,commissions,net_pnl,return_pct,exit_reason");
            writer.newLine();
            for (Trade trade : result.trades()) {
                writer.write(String.format(Locale.ROOT, "%s,%.4f,%s,%.4f,%.6f,%.4f,%.4f,%.6f,%s",
                        trade.entryDate(), trade.entryPrice(), trade.exitDate(), trade.exitPrice(),
                        trade.quantity(), trade.commissions(), trade.netPnl(), trade.returnPct(),
                        trade.exitReason()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
        return file;
    }

    public Path writeEquityCurve(BacktestResult result) {
        Path file = outputDirectory.resolve(fileName(result, "equity"));
        try (BufferedWriter writer = open(file)) {
            writer.write("date,equity");
            writer.newLine();
            for (EquityPoint point : result.equityCurve()) {
                writer.write(String.format(Locale.ROOT, "%s,%.2f", point.date(), point.equity()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
        return file;
    }

    private BufferedWriter open(Path file) throws IOException {
        Files.createDirectories(outputDirectory);
        return Files.newBufferedWriter(file, StandardCharsets.UTF_8);
    }

    private static String fileName(BacktestResult result, String suffix) {
        String slug = result.strategyName()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return result.symbol().toLowerCase(Locale.ROOT) + "_" + slug + "_" + suffix + ".csv";
    }
}
