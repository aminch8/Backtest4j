package com.auctus.core.domains;

import com.auctus.core.domains.enums.TradeSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Trade {
    private String symbol;
    private Num entryPrice;
    private Num stopLoss;
    private Num takeProfit;
    private Num exitedPrice;
    private TradeSide tradeSide;
    private Num volume;
    private ZonedDateTime enteredDate;
    private ZonedDateTime exitedDate;
    public Num profitAbs(){
        Num profitInPoints;
        if (tradeSide==TradeSide.BUY){
            profitInPoints = exitedPrice.minus(entryPrice);
        }else {
            profitInPoints=entryPrice.minus(exitedPrice);
        }
        return profitInPoints.multipliedBy(volume);
    }
}
