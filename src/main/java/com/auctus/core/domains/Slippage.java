package com.auctus.core.domains;


import com.auctus.core.domains.enums.CostType;
import com.auctus.core.utils.NumUtil;
import lombok.Getter;


public class Slippage extends Costs {

    public static Slippage ofAbsolutePrice(Number valueInPrice){
        Slippage slippage = new Slippage();
        slippage.value= NumUtil.getNum(valueInPrice);
        slippage.costType= CostType.ABSOLUTE;
        return slippage;
    }

    public static Slippage ofPercentPrice(Number valueInPercent){
        Slippage slippage = new Slippage();
        slippage.value= NumUtil.getNum(valueInPercent).dividedBy(NumUtil.getNum(100));
        slippage.costType= CostType.PERCENT;
        return slippage;
    }

    private Slippage() {
    }
}
