package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.AbstractBarSeriesProvider;
import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.*;
import com.auctus.core.domains.enums.OrderType;
import com.auctus.core.domains.enums.PeriodicCostInterval;
import com.auctus.core.utils.NumUtil;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class Simulator<T extends AbstractTradingSystem> {

    private T tradingSystem;
    private Slippage slippage = Slippage.ofPercentPrice(0);
    private Commission commission = Commission.ofPercentPrice(0);
    private FundingRate fundingRate = FundingRate.ofPercentPrice(0, PeriodicCostInterval.EIGHT_HOURS);
    private List<TradeLog> tradeHistory = new ArrayList<>();
    private List<BalanceSnapshot> balanceSnapshots = new ArrayList<>();

    public Simulator(T tradingSystem) {
        this.tradingSystem = tradingSystem;
    }

    public void startSimulation(){
        while (tradingSystem.getBarSeriesProvider().tickForward()){
            this.simulateTick();
        }
        log.info("Simulation Ended : " + this.tradingSystem.getSymbol());
        log.info("Total Bars Simulated : " + this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getBarCount());
        log.info("Ending Balance : " + this.tradingSystem.getBalance());
        log.info("Starting Balance : " + this.tradingSystem.startingBalance());
    }

    private void simulateTick() {
        processTick();
        Position runningPosition = tradingSystem.getRunningPosition();

        if (runningPosition.isLong()) {
            Order exitBuy = tradingSystem.onExitBuyCondition();
            if (exitBuy!=null && !exitBuy.getVolume().isZero() && exitBuy.getOrderType() == OrderType.LIMIT)
                tradingSystem.addOrder(exitBuy);
            else analyzeOrder(exitBuy);
        }

        if (runningPosition.isShort()) {
            Order exitSell = tradingSystem.onExitSellCondition();
            if (exitSell!=null && !exitSell.getVolume().isZero() && exitSell.getOrderType() == OrderType.LIMIT)
                tradingSystem.addOrder(exitSell);
            else analyzeOrder(exitSell);
        }

        Order buyOrder = tradingSystem.onBuyCondition();
        if (buyOrder!=null && !buyOrder.getVolume().isZero() && buyOrder.getOrderType() == OrderType.LIMIT)
            tradingSystem.addOrder(buyOrder);
        else analyzeOrder(buyOrder);


        Order sellOrder = tradingSystem.onSellCondition();
        if (sellOrder!=null && !sellOrder.getVolume().isZero() && sellOrder.getOrderType() == OrderType.LIMIT)
            tradingSystem.addOrder(sellOrder);
        else analyzeOrder(sellOrder);

       this.createSnapShotOfBalance();
       this.fundingRateFees();
    }

    private void fundingRateFees() {
        BarSeries baseBarSeries = tradingSystem.getBarSeriesProvider().getBaseBarSeries();
        if (baseBarSeries.getBarCount()<2) return;
        Bar lastBar = baseBarSeries.getLastBar();
        Bar previousBar = baseBarSeries.getBar(baseBarSeries.getBarCount()-2);
        ZonedDateTime lastBarEndTime = lastBar.getEndTime();
        ZonedDateTime previousBarEndTime = previousBar.getEndTime();

        lastBarEndTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

    }

    private void createSnapShotOfBalance() {
        Bar currentBar = tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        Position runningPosition = tradingSystem.getRunningPosition();
        BalanceSnapshot balanceSnapshot =
                BalanceSnapshot.createSnapshot(currentBar.getEndTime(),
                        runningPosition.getUnrealizedProfitAndLoss(currentBar.getClosePrice()).plus(tradingSystem.getBalance()),
                        tradingSystem.getBalance());
        balanceSnapshots.add(balanceSnapshot);
    }


    private void analyzeOrder(Order order) {
        if (order == null || order.getVolume().isZero()) return;

        if (order.isReduceOnly()) {
           closePositionOrderAnalyze(order);
        } else {
            openPositionOrderAnalyze(order);
        }

    }

    private void analyzeOrders(List<Order> orders) {
        for (Order order : orders) {
            this.analyzeOrder(order);
        }
    }

    private void processTick() {
        List<Order> activeOrders = tradingSystem.getActiveOrders();
        analyzeOrders(activeOrders);
    }

    private void openPositionOrderAnalyze(Order order){
        Num orderVolume = order.getVolume();
        Num orderPrice = order.getPrice();
        Bar lastCandle = this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        Num executedPrice = null;
        Position position = tradingSystem.getRunningPosition();
        if (order.getOrderType() == OrderType.MARKET) {
            executedPrice = lastCandle.getClosePrice();
            executedPrice = slippage.getSlippedPrice(executedPrice,order);
            tradingSystem.reduceBalance(commission.getCostOfCommission(lastCandle.getClosePrice(), order));
        }

        if (order.getOrderType() == OrderType.LIMIT) {
            executedPrice = orderPrice;
            if (orderVolume.isPositive()) {
                if (lastCandle.getLowPrice().isGreaterThan(orderPrice)) return;
            } else {
                if (lastCandle.getHighPrice().isLessThan(orderPrice)) return;
            }
            tradingSystem.reduceBalance(commission.getCostOfCommission(orderPrice, order));
        }


        TradeLog tradeLog = TradeLog.createLog(tradingSystem.getSymbol(), orderVolume, executedPrice, lastCandle.getEndTime());
        tradeHistory.add(tradeLog);

        position.setSize(position.getSize().plus(orderVolume));
        tradingSystem.updatePosition(position);
    }

    private void closePositionOrderAnalyze(Order order){
        Num orderVolume = order.getVolume();
        Bar lastCandle = this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        Position position = this.tradingSystem.getRunningPosition();
        Num executedPrice = null;
        Num newNetSize = null;

        switch (order.getOrderType()){
            case LIMIT:
                Num orderPrice = order.getPrice();
                executedPrice = orderPrice;
                if (orderVolume.isPositive()) {
                    //closing short position
                    if (lastCandle.getLowPrice().isGreaterThan(orderPrice)) return;
                    if (position.getSize().isNegative()) {
                        newNetSize = position.getSize().plus(orderVolume).min(NumUtil.getNum(0));
                        tradingSystem.reduceBalance(commission.getCostOfCommission(orderPrice, order));
                    } else {
                        log.error("Something has gone wrong... Reduce only order on wrong side of the position?");
                    }
                } else {
                    //closing long position
                    if (lastCandle.getHighPrice().isLessThan(orderPrice)) return;
                    if (position.getSize().isPositive()) {
                        newNetSize = position.getSize().plus(orderVolume).max(NumUtil.getNum(0));
                        tradingSystem.reduceBalance(commission.getCostOfCommission(orderPrice, order));
                    } else {
                        log.error("Something has gone wrong... Reduce only order on wrong side of the position?");
                    }
                }
                break;
            case MARKET:
                executedPrice = lastCandle.getClosePrice();
                executedPrice = slippage.getSlippedPrice(executedPrice,order);
                if (orderVolume.isPositive()) {
                    //closing short position
                    if (position.getSize().isNegative()) {
                        newNetSize = position.getSize().plus(orderVolume).min(NumUtil.getNum(0));
                        tradingSystem.reduceBalance(commission.getCostOfCommission(lastCandle.getClosePrice(), order));
                    } else {
                        log.error("Something has gone wrong... Reduce only order on wrong side of the position?");
                    }
                } else {
                    //closing long position
                    if (position.getSize().isPositive()) {
                        newNetSize = position.getSize().plus(orderVolume).max(NumUtil.getNum(0));
                        tradingSystem.reduceBalance(commission.getCostOfCommission(lastCandle.getClosePrice(), order));
                    } else {
                        log.error("Something has gone wrong... Reduce only order on wrong side of the position?");
                    }
                }
                break;
            default: return;
        }

        Num deltaPositionSize = newNetSize.minus(position.getSize());

        Num realizedProfitAndLoss = deltaPositionSize.multipliedBy(executedPrice);
        tradingSystem.addBalance(realizedProfitAndLoss);

        TradeLog tradeLog = TradeLog.createLog(tradingSystem.getSymbol(), deltaPositionSize, executedPrice, lastCandle.getEndTime());
        tradeHistory.add(tradeLog);

        position.setSize(newNetSize);
        tradingSystem.updatePosition(position);

    }

}
