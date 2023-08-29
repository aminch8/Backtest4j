package com.auctus.core.utils;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;

import java.util.List;

public class NumUtil {

    public static Num getNum(Number number){
        BarSeries BarSeries = new BaseBarSeries();
        return BarSeries.numOf(number);
    }

    public static Num getStandardDeviation(List<Num> numbers){
        Num sum = NumUtil.getNum(0);
        Num average = NumUtil.getNum(0);
        Num sumForSD = NumUtil.getNum(0);
        for (Num number : numbers) {
            sum = sum.plus(number);
        }
        average = sum.dividedBy(NumUtil.getNum(numbers.size()));
        for (Num number : numbers) {
            sumForSD = sumForSD.plus(number.minus(average).pow(2));
        }

        return sumForSD.dividedBy(getNum(numbers.size()-1)).sqrt();
    }
}
