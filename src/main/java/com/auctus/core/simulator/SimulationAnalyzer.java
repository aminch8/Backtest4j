package com.auctus.core.simulator;

import com.auctus.core.domains.BalanceSnapshot;
import com.auctus.core.domains.TradeLog;
import com.auctus.core.exceptions.SimulatorException;
import com.auctus.core.utils.NumUtil;
import com.auctus.core.utils.PlotterUtil;
import com.auctus.core.utils.ZDTUtil;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.*;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Slf4j
public class SimulationAnalyzer {


    private Simulator<? extends AbstractTradingSystem> simulator;


    public SimulationAnalyzer(Simulator<? extends AbstractTradingSystem> simulator) {
        this.simulator = simulator;
        if (!simulator.isSimulationComplete()) {
            throw new SimulatorException("Simulator needs to be completed before analyzing...");
        }
    }

    private SimulationAnalyzer() {
    }


    public void generateBalanceDiagrams(boolean saveToFile, boolean showBuyAndHold) {
        List<Number> xData = new ArrayList<>();
        List<Number> balance = new ArrayList<>();
        List<Number> equity = new ArrayList<>();
        List<Number> buyAndHold = new ArrayList<>();
        Num openPriceOfSimluation = simulator.getBarSeriesProvider().getBaseBarSeries().getFirstBar().getOpenPrice();
        int indexOfBuyAndHold = 1;
        for (BalanceSnapshot balanceSnapshot : simulator.getBalanceSnapshots()) {
            balance.add(balanceSnapshot.getBalanceRPNL().doubleValue());
            equity.add(balanceSnapshot.getBalanceUPNL().doubleValue());
            Num latestPrice = simulator.getBarSeriesProvider().getBaseBarSeries().getBar(indexOfBuyAndHold).getClosePrice();
            buyAndHold.add(
                    simulator.getStartingBalance().multipliedBy(latestPrice.dividedBy(openPriceOfSimluation)).doubleValue()
            );
            xData.add(balance.size());
            indexOfBuyAndHold++;
        }
        XYChart balanceAndEquityChart = new XYChartBuilder().xAxisTitle("Bar")
                .yAxisTitle("Balance")
                .width(1920)
                .height(1080).build();
        balanceAndEquityChart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        XYSeries balanceSeries = balanceAndEquityChart.addSeries("Balance", xData, balance);
        XYSeries equitySeries = balanceAndEquityChart.addSeries("Equity", xData, equity);


        balanceSeries.setSmooth(true);
        balanceSeries.setLineStyle(SeriesLines.SOLID);
        balanceSeries.setLineColor(XChartSeriesColors.BLUE);
        balanceSeries.setMarkerColor(XChartSeriesColors.BLUE);
        balanceSeries.setFillColor(XChartSeriesColors.BLUE);
        balanceSeries.setMarker(SeriesMarkers.NONE);

        equitySeries.setSmooth(true);
        equitySeries.setLineStyle(SeriesLines.SOLID);
        equitySeries.setLineColor(XChartSeriesColors.YELLOW);
        equitySeries.setMarkerColor(XChartSeriesColors.YELLOW);
        equitySeries.setFillColor(XChartSeriesColors.YELLOW);
        equitySeries.setMarker(SeriesMarkers.NONE);

        if (showBuyAndHold) {
            XYSeries buyAndHoldSeries = balanceAndEquityChart.addSeries("Buy&Hold", xData, buyAndHold);
            buyAndHoldSeries.setSmooth(true);
            buyAndHoldSeries.setLineStyle(SeriesLines.SOLID);
            buyAndHoldSeries.setLineColor(XChartSeriesColors.BLACK);
            buyAndHoldSeries.setMarkerColor(XChartSeriesColors.BLACK);
            buyAndHoldSeries.setFillColor(XChartSeriesColors.BLACK);
            buyAndHoldSeries.setMarker(SeriesMarkers.NONE);
        }

        balanceAndEquityChart.getStyler().setCursorEnabled(true);
        balanceAndEquityChart.getStyler().setZoomEnabled(true);
        balanceAndEquityChart.getStyler().setZoomResetByButton(true);
        balanceAndEquityChart.getStyler().setZoomResetByDoubleClick(true);
        balanceAndEquityChart.getStyler().setZoomSelectionColor(XChartSeriesColors.CYAN);


        new SwingWrapper(balanceAndEquityChart).isCentered(true).displayChart();

        if (saveToFile) {
            try {
                BitmapEncoder.saveBitmapWithDPI(balanceAndEquityChart, "./Balance", BitmapEncoder.BitmapFormat.PNG, 300);
            } catch (IOException e) {
                log.error("Error in saving diagrams files..." + e);
            }
        }

    }

