package com.tradingbot.model;

import java.time.LocalDate;

public record EquityPoint(LocalDate date, double equity) {
}
