package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class MAStrength extends Strength {

    private EMAIndicator shorterMa;
    private EMAIndicator longerMa;

    public MAStrength(BarSeries barSeries, int shorterMaPeriod, int longerMaPeriod) {
        super(barSeries);
        this.shorterMa = new EMAIndicator(new ClosePriceIndicator(barSeries),shorterMaPeriod);
        this.longerMa = new EMAIndicator(new ClosePriceIndicator(barSeries),longerMaPeriod);
    }

    public Num getStrength(){
        int index = shorterMa.getBarSeries().getBarCount()-1;
        Num shorterMaValue = shorterMa.getValue(index);
        Num longerMaValue = longerMa.getValue(index);
        if (shorterMaValue.isGreaterThan(longerMaValue)){
            return NumUtil.getNum(1);
        }else {
            return NumUtil.getNum(0);
        }
    }

}
