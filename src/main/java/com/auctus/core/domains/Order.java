package com.auctus.core.domains;

import com.auctus.core.domains.enums.OrderType;
import com.auctus.core.utils.NumUtil;
import lombok.Data;
import org.ta4j.core.num.Num;

@Data
public class Order {
    private OrderType orderType=OrderType.MARKET;
    private Num volume=NumUtil.getNum(0);
    private Num price=NumUtil.getNum(0);
    private boolean reduceOnly=false;


    public static Order ofMarketIn(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }

    public static Order ofLimitIn(Num volume,Num price){
        Order marketOrder = new Order();
        marketOrder.setOrderType(OrderType.LIMIT);
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        if (price!=null) marketOrder.setPrice(price);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }


    public static Order ofMarketOut(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(true);
        return marketOrder;
    }

    public static Order ofLimitOut(Num volume,Num price){
        Order marketOrder = new Order();
        marketOrder.setOrderType(OrderType.LIMIT);
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        if (price!=null) marketOrder.setPrice(price);
        marketOrder.setReduceOnly(true);
        return marketOrder;
    }

}
