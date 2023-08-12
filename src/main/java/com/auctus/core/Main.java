package com.auctus.core;

import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.TradeLog;
import com.auctus.core.domains.enums.TimeFrame;
import com.auctus.core.services.BarService;
import com.auctus.core.simulator.SimulationAnalyzer;
import com.auctus.core.simulator.Simulator;
import com.auctus.core.strategies.BTCUSDTStrategy;
import com.mt5.core.enums.MT5TimeFrame;
import org.ta4j.core.BarSeries;

import java.time.ZonedDateTime;

public class Main {

    private static final BarService barService = new BarService();

    public static void main(String[] args) {
        BarSeries barSeries = barService.getBarSeries("BTCUSD", MT5TimeFrame.H1, ZonedDateTime.now().minusYears(1),ZonedDateTime.now());
        BarSeriesProvider barSeriesProvider = new BarSeriesProvider(barSeries, TimeFrame.H1,"BTCUSD");
        BTCUSDTStrategy btcusdtStrategy = new BTCUSDTStrategy(barSeriesProvider);
        Simulator<BTCUSDTStrategy> simulator = new Simulator<>(btcusdtStrategy);
        simulator.startSimulation();
        SimulationAnalyzer simulationAnalyzer = new SimulationAnalyzer(simulator);
        simulationAnalyzer.generateBalanceDiagrams(true,true);
        System.out.println("Winning rate percent : " + simulationAnalyzer.getWinningRatePercent());
    }
}
