package com.tradingbot.engine;

import com.tradingbot.model.ExitReason;
import com.tradingbot.model.Trade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class Portfolio {

    private final double commissionRate;
    private final List<Trade> closedTrades = new ArrayList<>();
    private double cash;
    private OpenPosition position;

    Portfolio(double initialCash, double commissionRate) {
        this.cash = initialCash;
        this.commissionRate = commissionRate;
    }

    boolean hasPosition() {
        return position != null;
    }

    double entryPrice() {
        return position.entryPrice();
    }

    void open(LocalDate date, double price, double allocation) {
        if (hasPosition()) {
            throw new IllegalStateException("A position is already open");
        }
        double budget = cash * allocation;
        double quantity = budget / (price * (1 + commissionRate));
        if (quantity <= 0) {
            return;
        }
        double commission = quantity * price * commissionRate;
        cash -= quantity * price + commission;
        position = new OpenPosition(date, price, quantity, commission);
    }

    void close(LocalDate date, double price, ExitReason reason) {
        if (!hasPosition()) {
            throw new IllegalStateException("There is no open position to close");
        }
        double proceeds = position.quantity() * price;
        double commission = proceeds * commissionRate;
        cash += proceeds - commission;
        closedTrades.add(new Trade(
                position.entryDate(),
                position.entryPrice(),
                date,
                price,
                position.quantity(),
                position.entryCommission() + commission,
                reason));
        position = null;
    }

    double equity(double markPrice) {
        return hasPosition() ? cash + position.quantity() * markPrice : cash;
    }

    List<Trade> closedTrades() {
        return List.copyOf(closedTrades);
    }

    private record OpenPosition(LocalDate entryDate, double entryPrice, double quantity, double entryCommission) {
    }
}
