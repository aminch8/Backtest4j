package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.AbstractBarSeriesProvider;
import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.Order;
import com.auctus.core.domains.Position;
import lombok.AccessLevel;
import lombok.Getter;
import org.ta4j.core.num.Num;

import java.util.List;

public abstract class AbstractTradingSystem<T extends AbstractBarSeriesProvider> {

    private String symbol;
    private List<Order> orders;
    private Position position;
    private Num balance;

    public abstract Order onBuyCondition();
    public abstract Order onSellCondition();
    public abstract Order onExitBuyCondition();
    public abstract Order onExitSellCondition();

    @Getter
    private T barSeriesProvider;


    public AbstractTradingSystem(T barSeriesProvider , String symbol) {
        this.symbol = symbol;
        this.barSeriesProvider = barSeriesProvider;
    }

    public AbstractTradingSystem(T barSeriesProvider ) {
        this.barSeriesProvider = barSeriesProvider;
    }

    public Position getRunningPosition() {
        return position;
    }

    public List<Order> getActiveOrders() {
        return orders;
    }

    public String getSymbol() {
        return symbol;
    }
}
