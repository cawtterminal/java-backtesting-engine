package com.tradingbot.engine;

import com.tradingbot.metrics.MetricsCalculator;
import com.tradingbot.metrics.PerformanceMetrics;
import com.tradingbot.model.Candle;
import com.tradingbot.model.EquityPoint;
import com.tradingbot.model.ExitReason;
import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;
import com.tradingbot.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BacktestEngine {

    private final BacktestConfig config;
    private final MetricsCalculator metricsCalculator;

    public BacktestEngine(BacktestConfig config) {
        this(config, new MetricsCalculator());
    }

    public BacktestEngine(BacktestConfig config, MetricsCalculator metricsCalculator) {
        this.config = Objects.requireNonNull(config, "config");
        this.metricsCalculator = Objects.requireNonNull(metricsCalculator, "metricsCalculator");
    }

    public BacktestConfig config() {
        return config;
    }

    public BacktestResult run(PriceSeries series, Strategy strategy) {
        Signal[] signals = strategy.generateSignals(series);
        if (signals.length != series.size()) {
            throw new IllegalStateException(strategy.name() + " produced " + signals.length
                    + " signals for " + series.size() + " candles");
        }

        Portfolio portfolio = new Portfolio(config.initialCapital(), config.commissionRate());
        List<EquityPoint> equityCurve = new ArrayList<>(series.size());
        Signal pendingSignal = Signal.HOLD;
        int barsInMarket = 0;

        for (int i = 0; i < series.size(); i++) {
            Candle candle = series.get(i);
            executePendingSignal(portfolio, candle, pendingSignal);
            applyProtectiveExits(portfolio, candle);

            if (portfolio.hasPosition()) {
                barsInMarket++;
            }
            equityCurve.add(new EquityPoint(candle.date(), portfolio.equity(candle.close())));
            pendingSignal = signals[i];
        }

        Candle lastCandle = series.last();
        if (portfolio.hasPosition()) {
            portfolio.close(lastCandle.date(), sellFill(lastCandle.close()), ExitReason.END_OF_DATA);
            equityCurve.set(equityCurve.size() - 1, new EquityPoint(lastCandle.date(), portfolio.equity(0)));
        }

        double buyAndHoldReturn = lastCandle.close() / series.first().open() - 1;
        double exposure = (double) barsInMarket / series.size();
        PerformanceMetrics metrics = metricsCalculator.calculate(
                config.initialCapital(), equityCurve, portfolio.closedTrades(), buyAndHoldReturn, exposure);

        return new BacktestResult(strategy.name(), series.symbol(), config, equityCurve,
                portfolio.closedTrades(), metrics);
    }

    private void executePendingSignal(Portfolio portfolio, Candle candle, Signal signal) {
        if (signal == Signal.BUY && !portfolio.hasPosition()) {
            portfolio.open(candle.date(), buyFill(candle.open()), config.positionSizing());
        } else if (signal == Signal.SELL && portfolio.hasPosition()) {
            portfolio.close(candle.date(), sellFill(candle.open()), ExitReason.SIGNAL);
        }
    }

    private void applyProtectiveExits(Portfolio portfolio, Candle candle) {
        if (!portfolio.hasPosition()) {
            return;
        }
        double entry = portfolio.entryPrice();

        if (config.hasStopLoss()) {
            double stopPrice = entry * (1 - config.stopLossPct());
            if (candle.low() <= stopPrice) {
                double triggered = Math.min(candle.open(), stopPrice);
                portfolio.close(candle.date(), sellFill(triggered), ExitReason.STOP_LOSS);
                return;
            }
        }

        if (config.hasTakeProfit()) {
            double targetPrice = entry * (1 + config.takeProfitPct());
            if (candle.high() >= targetPrice) {
                double triggered = Math.max(candle.open(), targetPrice);
                portfolio.close(candle.date(), sellFill(triggered), ExitReason.TAKE_PROFIT);
            }
        }
    }

    private double buyFill(double price) {
        return price * (1 + config.slippageRate());
    }

    private double sellFill(double price) {
        return price * (1 - config.slippageRate());
    }
}
