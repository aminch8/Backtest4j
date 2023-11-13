package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.Num;

public class EMANormalizedIndicator extends CachedIndicator<Num> {


    private EMAIndicator emaIndicator;
    private LowestValueIndicator lowestValueIndicator;
    private HighestValueIndicator highestValueIndicator;
    private int lookBackPeriod;


    public EMANormalizedIndicator(BarSeries series, int maPeriod) {
        super(series);
        emaIndicator = new EMAIndicator(new ClosePriceIndicator(series),maPeriod);
        lowestValueIndicator = new LowestValueIndicator(emaIndicator,maPeriod);
        highestValueIndicator = new HighestValueIndicator(emaIndicator,maPeriod);
    }

    @Override
    protected Num calculate(int index) {
        return emaIndicator.getValue(index).minus(lowestValueIndicator.getValue(index)).dividedBy(highestValueIndicator.getValue(index).minus(lowestValueIndicator.getValue(index)))
                .minus(NumUtil.getNum(0.5)).multipliedBy(NumUtil.getNum(2));
    }
}
