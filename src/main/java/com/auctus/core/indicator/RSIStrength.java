package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class RSIStrength extends Strength {

    private RSIIndicator rsiIndicator;

    public RSIStrength(BarSeries barSeries, int rsiPeriod) {
        super(barSeries);
        this.rsiIndicator = new RSIIndicator(new ClosePriceIndicator(barSeries),rsiPeriod);
    }


    public Num getStrength(){
        if (rsiIndicator.getValue(getIndex()).isLessThan(NumUtil.getNum(30))){
            return NumUtil.getNum(0);
        }else if (rsiIndicator.getValue(getIndex()).isLessThan(NumUtil.getNum(50))){
            return NumUtil.getNum(0.33);
        }else if (rsiIndicator.getValue(getIndex()).isLessThan(NumUtil.getNum(70))){
            return NumUtil.getNum(0.66);
        }else {
            return NumUtil.getNum(1);
        }
    }


}
