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


    public static Order ofMarketInLong(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }

    public static Order ofLimitInLong(Num volume,Num price){
        Order marketOrder = new Order();
        marketOrder.setOrderType(OrderType.LIMIT);
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        if (price!=null) marketOrder.setPrice(price);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }

    public static Order ofMarketInShort(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }

    public static Order ofLimitInShort(Num volume,Num price){
        Order marketOrder = new Order();
        marketOrder.setOrderType(OrderType.LIMIT);
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        if (price!=null) marketOrder.setPrice(price);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }


    public static Order ofMarketOutLong(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(true);
        return marketOrder;
    }

    public static Order ofLimitOutLong(Num volume,Num price){
        Order marketOrder = new Order();
        marketOrder.setOrderType(OrderType.LIMIT);
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        if (price!=null) marketOrder.setPrice(price);
        marketOrder.setReduceOnly(true);
        return marketOrder;
    }

    public static Order ofMarketOutShort(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(true);
        return marketOrder;
    }

    public static Order ofLimitOutShort(Num volume,Num price){
        Order marketOrder = new Order();
        marketOrder.setOrderType(OrderType.LIMIT);
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        if (price!=null) marketOrder.setPrice(price);
        marketOrder.setReduceOnly(true);
        return marketOrder;
    }

}
