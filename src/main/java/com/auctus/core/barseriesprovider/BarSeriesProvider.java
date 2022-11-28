package com.auctus.core.barseriesprovider;

import com.auctus.core.barseriesprovider.AbstractBarSeriesProvider;
import com.auctus.core.domains.enums.TimeFrame;
import org.ta4j.core.BarSeries;

public class BarSeriesProvider extends AbstractBarSeriesProvider {

    public BarSeriesProvider(BarSeries barSeries, TimeFrame timeFrame) {
        super(barSeries, timeFrame);
    }

}
