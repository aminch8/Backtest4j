package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

public class GreenCandlesStrength extends Strength {

    private BarSeries barSeries;
    private int candlesCheck;

    public GreenCandlesStrength(BarSeries barSeries,int candlesCheck) {
        super(barSeries);
        this.barSeries=barSeries;
        this.candlesCheck=candlesCheck;
    }

    @Override
    Num getStrength() {
        int totalCount = 0;
        for (int i = getIndex();i > getIndex()-5;i--){
            if (barSeries.getBar(i).isBullish()){
                totalCount++;
            }
        }
        return NumUtil.getNum(totalCount).dividedBy(NumUtil.getNum(candlesCheck));
    }
}
