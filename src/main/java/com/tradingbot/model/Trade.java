package com.tradingbot.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Trade(
        LocalDate entryDate,
        double entryPrice,
        LocalDate exitDate,
        double exitPrice,
        double quantity,
        double commissions,
        ExitReason exitReason) {

    public double grossPnl() {
        return (exitPrice - entryPrice) * quantity;
    }

    public double netPnl() {
        return grossPnl() - commissions;
    }

    public double returnPct() {
        return netPnl() / (entryPrice * quantity);
    }

    public boolean isWinner() {
        return netPnl() > 0;
    }

    public long holdingDays() {
        return ChronoUnit.DAYS.between(entryDate, exitDate);
    }
}
