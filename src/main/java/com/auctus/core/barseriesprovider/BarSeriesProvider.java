package com.auctus.core.barseriesprovider;

import com.auctus.core.domains.enums.TimeFrame;
import org.ta4j.core.BarSeries;

public class BarSeriesProvider extends BaseBarSeriesProvider {

    public BarSeriesProvider(BarSeries barSeries, TimeFrame timeFrame, String symbol) {
        super(barSeries, timeFrame, symbol);
    }

    
}
