package com.auctus.core.simulator;

import com.auctus.core.domains.*;
import com.auctus.core.domains.enums.OrderType;
import com.auctus.core.domains.enums.PeriodicCostInterval;
import com.auctus.core.exceptions.SimulatorException;
import com.auctus.core.utils.NumUtil;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Simulator {

    private AbstractTradingSystem tradingSystem;
    private Slippage slippage = tradingSystem.getSlippage()==null?Slippage.ofPercentPrice(0):tradingSystem.getSlippage();
    private Commission commission = tradingSystem.getCommission()==null?Commission.ofPercentPrice(0):tradingSystem.getCommission();
    private FundingRate fundingRate = tradingSystem.getFundingRate()==null?FundingRate.ofPercentPrice(0, PeriodicCostInterval.EIGHT_HOURS):tradingSystem.getFundingRate();
    private List<TradeLog> tradeHistory = new ArrayList<>();
    private List<BalanceSnapshot> balanceSnapshots = new ArrayList<>();

    public Simulator(Class<? extends AbstractTradingSystem> tradingSystem) {
        try{
            this.tradingSystem = tradingSystem.getConstructor().newInstance();
        }catch (Exception e){
            throw new SimulatorException("Cannot create instance of class " + tradingSystem.getName());
        }
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
        Bar lastBar = baseBarSeries.getLastBar();
        ZonedDateTime openTime = lastBar.getBeginTime();
        ZonedDateTime closeTime = lastBar.getEndTime();
        ZonedDateTime startOfDayOnOpen = openTime.withHour(0).withHour(0).withSecond(0).withNano(0);
        ZonedDateTime checkPoint = startOfDayOnOpen;

        int fundingRatesHappened=0;

        switch (fundingRate.getPeriodicCostInterval()){
            case EIGHT_HOURS:
                if (openTime.equals(checkPoint)){
                    fundingRatesHappened++;
                }
                while (checkPoint.isBefore(closeTime)){
                    checkPoint = checkPoint.plusHours(8);
                    if (checkPoint.isBefore(closeTime)){
                        fundingRatesHappened++;
                    }
                }
            case DAILY:
                if (openTime.equals(checkPoint)){
                    fundingRatesHappened++;
                }
                while (checkPoint.isBefore(closeTime)){
                    checkPoint = checkPoint.plusHours(24);
                    if (checkPoint.isBefore(closeTime)){
                        fundingRatesHappened++;
                    }
                }
        }
        Position runningPosition = tradingSystem.getRunningPosition();
        Num fundingFee = fundingRate.getFundingRateValue(runningPosition,lastBar.getClosePrice()).multipliedBy(NumUtil.getNum(fundingRatesHappened));
        tradingSystem.reduceBalance(fundingFee);
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