    public void generateTradesDistributionDiagram(boolean saveToFile) {
        CategoryChart categoryChart =
                new CategoryChartBuilder().xAxisTitle("Trades").yAxisTitle("Profits").height(1080).width(1920).title("Profit Distribution").build();
        List<Double> profitsByPercentage =
                simulator.getClosedTrades().stream().map(i -> i.getRealizedProfitAndLoss().dividedBy(i.getBalance().minus(i.getRealizedProfitAndLoss())).doubleValue() * 100).collect(Collectors.toList());
        List<Long> tradeNumber = new ArrayList<>();
        for (int index = 0; index < profitsByPercentage.size(); index++) {
            tradeNumber.add(index + 1L);
        }
        categoryChart.addSeries("Profits Percentage", tradeNumber, profitsByPercentage);

        new SwingWrapper(categoryChart).isCentered(true).displayChart();

        if (saveToFile) {
            try {
                BitmapEncoder.saveBitmapWithDPI(categoryChart, "./Trades", BitmapEncoder.BitmapFormat.PNG, 300);
            } catch (IOException e) {
                log.error("Error in saving diagrams files..." + e);
            }
        }

    }

    public Num getMaximumDrawDown() {
        Num maximumDrawDown = NumUtil.getNum(0);
        Num maximumBalance = simulator.getStartingBalance();
        Num minimumBalance = simulator.getStartingBalance();
        for (BalanceSnapshot snapshot : simulator.getBalanceSnapshots()) {
            Num balanceRealized = snapshot.getBalanceRPNL();
            if (balanceRealized.isGreaterThan(maximumBalance)) {
                maximumBalance = balanceRealized;
                minimumBalance = maximumBalance;
            }
            if (balanceRealized.isLessThan(minimumBalance)) {
                minimumBalance = balanceRealized;
            }

            maximumDrawDown = maximumDrawDown.max(NumUtil.getNum(1).minus(minimumBalance.dividedBy(maximumBalance)));
        }
        return maximumDrawDown.multipliedBy(NumUtil.getNum(100));
    }

