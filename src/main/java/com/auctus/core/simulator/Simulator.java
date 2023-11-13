package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.*;
import com.auctus.core.domains.enums.OrderType;
import com.auctus.core.domains.enums.PeriodicCostInterval;
import com.auctus.core.domains.enums.TimeFrame;
import com.auctus.core.exceptions.SimulatorException;
import com.auctus.core.utils.NumUtil;
import com.auctus.core.utils.OrderUtil;
import com.auctus.core.utils.ZDTUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.*;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public class Simulator<T extends AbstractTradingSystem> {

    private T tradingSystem;
    private Slippage slippage;
    private Commission commission;
    private FundingRate fundingRate;
    private Spread spread;
    private List<TradeLog> tradeHistory = new ArrayList<>();
    private List<BalanceSnapshot> balanceSnapshots = new ArrayList<>();
    private Num totalFundingFees = NumUtil.getNum(0);
    private Num totalCommissions = NumUtil.getNum(0);
    private boolean simulationComplete = false;

    public Simulator(T abstractTradingSystem) {
        this.tradingSystem = abstractTradingSystem;
        slippage = tradingSystem.getSlippage() == null ? Slippage.ofPercentPrice(0) : tradingSystem.getSlippage();
        commission = tradingSystem.getCommission() == null ? Commission.ofPercentPrice(0) : tradingSystem.getCommission();
        fundingRate = tradingSystem.getFundingRate() == null ? FundingRate.ofPercentPrice(0, PeriodicCostInterval.EIGHT_HOURS) : tradingSystem.getFundingRate();
        spread = tradingSystem.getSpread() == null ? Spread.ofAbsolutePrice(0) : tradingSystem.getSpread();
    }

    public void startSimulation() {
        if (simulationComplete) return;
        while (tradingSystem.getBarSeriesProvider().tickForward()) {
            this.processTick();
        }
        simulationComplete = true;
    }

    public Num getStartingBalance() {
        return this.tradingSystem.getStartingBalance();
    }

    public Num getBalance() {
        return this.tradingSystem.getBalance();
    }


    private void processTick() {
        this.processOrders();
        this.fundingRateFees();
        this.createSnapShotOfBalance();
        this.checkLiquidation();
    }

    private void fundingRateFees() {
        BarSeries baseBarSeries = tradingSystem.getBarSeriesProvider().getBaseBarSeries();
        Bar lastBar = baseBarSeries.getLastBar();
        ZonedDateTime openTime = lastBar.getBeginTime();
        ZonedDateTime closeTime = lastBar.getEndTime();
        ZonedDateTime startOfDayOnOpen = openTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime checkPoint = startOfDayOnOpen;

        int fundingRatesHappened = 0;

        switch (fundingRate.getPeriodicCostInterval()) {
            case EIGHT_HOURS:
                if (openTime.equals(checkPoint)) {
                    fundingRatesHappened++;
                }
                while (checkPoint.isBefore(closeTime)) {
                    checkPoint = checkPoint.plusHours(8);
                    if (checkPoint.isBefore(closeTime) && openTime.isBefore(checkPoint)) {
                        fundingRatesHappened++;
                    }
                }
            case DAILY:
                if (openTime.equals(checkPoint)) {
                    fundingRatesHappened++;
                }
                while (checkPoint.isBefore(closeTime)) {
                    checkPoint = checkPoint.plusHours(24);
                    if (checkPoint.isBefore(closeTime) && openTime.isBefore(checkPoint)) {
                        fundingRatesHappened++;
                    }
                }
        }
        Position runningPosition = tradingSystem.getRunningPosition();
        Num fundingFee = fundingRate.getFundingRateValue(runningPosition, lastBar.getClosePrice()).multipliedBy(NumUtil.getNum(fundingRatesHappened));
        reduceFundingFee(fundingFee);
    }

    private void reduceFundingFee(Num fundingFee) {
        tradingSystem.reduceBalance(fundingFee);
        this.totalFundingFees = totalFundingFees.plus(fundingFee);
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

    private void analyzeOrders() {
        Num currentClose = tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar().getClosePrice();
        ZonedDateTime currentTime = tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar().getEndTime();
        checkOrdersForExpirations(tradingSystem.getActiveOrders(),currentTime);
        List<Order> stopMarketOrders = OrderUtil.getSelectedOrders(tradingSystem.getActiveOrders(), OrderType.STOP_MARKET, currentClose);
        List<Order> limitOrders = OrderUtil.getSelectedOrders(tradingSystem.getActiveOrders(), OrderType.LIMIT, currentClose);

        stopMarketOrders.forEach(this::processOrder);
        limitOrders.forEach(this::processOrder);
        tradingSystem.onEveryCandle();
        if (tradingSystem.getBarSeriesProvider().getBaseBarSeries().getBarCount()>tradingSystem.getStartIndexBackTest()){
            tradingSystem.onBuyCondition();
            tradingSystem.onSellCondition();
        }
        if (tradingSystem.getRunningPosition().isLong()) {
            tradingSystem.onExitBuyCondition();
        }
        if (tradingSystem.getRunningPosition().isShort()) {
            tradingSystem.onExitSellCondition();
        }
        List<Order> marketOrders = OrderUtil.getSelectedOrders(tradingSystem.getActiveOrders(), OrderType.MARKET, currentClose);
        marketOrders.forEach(this::processOrder);
    }

    private void checkOrdersForExpirations(List<Order> orders,ZonedDateTime currentTime) {
        List<Order> ordersToDelete = new ArrayList<>();
        for (Order order : orders) {
            if (order.getExpiration()!=null){
                if (order.getExpiration().isBefore(currentTime)){
                    ordersToDelete.add(order);
                }
            }
        }
        for (Order orderToDelete : ordersToDelete) {
            tradingSystem.clearOrder(orderToDelete);
        }
    }

    private void processOrders() {
        analyzeOrders();
    }

    private void processOrder(Order order) {
        if (order == null || order.getVolume().isZero()) return;
        Num orderVolume = order.getVolume();
        Bar lastCandle = this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        int index = this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getBarCount()-1;
        Position position = this.tradingSystem.getRunningPosition();
        Num executedPrice;
        Num newNetSize;

        switch (order.getOrderType()) {
            case LIMIT: {
                Num orderPrice = order.getPrice();
                executedPrice = orderPrice;
                if (orderVolume.isPositive()) {
                    //closing short position
                    if (spread.getAskPrice(lastCandle.getLowPrice()).isGreaterThan(orderPrice)) return;
                    newNetSize = position.getSize().plus(orderVolume);
                    if (order.isReduceOnly()) {
                        newNetSize = newNetSize.min(NumUtil.getNum(0));
                    }
                } else {
                    //closing long position
                    if (spread.getBidPrice(lastCandle.getHighPrice()).isLessThan(orderPrice)) return;
                    newNetSize = position.getSize().plus(orderVolume);
                    if (order.isReduceOnly()) {
                        newNetSize = newNetSize.max(NumUtil.getNum(0));
                    }
                }
                if (order.getOrderExecutionCallback()!=null){
                    order.getOrderExecutionCallback().doAfterOrderExecution();
                }
                reduceCommission(orderPrice, order);
                break;
            }
            case MARKET: {
                if (orderVolume.isPositive()) {
                    executedPrice = slippage.getSlippedPrice(spread.getAskPrice(lastCandle.getClosePrice()), order);
                    newNetSize = position.getSize().plus(orderVolume);
                    if (order.isReduceOnly()) {
                        newNetSize = newNetSize.min(NumUtil.getNum(0));
                    }
                } else {
                    executedPrice = slippage.getSlippedPrice(spread.getBidPrice(lastCandle.getClosePrice()), order);
                    newNetSize = position.getSize().plus(orderVolume);
                    if (order.isReduceOnly()) {
                        newNetSize = newNetSize.max(NumUtil.getNum(0));
                    }
                }
                if (order.getOrderExecutionCallback()!=null){
                    order.getOrderExecutionCallback().doAfterOrderExecution();
                }
                reduceCommission(executedPrice, order);
                break;
            }
            default:

            case STOP_MARKET: {
                if (orderVolume.isPositive()) {
                    //closing short position
                    if (spread.getAskPrice(lastCandle.getHighPrice()).isGreaterThan(order.getPrice()) && spread.getAskPrice(lastCandle.getOpenPrice()).isLessThan(order.getPrice())) {
                        executedPrice = slippage.getSlippedPrice(order.getPrice(), order);
                    } else if (spread.getAskPrice(lastCandle.getHighPrice()).isGreaterThan(order.getPrice()) && spread.getAskPrice(lastCandle.getOpenPrice()).isGreaterThan(order.getPrice())) {
                        executedPrice = slippage.getSlippedPrice(spread.getAskPrice(lastCandle.getOpenPrice()), order);
                    } else {
                        return;
                    }
                    newNetSize = position.getSize().plus(orderVolume);
                    if (order.isReduceOnly()) {
                        newNetSize = newNetSize.min(NumUtil.getNum(0));
                    }
                } else {
                    //closing long position
                    if (spread.getBidPrice(lastCandle.getLowPrice()).isLessThan(order.getPrice()) && spread.getBidPrice(lastCandle.getOpenPrice()).isGreaterThan(order.getPrice())) {
                        executedPrice = slippage.getSlippedPrice(order.getPrice(), order);
                    } else if (spread.getBidPrice(lastCandle.getLowPrice()).isLessThan(order.getPrice()) && spread.getBidPrice(lastCandle.getOpenPrice()).isLessThan(order.getPrice())) {
                        executedPrice = slippage.getSlippedPrice(spread.getBidPrice(lastCandle.getOpenPrice()), order);
                    } else {
                        return;
                    }
                    newNetSize = position.getSize().plus(orderVolume);
                    if (order.isReduceOnly()) {
                        newNetSize = newNetSize.max(NumUtil.getNum(0));
                    }
                }
                if (order.getOrderExecutionCallback()!=null){
                    order.getOrderExecutionCallback().doAfterOrderExecution();
                }
                reduceCommission(executedPrice, order);
                break;
            }
        }

        Num deltaPositionSize = newNetSize.minus(position.getSize());

        TradeLog tradeLog;
        if (newNetSize.abs().isLessThan(position.getSize().abs())) {
            //this means that position size was reduced
            Num realizedProfitAndLoss = deltaPositionSize.multipliedBy(position.getAverageEntryPrice().minus(executedPrice));
            tradingSystem.addBalance(realizedProfitAndLoss);
            tradeLog = TradeLog.createLog(tradingSystem.getSymbol(), deltaPositionSize, executedPrice, lastCandle.getEndTime(), realizedProfitAndLoss, getBalance(),index);
        } else if (newNetSize.abs().isZero() && position.getSize().isZero()) {
            tradingSystem.clearOrder(order);
            return;
        } else {
            tradeLog = TradeLog.createLog(tradingSystem.getSymbol(), deltaPositionSize, executedPrice, lastCandle.getEndTime(), getBalance(),index);
        }


        tradeHistory.add(tradeLog);
        tradingSystem.updatePosition(deltaPositionSize, executedPrice);
        tradingSystem.clearOrder(order);

    }

    private void reduceCommission(Num executedPrice, Order order) {
        tradingSystem.reduceBalance(commission.getCostOfCommission(executedPrice, order));
        this.totalCommissions = totalCommissions.plus(commission.getCostOfCommission(executedPrice, order));
    }

    public List<TradeLog> getClosedTrades() {
        return tradeHistory.stream().filter(i -> !i.getRealizedProfitAndLoss().isZero()).collect(Collectors.toList());
    }

    public List<TradeLog> getAllTrades() {
        return tradeHistory;
    }


    private void checkLiquidation() {
        Bar lastBar = tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        if (tradingSystem.getRunningPosition().getUnrealizedProfitAndLoss(lastBar.getClosePrice()).plus(tradingSystem.getBalance()).isNegativeOrZero()) {
            log.error("Balance : " + tradingSystem.getBalance());
            log.error("Unrealized Profit&Loss : " + tradingSystem.getRunningPosition().getUnrealizedProfitAndLoss(getBarSeriesProvider().getBaseBarSeries().getLastBar().getClosePrice()));
            log.error("Current position : " + tradingSystem.getRunningPosition());
            throw new SimulatorException("Liquidated");
        }
    }

    protected BarSeriesProvider getBarSeriesProvider() {
        return this.tradingSystem.getBarSeriesProvider();
    }

}
