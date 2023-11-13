package com.auctus.core.indicator;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class IBSIndicator extends CachedIndicator<Num> {

    public IBSIndicator(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar currentBar = getBarSeries().getBar(index);
        Num range = currentBar.getHighPrice().minus(currentBar.getLowPrice()).abs();
        if (currentBar.isBullish()){
            return currentBar.getClosePrice().minus(currentBar.getLowPrice()).dividedBy(range);
        }else {
            return currentBar.getClosePrice().minus(currentBar.getHighPrice()).dividedBy(range);
        }
    }
}
