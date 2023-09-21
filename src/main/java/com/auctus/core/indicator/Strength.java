package com.auctus.core.indicator;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

public abstract class Strength {

    public Strength(BarSeries barSeries) {
        this.barSeries = barSeries;
    }
    private BarSeries barSeries;
    abstract Num getStrength();
    protected int getIndex(){
        return barSeries.getBarCount()-1;
    }
}
