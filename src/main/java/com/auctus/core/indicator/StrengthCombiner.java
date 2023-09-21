package com.auctus.core.indicator;

import com.auctus.core.utils.NumUtil;
import org.ta4j.core.num.Num;

import java.util.List;

public class StrengthCombiner extends Strength {


    private List<? extends Strength> strengths;

    public StrengthCombiner(Strength... strengths) {
        super(null);
        this.strengths = List.of(strengths);
    }


    @Override
    Num getStrength() {
        return NumUtil.getNum(strengths.stream().mapToDouble(i->i.getStrength().doubleValue()).sum()/ (double) strengths.size());
    }
}
