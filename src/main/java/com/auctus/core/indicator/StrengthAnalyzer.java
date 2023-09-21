package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.num.Num;

import java.util.List;

public class StrengthAnalyzer {

    private List<? extends Strength> strengths;

    public StrengthAnalyzer(Strength... strengths) {
        this.strengths = List.of(strengths);
    }

    private StrengthAnalyzer(){

    }

    private Num getStrength(){
        Num totalStrength = NumUtil.getNum(0);
        for (Strength strength : strengths) {
            totalStrength = totalStrength.plus(strength.getStrength());
        }
        return totalStrength.dividedBy(NumUtil.getNum(strengths.size())).multipliedBy(NumUtil.getNum(20)).minus(NumUtil.getNum(10));
    }

    public Num sigmoid(){
        return NumUtil.getNum(1).dividedBy(
                NumUtil.getNum(1+Math.exp(-(getStrength().doubleValue())))
        );
    }

    public Num sigmoid2(){
        return getStrength().multipliedBy(
               NumUtil.getNum(
                       Math.exp(
                               -(Math.pow(getStrength().doubleValue(),2))
                       )
               )
        );
    }
}
