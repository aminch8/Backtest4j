package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class MultipleMaStrength extends Strength{

    private EMAIndicator shorterEMA;
    private EMAIndicator mediumEMA;
    private EMAIndicator longerEMA;

    public MultipleMaStrength(BarSeries barSeries) {
        super(barSeries);
        shorterEMA = new EMAIndicator(new ClosePriceIndicator(barSeries),10);
        mediumEMA = new EMAIndicator(new ClosePriceIndicator(barSeries),20);
        longerEMA = new EMAIndicator(new ClosePriceIndicator(barSeries),50);
    }

    @Override
    Num getStrength() {
        if (
                shorterEMA.getValue(getIndex()).isGreaterThan(mediumEMA.getValue(getIndex()))
                &&
                        mediumEMA.getValue(getIndex()).isGreaterThan(longerEMA.getValue(getIndex()))
        ){
            return NumUtil.getNum(1);
        }else if (shorterEMA.getValue(getIndex()).isGreaterThan(mediumEMA.getValue(getIndex())) ||   mediumEMA.getValue(getIndex()).isGreaterThan(longerEMA.getValue(getIndex()))){
            return NumUtil.getNum(0.33);
        }
        return NumUtil.getNum(0);
    }
}
