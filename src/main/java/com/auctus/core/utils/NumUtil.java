package com.auctus.core.utils;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;

public class NumUtil {

    public static Num getNum(Number number){
        BarSeries BarSeries = new BaseBarSeries();
        return BarSeries.numOf(number);
    }
}
