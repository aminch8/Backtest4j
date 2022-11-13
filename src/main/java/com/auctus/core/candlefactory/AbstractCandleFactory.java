package com.auctus.core.candlefactory;

import org.ta4j.core.BarSeries;

abstract class AbstractCandleFactory {
    abstract BarSeries getBarSeries();
}
