package com.auctus.core.utils;

import com.auctus.core.domains.Order;
import com.auctus.core.domains.enums.OrderType;
import org.ta4j.core.num.Num;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderUtil {


    public static List<Order> getSelectedOrders(List<Order> orders,OrderType orderType, Num latestPrice) {
        return orders.stream().filter(i->i.getOrderType()==orderType).sorted(
                (o1, o2) -> {
                    if ( o1.getPrice().minus(latestPrice).isGreaterThan(o2.getPrice().minus(latestPrice)) ){
                        return 1;
                    }else if (o1.getPrice().minus(latestPrice).isLessThan(o2.getPrice().minus(latestPrice))){
                        return -1;
                    }else {
                        return 0;
                    }
                }
        ).collect(Collectors.toList());
    }

}
