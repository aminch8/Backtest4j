package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.num.Num;

public class StochasticStrength extends Strength{

    private StochasticOscillatorKIndicator stochasticOscillatorKIndicator;



    public StochasticStrength(BarSeries barSeries,int stochasticPeriod) {
        super(barSeries);
        this.stochasticOscillatorKIndicator = new StochasticOscillatorKIndicator(barSeries,stochasticPeriod);
    }

    @Override
    Num getStrength() {
        Num stochasticValue = stochasticOscillatorKIndicator.getValue(getIndex());
        if (stochasticValue.isLessThanOrEqual(NumUtil.getNum(20))){
            return NumUtil.getNum(0);
        }else if (stochasticValue.isLessThanOrEqual(NumUtil.getNum(50))){
            return NumUtil.getNum(0.33);
        }else if (stochasticValue.isLessThanOrEqual(NumUtil.getNum(80))){
            return NumUtil.getNum(0.66);
        }else {
            return NumUtil.getNum(1);
        }
    }
}
