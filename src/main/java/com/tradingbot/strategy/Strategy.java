package com.tradingbot.strategy;

import com.tradingbot.model.PriceSeries;
import com.tradingbot.model.Signal;

public interface Strategy {

    String name();

    Signal[] generateSignals(PriceSeries series);
}
