package com.auctus.core;

import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.Commission;
import com.auctus.core.domains.FundingRate;
import com.auctus.core.domains.Order;
import com.auctus.core.domains.Slippage;
import com.auctus.core.simulator.AbstractTradingSystem;

public class BTCStrat extends AbstractTradingSystem {
    public BTCStrat(BarSeriesProvider barSeriesProvider) {
        super(barSeriesProvider);
    }

    @Override
    public Order onBuyCondition() {
        return null;
    }

    @Override
    public Order onSellCondition() {
        return null;
    }

    @Override
    public Order onExitBuyCondition() {
        return null;
    }

    @Override
    public Order onExitSellCondition() {
        return null;
    }

    @Override
    public Number startingBalance() {
        return null;
    }

    @Override
    public FundingRate getFundingRate() {
        return null;
    }

    @Override
    public Commission getCommission() {
        return null;
    }

    @Override
    public Slippage getSlippage() {
        return null;
    }
}
