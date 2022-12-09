package com.auctus.core.simulator;

import com.auctus.core.barseriesprovider.AbstractBarSeriesProvider;
import com.auctus.core.barseriesprovider.BarSeriesProvider;
import com.auctus.core.domains.Order;
import com.auctus.core.domains.Position;
import com.auctus.core.utils.NumUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTradingSystem<T extends AbstractBarSeriesProvider> {


    private List<Order> orders=new ArrayList<Order>();
    private Position position=new Position();
    @Getter
    private Num balance= NumUtil.getNum(this.startingBalance());

    public abstract Order onBuyCondition();
    public abstract Order onSellCondition();
    public abstract Order onExitBuyCondition();
    public abstract Order onExitSellCondition();
    public abstract Number startingBalance();

    @Getter
    private final T barSeriesProvider;

    public AbstractTradingSystem(T barSeriesProvider) {
        this.barSeriesProvider = barSeriesProvider;
    }

    public Position getRunningPosition() {
        return position;
    }

    public List<Order> getActiveOrders() {
        return orders;
    }

    public String getSymbol() {
        return getBarSeriesProvider().getSymbol();
    }

    public void addOrder(Order order){
        this.orders.add(order);
    }

    public void clearAllOrders(){
        this.orders=new ArrayList<Order>();
    }

    public void updatePosition(Position position){
        this.position = position;
    }

    public void addBalance(Num realizedProfitAndLoss){
        this.balance=this.balance.plus(realizedProfitAndLoss);
    }
    public void reduceBalance(Num reductionAmount){
        this.balance=this.balance.minus(reductionAmount);
    }
    public void reduceBalancePercent(Num reductionInPercent){
        this.balance=this.balance.multipliedBy(
                NumUtil.getNum(1)
                .minus(
                    reductionInPercent.dividedBy(NumUtil.getNum(100))
                )
        );
    }
}
