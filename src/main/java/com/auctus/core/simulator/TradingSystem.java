package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.AbstractBarSeriesProvider;
import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.Order;
import com.auctus.core.domains.Position;

public class TradingSystem extends AbstractTradingSystem {


    public TradingSystem(AbstractBarSeriesProvider barSeriesProvider) {
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



}
