package com.auctus.core.domains;

import com.auctus.core.domains.enums.TimeFrame;
import org.ta4j.core.BarSeries;

public abstract class BarSeriesProvider {

    private BarSeries oneMinuteBarSeries;
    private BarSeries fiveMinuteBarSeries;
    private BarSeries fifteenMinutesBarSeries;
    private BarSeries thirtyMinutesBarSeries;
    private BarSeries oneHourBarSeries;
    private BarSeries twoHourBarSeries;
    private BarSeries fourHourBarSeries;
    private BarSeries dailyBarSeries;
    private BarSeries weeklyBarSeries;
    private BarSeries monthlyBarSeries;

    private TimeFrame timeFrame;
    private long maximumBars=0;

    public BarSeriesProvider(BarSeries barSeries, TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
        this.maximumBars = barSeries.getMaximumBarCount();
        switch (timeFrame){
            case M1:
                this.oneMinuteBarSeries = barSeries;
            case M5:
                this.fiveMinuteBarSeries = barSeries;
            case M15:
                this.fifteenMinutesBarSeries = barSeries;
            case M30:
                this.thirtyMinutesBarSeries = barSeries;
            case H1:
                this.oneHourBarSeries = barSeries;
            case H2:
                this.twoHourBarSeries = barSeries;
            case H4:
                this.fourHourBarSeries = barSeries;
            case D1:
                this.dailyBarSeries = barSeries;
            case W1:
                this.weeklyBarSeries = barSeries;
            case Mo:
                this.monthlyBarSeries = barSeries;
            default:
                throw new IllegalStateException("Wrong Timeframe given for chosen bar series");
        }
    }

    public void tick(){

    }
}
