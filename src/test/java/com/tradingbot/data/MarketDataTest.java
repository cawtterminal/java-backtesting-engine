package com.tradingbot.data;

import com.tradingbot.model.Candle;
import com.tradingbot.model.PriceSeries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketDataTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsCsvSortedByDateWithCaseInsensitiveHeaders() throws IOException {
        Path file = write("""
                DATE,OPEN,HIGH,LOW,CLOSE,VOLUME
                2024-01-03,102,104,101,103,1500
                2024-01-02,100,103,99,102,1200
                """);

        PriceSeries series = CsvCandleLoader.load(file, "ABC");

        assertEquals(2, series.size());
        assertEquals(LocalDate.of(2024, 1, 2), series.first().date());
        assertEquals(103.0, series.last().close());
        assertEquals("ABC", series.symbol());
    }

    @Test
    void volumeColumnIsOptional() throws IOException {
        Path file = write("""
                Date,Open,High,Low,Close
                2024-01-02,100,103,99,102
                """);

        assertEquals(0.0, CsvCandleLoader.load(file, "ABC").first().volume());
    }

    @Test
    void rejectsFilesWithMissingColumns() throws IOException {
        Path file = write("""
                Date,Open,High,Close
                2024-01-02,100,103,102
                """);

        assertThrows(IllegalArgumentException.class, () -> CsvCandleLoader.load(file, "ABC"));
    }

    @Test
    void rejectsMalformedRows() throws IOException {
        Path file = write("""
                Date,Open,High,Low,Close
                2024-01-02,100,abc,99,102
                """);

        assertThrows(IllegalArgumentException.class, () -> CsvCandleLoader.load(file, "ABC"));
    }

    @Test
    void writerAndLoaderRoundTrip() {
        PriceSeries original = new SyntheticMarketGenerator(7).generate("RT", LocalDate.of(2024, 1, 1), 50, 100);
        Path file = tempDir.resolve("roundtrip.csv");

        CsvCandleWriter.write(original, file);
        PriceSeries loaded = CsvCandleLoader.load(file, "RT");

        assertEquals(original.size(), loaded.size());
        assertEquals(original.last().close(), loaded.last().close(), 1e-9);
    }

    @Test
    void generatorIsDeterministicForTheSameSeed() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        PriceSeries first = new SyntheticMarketGenerator(42).generate("A", start, 200, 100);
        PriceSeries second = new SyntheticMarketGenerator(42).generate("A", start, 200, 100);

        assertEquals(first.candles(), second.candles());
    }

    @Test
    void generatorSkipsWeekends() {
        PriceSeries series = new SyntheticMarketGenerator(1).generate("A", LocalDate.of(2024, 1, 6), 30, 100);

        for (Candle candle : series.candles()) {
            DayOfWeek day = candle.date().getDayOfWeek();
            assertTrue(day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY);
        }
        assertEquals(LocalDate.of(2024, 1, 8), series.first().date());
    }

    @Test
    void candlesRejectInconsistentPrices() {
        LocalDate date = LocalDate.of(2024, 1, 2);

        assertThrows(IllegalArgumentException.class, () -> new Candle(date, 10, 9, 11, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Candle(date, 12, 11, 9, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Candle(date, 10, 11, 9, 10, -5));
    }

    private Path write(String content) throws IOException {
        Path file = tempDir.resolve("prices.csv");
        Files.writeString(file, content);
        return file;
    }
}
