package com.tradingbot.data;

import com.tradingbot.model.Candle;
import com.tradingbot.model.PriceSeries;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class CsvCandleLoader {

    private static final List<String> REQUIRED_COLUMNS = List.of("date", "open", "high", "low", "close");

    private CsvCandleLoader() {
    }

    public static PriceSeries load(Path path, String symbol) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + path, e);
        }
        if (lines.size() < 2) {
            throw new IllegalArgumentException("File " + path + " has no data rows");
        }

        List<String> header = Arrays.stream(stripBom(lines.get(0)).split(","))
                .map(column -> column.trim().toLowerCase(Locale.ROOT))
                .toList();
        for (String column : REQUIRED_COLUMNS) {
            if (!header.contains(column)) {
                throw new IllegalArgumentException("Missing column '" + column + "' in " + path);
            }
        }
        int dateIdx = header.indexOf("date");
        int openIdx = header.indexOf("open");
        int highIdx = header.indexOf("high");
        int lowIdx = header.indexOf("low");
        int closeIdx = header.indexOf("close");
        int volumeIdx = header.indexOf("volume");

        List<Candle> candles = new ArrayList<>();
        for (int lineNumber = 2; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split(",");
            try {
                candles.add(new Candle(
                        LocalDate.parse(fields[dateIdx].trim()),
                        parse(fields[openIdx]),
                        parse(fields[highIdx]),
                        parse(fields[lowIdx]),
                        parse(fields[closeIdx]),
                        volumeIdx >= 0 && volumeIdx < fields.length ? parse(fields[volumeIdx]) : 0));
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException | DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid row at line " + lineNumber + " of " + path, e);
            }
        }
        candles.sort(Comparator.comparing(Candle::date));
        return new PriceSeries(symbol, candles);
    }

    private static double parse(String value) {
        return Double.parseDouble(value.trim());
    }

    private static String stripBom(String line) {
        return line.startsWith("﻿") ? line.substring(1) : line;
    }
}
