package com.auctus.core.domains;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ta4j.core.num.Num;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private Num size;
    private String symbol;
    private Num averageEntryPrice;
    private Num stoploss;
    private Num limitOrder;
}