    public Num getProfitFactor() {
        Num balanceLost = NumUtil.getNum(0);
        Num balanceGained = NumUtil.getNum(0);
        for (int index = 1; index < simulator.getBalanceSnapshots().size(); index++) {
            BalanceSnapshot previousSnapshot = simulator.getBalanceSnapshots().get(index - 1);
            BalanceSnapshot currentSnapshot = simulator.getBalanceSnapshots().get(index);
            if (currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()).isPositive()) {
                balanceGained = balanceGained.plus(currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()));
            } else if (currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()).isNegative()) {
                balanceLost = balanceLost.plus(currentSnapshot.getBalanceRPNL().minus(previousSnapshot.getBalanceRPNL()));
            }
        }
        return balanceGained.dividedBy(balanceLost.abs());
    }

    public int getTotalNumberOfTransactions() {
        return simulator.getTradeHistory().size();
    }

    public long getTotalTrades() {
        return simulator.getTradeHistory().stream().filter(i -> i.getRealizedProfitAndLoss() != null).count();
    }

    public long getTotalWinningTrades() {
        return simulator.getTradeHistory().stream().filter(i -> i.getRealizedProfitAndLoss() != null && i.getRealizedProfitAndLoss().isPositive()).count();
    }

    public long getTotalLosingTrades() {
        return simulator.getTradeHistory().stream().filter(i -> i.getRealizedProfitAndLoss() != null && i.getRealizedProfitAndLoss().isNegative()).count();
    }

    public double getWinningRatePercent() {
        return (double) getTotalWinningTrades() / (double) getTotalTrades() * 100;
    }


    public Num getCAGR() {
        BalanceSnapshot firstSnapshot = simulator.getBalanceSnapshots().get(0);
        BalanceSnapshot lastSnapshot = simulator.getBalanceSnapshots().get(simulator.getBalanceSnapshots().size() - 1);
        double years = (double) ZDTUtil.zonedDateTimeDifference(firstSnapshot.getTime(), lastSnapshot.getTime(), ChronoUnit.DAYS) / 365d;
        return lastSnapshot.getBalanceRPNL().dividedBy(firstSnapshot.getBalanceRPNL())
                .pow(NumUtil.getNum(1d / years)).minus(NumUtil.getNum(1)).multipliedBy(NumUtil.getNum(100));

    }

    public Num getReturnRaw() {
        return simulator.getBalance().minus(simulator.getStartingBalance());
    }

    public Num getReturnPercent() {
        return simulator.getBalance().dividedBy(simulator.getStartingBalance()).minus(NumUtil.getNum(1)).multipliedBy(NumUtil.getNum(100));
    }

    public Num getAverageWinningTradePercent(){
        List<Num> tradeReturnsPercent = simulator.getClosedTrades().stream()
                .filter(i->i.getRealizedProfitAndLoss().isPositive())
                .map(
                        i->i.getRealizedProfitAndLoss().dividedBy(i.getBalance().minus(i.getRealizedProfitAndLoss()))
                ).collect(Collectors.toList());
        Num averageWinningTradeReturnPercent = NumUtil.getNum(0);
        for (Num returnPercent : tradeReturnsPercent) {
            averageWinningTradeReturnPercent = averageWinningTradeReturnPercent.plus(returnPercent);
        }
        return averageWinningTradeReturnPercent.dividedBy(NumUtil.getNum(tradeReturnsPercent.size())).multipliedBy(NumUtil.getNum(100));
    }

    public Num getAverageLosingTradePercent(){
        List<Num> tradeReturnsPercent = simulator.getClosedTrades().stream()
                .filter(i->i.getRealizedProfitAndLoss().isNegative())
                .map(
                        i->i.getRealizedProfitAndLoss().dividedBy(i.getBalance().minus(i.getRealizedProfitAndLoss()))
                ).collect(Collectors.toList());
        Num averageLosingTradeReturnPercent = NumUtil.getNum(0);
        for (Num returnPercent : tradeReturnsPercent) {
            averageLosingTradeReturnPercent = averageLosingTradeReturnPercent.plus(returnPercent);
        }
        return averageLosingTradeReturnPercent.dividedBy(NumUtil.getNum(tradeReturnsPercent.size())).multipliedBy(NumUtil.getNum(100));
    }

    public Num getAverageTradeReturnPercent(){
        List<Num> tradeReturnsPercent = simulator.getClosedTrades().stream()
                .map(
                        i->i.getRealizedProfitAndLoss().dividedBy(i.getBalance().minus(i.getRealizedProfitAndLoss()))
                ).collect(Collectors.toList());
        Num averageTradeReturnPercent = NumUtil.getNum(0);
        for (Num returnPercent : tradeReturnsPercent) {
            averageTradeReturnPercent = averageTradeReturnPercent.plus(returnPercent);
        }
        return averageTradeReturnPercent.dividedBy(NumUtil.getNum(tradeReturnsPercent.size())).multipliedBy(NumUtil.getNum(100));
    }

    public Num getAverageDrawdown(){
        Num sumDrawdown = NumUtil.getNum(0);
        List<Num> drawdowns = this.getListOfDrawdowns();
        for (Num drawdow : drawdowns) {
            sumDrawdown = sumDrawdown.plus(drawdow);
        }
        return sumDrawdown.dividedBy(NumUtil.getNum(drawdowns.size())).multipliedBy(NumUtil.getNum(100));
    }

    public List<Num> getListOfDrawdowns(){
        List<Num> drawdowns = new ArrayList<>();
        Num maximumBalance = simulator.getStartingBalance();
        Num minimumBalance = simulator.getStartingBalance();
        for (BalanceSnapshot snapshot : simulator.getBalanceSnapshots()) {
            Num balanceRealized = snapshot.getBalanceRPNL();
            if (balanceRealized.isGreaterThan(maximumBalance)) {

                if (!maximumBalance.isEqual(minimumBalance)){
                    drawdowns.add(NumUtil.getNum(1).minus(minimumBalance.dividedBy(maximumBalance)));
                }

                maximumBalance = balanceRealized;
                minimumBalance = maximumBalance;
            }
            if (balanceRealized.isLessThan(minimumBalance)) {
                minimumBalance = balanceRealized;
            }
        }
        return drawdowns;
    }

    public Num getDrawdownStandardDeviation(){
        return NumUtil.getStandardDeviation(getListOfDrawdowns()).multipliedBy(NumUtil.getNum(100));
    }

    public Num getAverageTradeReturnPercentStandardDeviation(){
        List<Num> tradeReturnsPercent = simulator.getClosedTrades().stream()
                .map(
                        i->i.getRealizedProfitAndLoss().dividedBy(i.getBalance().minus(i.getRealizedProfitAndLoss()))
                ).collect(Collectors.toList());

        return NumUtil.getStandardDeviation(tradeReturnsPercent).multipliedBy(NumUtil.getNum(100));
    }

    public Num getTotalFundingRatesFees(){
        return this.simulator.getTotalFundingFees();
    }

    public Num getTotalCommissions(){
        return this.simulator.getTotalCommissions();
    }

    public Num getTimeInMarket() {
        List<BalanceSnapshot> balanceSnapshots =
                this.simulator.getBalanceSnapshots();
        long count = balanceSnapshots.stream().filter(i -> !i.getBalanceUPNL().isEqual(i.getBalanceRPNL())).count();
        System.out.println(count);
        System.out.println(balanceSnapshots.size());
        return NumUtil.getNum(count).dividedBy(NumUtil.getNum(balanceSnapshots.size())).multipliedBy(NumUtil.getNum(100));
    }


    public void generateDrawdownDistributionDiagram(boolean saveToFile) {
        CategoryChart categoryChart =
                new CategoryChartBuilder().xAxisTitle("Drawdowns").yAxisTitle("Percentages").height(1080).width(1920).title("Drawdown Distribution").build();
        List<Double> profitsByPercentage =
                getListOfDrawdowns().stream().map(i -> i.multipliedBy(NumUtil.getNum(100)).doubleValue()).collect(Collectors.toList());
        List<Long> tradeNumber = new ArrayList<>();
        for (int index = 0; index < profitsByPercentage.size(); index++) {
            tradeNumber.add(index + 1L);
        }
        categoryChart.addSeries("Drawdowns Percentage", tradeNumber, profitsByPercentage);

        new SwingWrapper(categoryChart).isCentered(true).displayChart();

        if (saveToFile) {
            try {
                BitmapEncoder.saveBitmapWithDPI(categoryChart, "./Drawdown", BitmapEncoder.BitmapFormat.PNG, 300);
            } catch (IOException e) {
                log.error("Error in saving diagrams files..." + e);
            }
        }

    }

    public void showTrades() {
        BarSeries barSeries = this.simulator.getBarSeriesProvider().getBaseBarSeries();
        TradeLog entry = null;
        TradeLog exit = null;
        for (TradeLog trade: this.simulator.getAllTrades()) {
            if (trade.getRealizedProfitAndLoss().isZero()){
                entry = trade;
            }else {
                exit = trade;
                if (entry!=null){
                    PlotterUtil.plotOHLC(barSeries,entry,exit);
                    System.out.println("Press any key to go next trade...");
                    Scanner sc=new Scanner(System.in);
                    sc.nextLine();
                }
            }
        }
    }

    //todo: Mont-Carlo simulation, this part should be done lastly.


}
