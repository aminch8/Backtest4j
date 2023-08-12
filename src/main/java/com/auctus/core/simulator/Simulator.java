package com.auctus.core.simulator;

import com.auctus.core.domains.*;
import com.auctus.core.domains.enums.OrderType;
import com.auctus.core.domains.enums.PeriodicCostInterval;
import com.auctus.core.exceptions.SimulatorException;
import com.auctus.core.utils.NumUtil;
import com.auctus.core.utils.OrderUtil;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.theme.Theme;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
            this.processTick();
        }
        log.info("Simulation Ended : " + this.tradingSystem.getSymbol());
        log.info("Total Bars Simulated : " + this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getBarCount());
        log.info("Ending Balance : " + this.tradingSystem.getBalance());
        log.info("Starting Balance : " + this.tradingSystem.getStartingBalance());
        log.info("Total trades taken : " + this.tradeHistory.size());
        for (TradeLog tradeLog : tradeHistory) {
            System.out.println(tradeLog);
        }
        log.info("Total number of balance snapshots : " + balanceSnapshots.size() );
        log.info("Maximum drawdown : " + getMaximumDrawDown() + "%");
        log.info("Profit factor is : " + getProfitFactor());
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

    private void analyzeOrders(List<Order> orders) {
        Num currentClose = tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar().getClosePrice();
        List<Order> stopMarketOrders = OrderUtil.getSelectedOrders(orders, OrderType.STOP_MARKET,currentClose);
        List<Order> limitOrders = OrderUtil.getSelectedOrders(orders,OrderType.LIMIT,currentClose);

        for (Order order : orders) {
            log.debug("Debugging orders order : " + order.getOrderType());
            log.debug("Debugging orders order : " + order.isReduceOnly());
            log.debug("--------------");
        }

        stopMarketOrders.forEach(this::processOrder);
        limitOrders.forEach(this::processOrder);
        tradingSystem.onBuyCondition();
        tradingSystem.onSellCondition();
        if (tradingSystem.getRunningPosition().isLong()){
            tradingSystem.onExitBuyCondition();
        }
        if (tradingSystem.getRunningPosition().isShort()){
            tradingSystem.onExitSellCondition();
        }
        List<Order> marketOrders = OrderUtil.getSelectedOrders(orders,OrderType.MARKET,currentClose);
        marketOrders.forEach(this::processOrder);
    }

    private void processOrders() {
        List<Order> activeOrders = tradingSystem.getActiveOrders();
        analyzeOrders(activeOrders);
    }

    private void processOrder(Order order) {
        if (order == null || order.getVolume().isZero()) return;
        Num orderVolume = order.getVolume();
        Bar lastCandle = this.tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        Position position = this.tradingSystem.getRunningPosition();
        Num executedPrice;
        Num newNetSize;

        switch (order.getOrderType()) {
            case LIMIT:{
                Num orderPrice = order.getPrice();
                executedPrice = orderPrice;
                if (orderVolume.isPositive()) {
                    //closing short position
                    if (lastCandle.getLowPrice().isGreaterThan(orderPrice)) return;
                    newNetSize = position.getSize().plus(orderVolume);
                    if(order.isReduceOnly()){
                        newNetSize = newNetSize.min(NumUtil.getNum(0));
                    }
                    tradingSystem.reduceBalance(commission.getCostOfCommission(orderPrice, order));
                } else {
                    //closing long position
                    if (lastCandle.getHighPrice().isLessThan(orderPrice)) return;
                    newNetSize = position.getSize().plus(orderVolume);
                    if(order.isReduceOnly()){
                        newNetSize = newNetSize.max(NumUtil.getNum(0));
                    }
                    tradingSystem.reduceBalance(commission.getCostOfCommission(orderPrice, order));
                }
                break;
            }
            case MARKET:{
                executedPrice = slippage.getSlippedPrice(lastCandle.getClosePrice(), order);
                if (orderVolume.isPositive()) {
                    newNetSize = position.getSize().plus(orderVolume);
                    if(order.isReduceOnly()){
                        newNetSize = newNetSize.min(NumUtil.getNum(0));
                    }
                    tradingSystem.reduceBalance(commission.getCostOfCommission(executedPrice, order));
                } else {
                    newNetSize = position.getSize().plus(orderVolume);
                    if(order.isReduceOnly()){
                        newNetSize = newNetSize.max(NumUtil.getNum(0));
                    }
                    tradingSystem.reduceBalance(commission.getCostOfCommission(executedPrice, order));
                }
                break;
            }
            default:

            case STOP_MARKET:{
                if (orderVolume.isPositive()){
                    //closing short position
                    if (lastCandle.getHighPrice().isGreaterThan(order.getPrice()) && lastCandle.getOpenPrice().isLessThan(order.getPrice())){
                        executedPrice = slippage.getSlippedPrice(order.getPrice(),order);
                    }else if (lastCandle.getHighPrice().isGreaterThan(order.getPrice()) && lastCandle.getOpenPrice().isGreaterThan(order.getPrice())){
                        executedPrice = slippage.getSlippedPrice(lastCandle.getOpenPrice(),order);
                    }else {
                        return;
                    }
                    newNetSize = position.getSize().plus(orderVolume);
                    if(order.isReduceOnly()){
                        newNetSize = newNetSize.min(NumUtil.getNum(0));
                    }
                    tradingSystem.reduceBalance(commission.getCostOfCommission(executedPrice, order));
                }else {
                    //closing long position
                    if (lastCandle.getHighPrice().isGreaterThan(order.getPrice()) && lastCandle.getOpenPrice().isLessThan(order.getPrice())){
                        executedPrice = slippage.getSlippedPrice(order.getPrice(),order);
                    }else if (lastCandle.getHighPrice().isGreaterThan(order.getPrice()) && lastCandle.getOpenPrice().isGreaterThan(order.getPrice())){
                        executedPrice = slippage.getSlippedPrice(lastCandle.getOpenPrice(),order);
                    }else {
                        return;
                    }
                    newNetSize = position.getSize().plus(orderVolume);
                    if(order.isReduceOnly()){
                        newNetSize = newNetSize.max(NumUtil.getNum(0));
                    }
                    tradingSystem.reduceBalance(commission.getCostOfCommission(executedPrice, order));
                }
                break;
            }
        }

        Num deltaPositionSize = newNetSize.minus(position.getSize());


        if (position.getSize().abs().isGreaterThan(newNetSize.abs())){
            //this means that position size was reduced
            Num realizedProfitAndLoss = deltaPositionSize.multipliedBy(position.getAverageEntryPrice().minus(executedPrice));
            System.out.println(realizedProfitAndLoss);
            tradingSystem.addBalance(realizedProfitAndLoss);
        }

        TradeLog tradeLog = TradeLog.createLog(tradingSystem.getSymbol(), deltaPositionSize, executedPrice, lastCandle.getEndTime());
        tradeHistory.add(tradeLog);
        tradingSystem.updatePosition(deltaPositionSize,executedPrice);
        tradingSystem.clearOrder(order);

    }

    public void generateBalanceDiagrams(boolean saveToFile) {
        List<Number> xData = new ArrayList<>();
        List<Number> balance = new ArrayList<>();
        List<Number> equity = new ArrayList<>();
        for (BalanceSnapshot balanceSnapshot : balanceSnapshots) {
            balance.add(balanceSnapshot.getBalanceRPNL().doubleValue());
            equity.add(balanceSnapshot.getBalanceUPNL().doubleValue());
            xData.add(balance.size());
        }
        XYChart balanceAndEquityChart = new XYChartBuilder().xAxisTitle("Bar")
                .yAxisTitle("Balance")
                .width(1920)
                .height(1080).build();
        balanceAndEquityChart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        XYSeries balanceSeries = balanceAndEquityChart.addSeries("Balance",xData,balance);
        XYSeries equitySeries = balanceAndEquityChart.addSeries("Equity",xData,equity);
        balanceSeries.setSmooth(true);
        balanceSeries.setLineStyle(SeriesLines.SOLID);
        balanceSeries.setLineColor(XChartSeriesColors.BLACK);
        balanceSeries.setMarkerColor(XChartSeriesColors.BLACK);
        balanceSeries.setFillColor(XChartSeriesColors.BLACK);
        balanceSeries.setMarker(SeriesMarkers.NONE);

        equitySeries.setSmooth(true);
        equitySeries.setLineStyle(SeriesLines.SOLID);
        equitySeries.setLineColor(XChartSeriesColors.YELLOW);
        equitySeries.setMarkerColor(XChartSeriesColors.YELLOW);
        equitySeries.setFillColor(XChartSeriesColors.YELLOW);
        equitySeries.setMarker(SeriesMarkers.NONE);

        balanceAndEquityChart.getStyler().setCursorEnabled(true);


        new SwingWrapper(balanceAndEquityChart).displayChart();

        if (saveToFile){
            try {
                BitmapEncoder.saveBitmapWithDPI(balanceAndEquityChart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG,300);
            } catch (IOException e) {
                log.error("Error in saving diagrams files..." + e);
            }
        }

    }

    private Num getMaximumDrawDown() {
        Num maximumDrawDown = NumUtil.getNum(0);
        Num maximumBalance = NumUtil.getNum(tradingSystem.getStartingBalance());
        Num minimumBalance = NumUtil.getNum(tradingSystem.getStartingBalance());
        for (BalanceSnapshot snapshot : balanceSnapshots) {
            Num balanceRealized = snapshot.getBalanceRPNL();
            Num balanceUnrealized = snapshot.getBalanceUPNL();
            if (balanceRealized.isGreaterThan(maximumBalance) || balanceUnrealized.isGreaterThan(maximumBalance)) {
                maximumBalance = balanceRealized.max(balanceUnrealized);
                minimumBalance = maximumBalance;
            }

            if (balanceRealized.isLessThan(minimumBalance) || balanceUnrealized.isLessThan(minimumBalance)) {
                minimumBalance = balanceRealized.min(balanceUnrealized);
            }
            maximumDrawDown = maximumDrawDown.max(NumUtil.getNum(1).minus(minimumBalance.dividedBy(maximumBalance)));
        }
        return maximumDrawDown.multipliedBy(NumUtil.getNum(100));
    }

    private Num getProfitFactor(){
        Num balanceLost = NumUtil.getNum(0);
        Num balanceGained = NumUtil.getNum(0);
        for (int index=1;index<balanceSnapshots.size();index++){
            BalanceSnapshot previousSnapshot = balanceSnapshots.get(index-1);
            BalanceSnapshot currentSnapshot =balanceSnapshots.get(index);
            if (currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()).isPositive()){
                balanceGained = balanceGained.plus(currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()));
            }else if (currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()).isNegative()){
                balanceLost =  balanceLost.plus(currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()));
            }
        }
        return balanceGained.dividedBy(balanceLost.abs());
    }

    private void checkLiquidation() {
        Bar lastBar = tradingSystem.getBarSeriesProvider().getBaseBarSeries().getLastBar();
        if (tradingSystem.getRunningPosition().getUnrealizedProfitAndLoss(lastBar.getClosePrice()).plus(tradingSystem.getBalance()).isNegativeOrZero()) {
            System.out.println(tradingSystem.getRunningPosition().getUnrealizedProfitAndLoss(lastBar.getClosePrice()));
            System.out.println((tradingSystem.getBalance()));
            throw new SimulatorException("Liquidated");
        }
    }

}
