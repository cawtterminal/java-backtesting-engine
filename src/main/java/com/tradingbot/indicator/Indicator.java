package com.tradingbot.indicator;

import com.tradingbot.model.PriceSeries;

public interface Indicator<T> {

    T calculate(PriceSeries series);

    String name();
}
