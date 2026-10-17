package com.tradingbot.data;

import com.tradingbot.model.Candle;
import com.tradingbot.model.PriceSeries;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CsvCandleWriter {

    private CsvCandleWriter() {
    }

    public static void write(PriceSeries series, Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write("Date,Open,High,Low,Close,Volume");
                writer.newLine();
                for (Candle candle : series.candles()) {
                    writer.write(String.format(Locale.ROOT, "%s,%.2f,%.2f,%.2f,%.2f,%.0f",
                            candle.date(), candle.open(), candle.high(), candle.low(), candle.close(),
                            candle.volume()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + path, e);
        }
    }
}
