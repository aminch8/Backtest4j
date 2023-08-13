package com.auctus.core.domains;

import com.auctus.core.domains.enums.TradeSide;
import com.auctus.core.utils.NumUtil;
import lombok.Data;
import lombok.ToString;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

@Data
@ToString
public class TradeLog {

    private String symbol;
    private TradeSide tradeSide;
    private Num volume;
    private Num price;
    private ZonedDateTime date;
    private Num realizedProfitAndLoss;
    private Num balance;


    public static TradeLog createLog(String symbol,Num executedVolume,Num price,ZonedDateTime time,Num currentBalance){
       return createLog(symbol,executedVolume,price,time,null,currentBalance);
    }

    public static TradeLog createLog(String symbol,Num executedVolume,Num price,ZonedDateTime time,Num realizedProfitAndLoss,Num currentBalance){
        TradeLog tradeLog = new TradeLog();
        if (executedVolume.isZero()) throw new IllegalStateException("Executed volume zero");
        tradeLog.setSymbol(symbol);
        tradeLog.setDate(time);
        tradeLog.setPrice(price);
        tradeLog.setVolume(executedVolume);
        tradeLog.setRealizedProfitAndLoss(realizedProfitAndLoss);
        tradeLog.setBalance(currentBalance);
        if (executedVolume.isPositive()){
            tradeLog.setTradeSide(TradeSide.BUY);
            return tradeLog;
        }else {
            tradeLog.setTradeSide(TradeSide.SELL);
            return tradeLog;
        }
    }

    private TradeLog(){

    }

}
