package com.auctus.core.domains;


import com.auctus.core.domains.enums.CostType;
import com.auctus.core.utils.NumUtil;
import org.ta4j.core.num.Num;


public class Spread extends Costs {

    public static Spread ofAbsolutePrice(Number valueInPrice) {
        Spread spread = new Spread();
        spread.value = NumUtil.getNum(valueInPrice);
        spread.costType = CostType.ABSOLUTE;
        return spread;
    }

    public static Spread ofPercentPrice(Number valueInPercent) {
        Spread spread = new Spread();
        spread.value = NumUtil.getNum(valueInPercent).dividedBy(NumUtil.getNum(100));
        spread.costType = CostType.PERCENT;
        return spread;
    }


    public Num getAskPrice(Num currentBid) {
        switch (this.costType) {
            case PERCENT:
                return currentBid.multipliedBy(
                        this.value.plus(NumUtil.getNum(1)));
            case ABSOLUTE:
                    return currentBid.plus(this.value);
        }
        return currentBid;
    }

    public Num getBidPrice(Num currentBid) {
        return currentBid;
    }

    private Spread() {
    }
}
