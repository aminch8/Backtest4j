package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.*;
import com.auctus.core.utils.NumUtil;
import lombok.Getter;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTradingSystem {


    private List<Order> orders=new ArrayList<Order>();
    private Position position=new Position();
    @Getter
    private Num balance= NumUtil.getNum(getStartingBalance());

    public abstract void onBuyCondition();
    public abstract void onSellCondition();
    public abstract void onExitBuyCondition();
    public abstract void onExitSellCondition();
    public abstract Number getStartingBalance();
    public abstract FundingRate getFundingRate();
    public abstract Commission getCommission();
    public abstract Slippage getSlippage();

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

    public void updatePosition(Num deltaPositionSize, Num executedPrice){
        Num newAverageEntryPrice = this.position.getSize().multipliedBy(position.getAverageEntryPrice())
                .plus(deltaPositionSize.multipliedBy(executedPrice))
                .dividedBy(deltaPositionSize.plus(position.getSize()));

        if (newAverageEntryPrice.isNaN()){
            this.position.setSize(NumUtil.getNum(0));
            this.position.setAverageEntryPrice(NumUtil.getNum(0));
        }else {
            this.position.setSize(position.getSize().plus(deltaPositionSize));
            this.position.setAverageEntryPrice(newAverageEntryPrice);
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


}
