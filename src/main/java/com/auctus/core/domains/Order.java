package com.auctus.core.domains;

import com.auctus.core.domains.enums.OrderType;
import com.auctus.core.interfaces.OrderExecutionCallback;
import com.auctus.core.utils.NumUtil;
import lombok.Data;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

@Data
public class Order {
    private OrderType orderType=OrderType.MARKET;
    private Num volume=NumUtil.getNum(0);
    private Num price=NumUtil.getNum(0);
    private boolean reduceOnly=false;
    private ZonedDateTime expiration;
    private OrderExecutionCallback orderExecutionCallback;

    private Order(){}


    public static Order ofMarketInLong(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }

    public static Order ofLimitInLong(Num volume,Num price){
        return Order.ofLimitInLong(volume,price,null);
    }

    public static Order ofLimitInLong(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order limitOrder = new Order();
        limitOrder.setOrderType(OrderType.LIMIT);
        limitOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        if (price!=null) limitOrder.setPrice(price);
        limitOrder.setReduceOnly(false);
        limitOrder.setExpiration(goodTillCandleTime);
        return limitOrder;
    }

    public static Order ofMarketInShort(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(false);
        return marketOrder;
    }

    public static Order ofLimitInShort(Num volume,Num price){
       return Order.ofLimitInShort(volume,price,null);
    }

    public static Order ofLimitInShort(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order limitOrder = new Order();
        limitOrder.setOrderType(OrderType.LIMIT);
        limitOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        if (price!=null) limitOrder.setPrice(price);
        limitOrder.setReduceOnly(false);
        limitOrder.setExpiration(goodTillCandleTime);
        return limitOrder;
    }


    public static Order ofMarketOutLong(Num volume){
        Order marketOrder = new Order();
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        marketOrder.setOrderType(OrderType.MARKET);
        marketOrder.setReduceOnly(true);
        return marketOrder;
    }

    public static Order ofLimitOutLong(Num volume,Num price){
        return Order.ofLimitOutLong(volume,price,null);
    }

    public static Order ofLimitOutLong(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order marketOrder = new Order();
        marketOrder.setOrderType(OrderType.LIMIT);
        marketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        if (price!=null) marketOrder.setPrice(price);
        marketOrder.setReduceOnly(true);
        marketOrder.setExpiration(goodTillCandleTime);
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
        return Order.ofLimitOutShort(volume,price,null);
    }

    public static Order ofLimitOutShort(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order limitOrder = new Order();
        limitOrder.setOrderType(OrderType.LIMIT);
        limitOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        if (price!=null) limitOrder.setPrice(price);
        limitOrder.setReduceOnly(true);
        limitOrder.setExpiration(goodTillCandleTime);
        return limitOrder;
    }

    public static Order ofStopMarketOutShort(Num volume,Num price){
         return Order.ofStopMarketOutShort(volume,price,null);
    }

    public static Order ofStopMarketOutShort(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order stopMarketOrder = new Order();
        stopMarketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        stopMarketOrder.setOrderType(OrderType.STOP_MARKET);
        stopMarketOrder.setReduceOnly(true);
        stopMarketOrder.setPrice(price);
        stopMarketOrder.setExpiration(goodTillCandleTime);
        return stopMarketOrder;
    }

    public static Order ofStopMarketOutLong(Num volume,Num price){
        return Order.ofStopMarketOutLong(volume,price,null);
    }

    public static Order ofStopMarketOutLong(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order stopMarketOrder = new Order();
        stopMarketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        stopMarketOrder.setOrderType(OrderType.STOP_MARKET);
        stopMarketOrder.setReduceOnly(true);
        stopMarketOrder.setPrice(price);
        stopMarketOrder.setExpiration(goodTillCandleTime);
        return stopMarketOrder;
    }

    public static Order ofStopMarketInShort(Num volume,Num price){
        return Order.ofStopMarketInShort(volume,price,null);
    }

    public static Order ofStopMarketInShort(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order stopMarketOrder = new Order();
        stopMarketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(-1)));
        stopMarketOrder.setOrderType(OrderType.STOP_MARKET);
        stopMarketOrder.setReduceOnly(false);
        stopMarketOrder.setPrice(price);
        stopMarketOrder.setExpiration(goodTillCandleTime);
        return stopMarketOrder;
    }

    public static Order ofStopMarketInLong(Num volume,Num price){
        return Order.ofStopMarketInLong(volume,price,null);
    }

    public static Order ofStopMarketInLong(Num volume,Num price,ZonedDateTime goodTillCandleTime){
        Order stopMarketOrder = new Order();
        stopMarketOrder.setVolume(volume.abs().multipliedBy(NumUtil.getNum(1)));
        stopMarketOrder.setOrderType(OrderType.STOP_MARKET);
        stopMarketOrder.setReduceOnly(false);
        stopMarketOrder.setPrice(price);
        stopMarketOrder.setExpiration(goodTillCandleTime);
        return stopMarketOrder;
    }


    public Order setOrderExecutionCallback(OrderExecutionCallback orderExecutionCallback){
        this.orderExecutionCallback = orderExecutionCallback;
        return this;
    }

}
