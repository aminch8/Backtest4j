package com.auctus.core.domains;

import com.auctus.core.domains.enums.CostType;
import com.auctus.core.utils.NumUtil;
import org.ta4j.core.num.Num;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;


class Costs {

    CostType costType = CostType.ABSOLUTE;
    Num value = NumUtil.getNum(0);

    public <T extends Costs> Num getSlippedPrice(T t,Num price,Order order){
       switch (t.costType) {
           case PERCENT:
                if (order.getVolume().isPositive()){
                    return price.plus(price.multipliedBy(
                            t.value.dividedBy(NumUtil.getNum(100)).plus(NumUtil.getNum(1))
                    ));
                }else {
                    return price.minus(price.multipliedBy(
                            NumUtil.getNum(1).minus(t.value.dividedBy(NumUtil.getNum(100)))
                    ));
                }
           case ABSOLUTE:
               if (order.getVolume().isPositive()){
                   return price.plus(t.value);
               }else {
                   return price.minus(t.value);
               }
       }
       return price;

    }

}
