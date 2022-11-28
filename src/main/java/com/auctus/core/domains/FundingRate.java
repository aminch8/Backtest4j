package com.auctus.core.domains;

import com.auctus.core.domains.enums.CostType;
import com.auctus.core.domains.enums.PeriodicCostInterval;
import com.auctus.core.utils.NumUtil;

public class FundingRate extends PeriodicCosts{


    public static FundingRate ofAbsolutePrice(Number valueInPrice,PeriodicCostInterval periodicCostInterval){
        FundingRate fundingRate = new FundingRate();
        fundingRate.value= NumUtil.getNum(valueInPrice);
        fundingRate.costType= CostType.ABSOLUTE;
        fundingRate.periodicCostInterval=periodicCostInterval;
        return fundingRate;
    }

    public static FundingRate ofPercentPrice(Number valueInPercent,PeriodicCostInterval periodicCostInterval){
        FundingRate fundingRate = new FundingRate();
        fundingRate.value= NumUtil.getNum(valueInPercent).dividedBy(NumUtil.getNum(100));
        fundingRate.costType= CostType.PERCENT;
        fundingRate.periodicCostInterval=periodicCostInterval;
        return fundingRate;
    }

    private FundingRate() {
    }
}
