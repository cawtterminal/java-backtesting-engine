package com.tradingbot.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CliOptionsTest {

    @Test
    void usesSensibleDefaults() {
        CliOptions options = CliOptions.parse(new String[0]);

        assertEquals(CliOptions.Mode.SINGLE, options.mode());
        assertEquals("sma-cross", options.strategyId());
        assertEquals("SIM", options.symbol());
        assertEquals(10_000.0, options.config().initialCapital());
    }

    @Test
    void parsesExecutionParameters() {
        CliOptions options = CliOptions.parse(new String[]{
                "--data", "data/btc.csv", "--strategy", "macd", "--capital", "5000",
                "--stop-loss", "0.05", "--take-profit", "0.2", "--compare"});

        assertEquals(CliOptions.Mode.COMPARE, options.mode());
        assertEquals(Path.of("data/btc.csv"), options.dataPath());
        assertEquals("BTC", options.symbol());
        assertEquals("macd", options.strategyId());
        assertEquals(5_000.0, options.config().initialCapital());
        assertEquals(0.05, options.config().stopLossPct());
        assertEquals(0.2, options.config().takeProfitPct());
    }

    @Test
    void helpTakesPrecedence() {
        assertEquals(CliOptions.Mode.HELP, CliOptions.parse(new String[]{"--compare", "--help"}).mode());
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[]{"--unknown"}));
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[]{"--capital"}));
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[]{"--capital", "lots"}));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[]{"--compare", "--optimize"}));
    }
}
