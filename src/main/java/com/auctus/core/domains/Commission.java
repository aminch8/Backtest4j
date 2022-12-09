package com.auctus.core.domains;

import com.auctus.core.domains.enums.CostType;
import com.auctus.core.utils.NumUtil;
import org.ta4j.core.num.Num;

public class Commission extends Costs {

    public static Commission ofAbsolutePrice(Number valueInPrice){
        Commission commission = new Commission();
        commission.value= NumUtil.getNum(valueInPrice);
        commission.costType= CostType.ABSOLUTE;
        return commission;
    }

    public static Commission ofPercentPrice(Number valueInPercent){
        Commission commission = new Commission();
        commission.value= NumUtil.getNum(valueInPercent).dividedBy(NumUtil.getNum(100));
        commission.costType= CostType.PERCENT;
        return commission;
    }

    public Num getCostOfCommission(Num price,Order order){
        switch (this.costType) {
            case PERCENT:
                    return price.multipliedBy(
                            this.value.dividedBy(NumUtil.getNum(100))
                    ).multipliedBy(
                            order.getVolume().abs()
                    );
            case ABSOLUTE:
                    return value.multipliedBy(order.getVolume().abs());
        }
        return price;
    }

    private Commission() {
    }
}
