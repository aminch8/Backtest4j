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
import java.util.Comparator;
import java.util.List;

@Slf4j
public class Simulator<T extends AbstractTradingSystem> {

    private T tradingSystem;
    private Slippage slippage;
    private Commission commission;
    private FundingRate fundingRate;
    private List<TradeLog> tradeHistory = new ArrayList<>();
    private List<BalanceSnapshot> balanceSnapshots = new ArrayList<>();

    public Simulator(T abstractTradingSystem) {
        this.tradingSystem = abstractTradingSystem;
        slippage = tradingSystem.getSlippage() == null ? Slippage.ofPercentPrice(0) : tradingSystem.getSlippage();
        commission = tradingSystem.getCommission() == null ? Commission.ofPercentPrice(0) : tradingSystem.getCommission();
        fundingRate = tradingSystem.getFundingRate() == null ? FundingRate.ofPercentPrice(0, PeriodicCostInterval.EIGHT_HOURS) : tradingSystem.getFundingRate();
    }

    public void startSimulation() {
        while (tradingSystem.getBarSeriesProvider().tickForward()) {
            this.simulateTick();
            System.out.println("QQQ");
        }
        log.info("Simulation Ended : " + this.tradingSystem.getSymbol());
        log.info("Total Bars Simulated : " + this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getBarCount());
        log.info("Ending Balance : " + this.tradingSystem.getBalance());
        log.info("Starting Balance : " + this.tradingSystem.startingBalance());
    }

    private void simulateTick() {
        processTick();
        analyzeOrders(tradingSystem.getActiveOrders());
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

        int fundingRatesHappened = 0;

        switch (fundingRate.getPeriodicCostInterval()) {
            case EIGHT_HOURS:
                if (openTime.equals(checkPoint)) {
                    fundingRatesHappened++;
                }
                while (checkPoint.isBefore(closeTime)) {
                    checkPoint = checkPoint.plusHours(8);
                    if (checkPoint.isBefore(closeTime)) {
                        fundingRatesHappened++;
                    }
                }
            case DAILY:
                if (openTime.equals(checkPoint)) {
                    fundingRatesHappened++;
                }
                while (checkPoint.isBefore(closeTime)) {
                    checkPoint = checkPoint.plusHours(24);
                    if (checkPoint.isBefore(closeTime)) {
                        fundingRatesHappened++;
                    }
                }
        }
        Position runningPosition = tradingSystem.getRunningPosition();
        Num fundingFee = fundingRate.getFundingRateValue(runningPosition, lastBar.getClosePrice()).multipliedBy(NumUtil.getNum(fundingRatesHappened));
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
        if (tradingSystem.getRunningPosition().isLong()){
            orders.sort(Comparator.comparing(Order::isReduceOnly).reversed());
            orders.sort(Comparator.comparing(Order::getOrderType));
            orders.sort(Comparator.comparing(Order::getPrice));
        }else if (tradingSystem.getRunningPosition().isShort()){
            orders.sort(Comparator.comparing(Order::isReduceOnly).reversed());
            orders.sort(Comparator.comparing(Order::getOrderType));
            orders.sort(Comparator.comparing(Order::getPrice).reversed());
        }
        for (Order order : orders) {
            log.debug("Debugging orders order : " + order.getOrderType());
            log.debug("Debugging orders order : " + order.isReduceOnly());
            log.debug("--------------");
            this.analyzeOrder(order);
        }
    }

    private void processTick() {
        List<Order> activeOrders = tradingSystem.getActiveOrders();
        analyzeOrders(activeOrders);
    }

    private void openPositionOrderAnalyze(Order order) {
        Num orderVolume = order.getVolume();
        Num orderPrice = order.getPrice();
        Bar lastCandle = this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        Num executedPrice = lastCandle.getClosePrice();
        Position position = tradingSystem.getRunningPosition();
        switch (order.getOrderType()){
            case MARKET:{
                executedPrice = lastCandle.getClosePrice();
                executedPrice = slippage.getSlippedPrice(executedPrice, order);
                tradingSystem.reduceBalance(commission.getCostOfCommission(lastCandle.getClosePrice(), order));
                break;
            }
            case LIMIT:{
                executedPrice = orderPrice;
                if (orderVolume.isPositive()) {
                    if (lastCandle.getLowPrice().isGreaterThan(orderPrice)) return;
                } else {
                    if (lastCandle.getHighPrice().isLessThan(orderPrice)) return;
                }
                tradingSystem.reduceBalance(commission.getCostOfCommission(orderPrice, order));
                break;
            }
        }
        TradeLog tradeLog = TradeLog.createLog(tradingSystem.getSymbol(), orderVolume, executedPrice, lastCandle.getEndTime());
        tradeHistory.add(tradeLog);

        position.setSize(position.getSize().plus(orderVolume));
        tradingSystem.updatePosition(position);
    }

    private void closePositionOrderAnalyze(Order order) {
        Num orderVolume = order.getVolume();
        Bar lastCandle = this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        Position position = this.tradingSystem.getRunningPosition();
        Num executedPrice = lastCandle.getClosePrice();
        Num newNetSize = position.getSize();

        switch (order.getOrderType()) {
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
                        tradingSystem.clearOrder(order);
                    }
                } else {
                    //closing long position
                    if (lastCandle.getHighPrice().isLessThan(orderPrice)) return;
                    if (position.getSize().isPositive()) {
                        newNetSize = position.getSize().plus(orderVolume).max(NumUtil.getNum(0));
                        tradingSystem.reduceBalance(commission.getCostOfCommission(orderPrice, order));
                    } else {
                        log.error("Something has gone wrong... Reduce only order on wrong side of the position?");
                        tradingSystem.clearOrder(order);
                    }
                }
                break;
            case MARKET:
                executedPrice = lastCandle.getClosePrice();
                executedPrice = slippage.getSlippedPrice(executedPrice, order);
                if (orderVolume.isPositive()) {
                    //closing short position
                    if (position.getSize().isNegative()) {
                        newNetSize = position.getSize().plus(orderVolume).min(NumUtil.getNum(0));
                        tradingSystem.reduceBalance(commission.getCostOfCommission(lastCandle.getClosePrice(), order));
                    } else {
                        log.error("Something has gone wrong... Reduce only order on wrong side of the position?");
                        tradingSystem.clearOrder(order);
                    }
                } else {
                    //closing long position
                    if (position.getSize().isPositive()) {
                        newNetSize = position.getSize().plus(orderVolume).max(NumUtil.getNum(0));
                        tradingSystem.reduceBalance(commission.getCostOfCommission(lastCandle.getClosePrice(), order));
                    } else {
                        log.error("Something has gone wrong... Reduce only order on wrong side of the position?");
                        tradingSystem.clearOrder(order);
                    }
                }
                break;
            default:
                return;
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
