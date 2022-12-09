package com.auctus.core.domains;


import com.auctus.core.domains.enums.CostType;
import com.auctus.core.utils.NumUtil;
import lombok.Getter;
import org.ta4j.core.num.Num;


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


    public Num getSlippedPrice(Num price, Order order){
        switch (this.costType) {
            case PERCENT:
                if (order.getVolume().isPositive()){
                    return price.plus(price.multipliedBy(
                            this.value.dividedBy(NumUtil.getNum(100)).plus(NumUtil.getNum(1))
                    ));
                }else {
                    return price.minus(price.multipliedBy(
                            NumUtil.getNum(1).minus(this.value.dividedBy(NumUtil.getNum(100)))
                    ));
                }
            case ABSOLUTE:
                if (order.getVolume().isPositive()){
                    return price.plus(this.value);
                }else {
                    return price.minus(this.value);
                }
        }
        return price;

    }

    private Slippage() {
    }
}
