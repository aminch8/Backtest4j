package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.AbstractBarSeriesProvider;
import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.*;
import com.auctus.core.domains.enums.PeriodicCostInterval;
import com.auctus.core.utils.NumUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Simulator<T extends AbstractTradingSystem> {

    private T tradingSystem;
    private Slippage slippage = Slippage.ofPercentPrice(0);
    private Commission commission = Commission.ofPercentPrice(0);
    private FundingRate fundingRate = FundingRate.ofPercentPrice(0, PeriodicCostInterval.EIGHT_HOURS);
    private List<TradeLog> tradeHistory = new ArrayList<>();

    public Simulator(T tradingSystem) {
        this.tradingSystem = tradingSystem;
    }


    private void simulateTick(){
        AbstractBarSeriesProvider barSeriesProvider = tradingSystem.getBarSeriesProvider();
        BarSeries barSeries = barSeriesProvider.getBaseBarSeries();
        Bar lastBar = barSeries.getLastBar();

        Position runningPosition = tradingSystem.getRunningPosition();
        processTick(lastBar,runningPosition);
        List<Order> activeOrders = tradingSystem.getActiveOrders();


        Order buyOrder = tradingSystem.onBuyCondition();
        Order exitBuyOrder = tradingSystem.onExitBuyCondition();
        Order sellOrder = tradingSystem.onSellCondition();
        Order exitSellOrder = tradingSystem.onExitSellCondition();

    }


    private void analyzeOrder(Order order){
        if (!order.isReduceOnly()){

        }else {

        }
    }

    private void processTick(Bar bar, Position position){
        if (position.getStopLoss().isZero() && position.getTakeProfit().isZero()) return;
        if (position.getSize().isZero()) return;

        Num positionSize = position.getSize();
        Num takeProfit = position.getTakeProfit();
        Num stoploss = position.getStopLoss();

        if (positionSize.isPositive()){

        }else {

        }

    }

}
