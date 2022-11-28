package com.auctus.core.domains;

import com.auctus.core.domains.enums.OrderType;
import lombok.Data;
import org.ta4j.core.num.Num;

@Data
public class Order {
    private OrderType orderType;
    private Num volume;
    private Num stopLoss;
    private Num takeProfit;
    private boolean reduceOnly=false;
}
