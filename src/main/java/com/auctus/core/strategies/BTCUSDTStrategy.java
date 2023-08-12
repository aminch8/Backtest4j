package com.auctus.core.strategies;

import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.Commission;
import com.auctus.core.domains.FundingRate;
import com.auctus.core.domains.Order;
import com.auctus.core.domains.Slippage;
import com.auctus.core.domains.enums.PeriodicCostInterval;
import com.auctus.core.simulator.AbstractTradingSystem;
import com.auctus.core.utils.NumUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;

public class BTCUSDTStrategy extends AbstractTradingSystem {


    private EMAIndicator shorterEMA;
    private EMAIndicator longerEMA;

    public BTCUSDTStrategy(BarSeriesProvider barSeriesProvider)
    {
        super(barSeriesProvider);
        this.shorterEMA=new EMAIndicator(new ClosePriceIndicator(getBarSeriesProvider().getBaseBarSeries()),50);
        this.longerEMA=new EMAIndicator(new ClosePriceIndicator(getBarSeriesProvider().getBaseBarSeries()),200);
    }

    @Override
    public void onBuyCondition()
    {
        int index = getBarSeriesProvider().getBaseBarSeries().getBarCount()-1;
        BarSeries baseBarSeries = getBarSeriesProvider().getBaseBarSeries();
        Bar lastBar = baseBarSeries.getLastBar();

        if (
                getRunningPosition().isEmpty() &&
                shorterEMA.getValue(index).isGreaterThan(longerEMA.getValue(index))
                &&
                        shorterEMA.getValue(index-1).isLessThan(longerEMA.getValue(index-1))
        )
        {
            Order order = Order.ofMarketInLong(getBalance().max(NumUtil.getNum(0)).dividedBy(lastBar.getClosePrice()));
            addOrder(order);
        }
    }

    @Override
    public void onSellCondition() {
    }

    @Override
    public void onExitBuyCondition() {
        int index = getBarSeriesProvider().getBaseBarSeries().getBarCount()-1;
        if (
                !getRunningPosition().isEmpty() &&
                        shorterEMA.getValue(index).isLessThan(longerEMA.getValue(index))
                        &&
                        shorterEMA.getValue(index-1).isGreaterThan(longerEMA.getValue(index-1))
        )
        {
            Order order =  Order.ofMarketOutLong(getRunningPosition().getSize());
            addOrder(order);
        }

    }

    @Override
    public void onExitSellCondition() {
    }

    @Override
    public Num getStartingBalance() {
        return NumUtil.getNum(1000);
    }


    @Override
    public FundingRate getFundingRate() {
        return FundingRate.ofPercentPrice(0.01, PeriodicCostInterval.EIGHT_HOURS);
    }

    @Override
    public Commission getCommission() {
        return Commission.ofPercentPrice(0.04);
    }

    @Override
    public Slippage getSlippage() {
        return Slippage.ofPercentPrice(0.04);
    }
}
