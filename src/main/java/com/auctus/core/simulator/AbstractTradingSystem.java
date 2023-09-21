package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.*;
import com.auctus.core.domains.enums.OrderType;
import com.auctus.core.utils.NumUtil;
import lombok.Getter;
import lombok.Setter;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractTradingSystem {


    private List<Order> orders=new ArrayList<Order>();
    private Position position=new Position();
    @Getter
    private Num balance= getStartingBalance();

    public abstract void onBuyCondition();
    public abstract void onSellCondition();
    public abstract void onExitBuyCondition();
    public abstract void onExitSellCondition();
    public abstract void onEveryCandle();
    public abstract Num getStartingBalance();
    public abstract FundingRate getFundingRate();
    public abstract Commission getCommission();
    public abstract Slippage getSlippage();
    public abstract Spread getSpread();
    public abstract int getStartIndexBackTest();

    @Getter
    private final BarSeriesProvider barSeriesProvider;

    public AbstractTradingSystem(BarSeriesProvider barSeriesProvider) {
        this.barSeriesProvider = barSeriesProvider;
    }

    public Position getRunningPosition() {
        return position;
    }

    public List<Order> getActiveOrders() {
        return orders;
    }

    public String getSymbol() {
        return getBarSeriesProvider().getSymbol();
    }

    public void addOrder(Order order){
        this.orders.add(order);
    }

    public void addOrder(Order... orders){
        for (Order order : orders) {
            this.orders.add(order);
        }
    }

    public void clearAllOrders(){
        this.orders=new ArrayList<Order>();
    }

    public void clearAllOrders(OrderType orderType){
        this.orders=this.orders.stream().filter(i->i.getOrderType()!=orderType).collect(Collectors.toList());
    }

    public void updatePosition(Num deltaPositionSize, Num executedPrice){

        if (this.position.getSize().isPositive() && deltaPositionSize.isPositive()){
            Num newAverageEntryPrice = this.position.getSize().multipliedBy(position.getAverageEntryPrice())
                    .plus(deltaPositionSize.multipliedBy(executedPrice))
                    .dividedBy(deltaPositionSize.plus(position.getSize()));
            this.position.setSize(position.getSize().plus(deltaPositionSize));
            this.position.setAverageEntryPrice(newAverageEntryPrice);
        }else if (this.position.getSize().isNegative() && deltaPositionSize.isNegative()){
            Num newAverageEntryPrice = this.position.getSize().multipliedBy(position.getAverageEntryPrice())
                    .plus(deltaPositionSize.multipliedBy(executedPrice))
                    .dividedBy(deltaPositionSize.plus(position.getSize()));
            this.position.setSize(position.getSize().plus(deltaPositionSize));
            this.position.setAverageEntryPrice(newAverageEntryPrice);
        }else {
            if(position.getSize().isZero()){
                this.position.setAverageEntryPrice(executedPrice);
            }else {
                if (position.getSize().plus(deltaPositionSize).multipliedBy(position.getSize()).isNegative()){
                    this.position.setAverageEntryPrice(executedPrice);
                }
            }
            this.position.setSize(position.getSize().plus(deltaPositionSize));
        }

        if (position.getSize().abs().isLessThan(NumUtil.getNum(0.000000000000000000000000000001))){
            position.setSize(NumUtil.getNum(0));
        }

    }

    public void addBalance(Num realizedProfitAndLoss){
        this.balance=this.balance.plus(realizedProfitAndLoss);
    }
    public void reduceBalance(Num reductionAmount){
        this.balance=this.balance.minus(reductionAmount);
    }
    public void reduceBalancePercent(Num reductionInPercent){
        this.balance=this.balance.multipliedBy(
                NumUtil.getNum(1)
                .minus(
                    reductionInPercent.dividedBy(NumUtil.getNum(100))
                )
        );
    }
    public void clearOrder(Order order){
        this.orders.remove(order);
    }

    public void clearOrders(Order... orders){
        for (Order order : orders) {
            this.clearOrder(order);
        }
    }

    public List<Order> getOrders(OrderType orderType){
        return this.orders.stream().filter(i->i.getOrderType()==orderType).collect(Collectors.toList());
    }


}
