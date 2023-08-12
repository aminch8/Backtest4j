package com.auctus.core.services;


import com.mt5.core.clients.MT5Client;
import com.mt5.core.domains.Candle;
import com.mt5.core.domains.History;
import com.mt5.core.enums.MT5TimeFrame;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.sql.Date;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public class BarService {

    private final MT5Client mt5Client = new MT5Client.MT5ClientFactory(2201,2202).build();

    public BarSeries getBarSeries(String symbol, MT5TimeFrame timeFrame, ZonedDateTime fromDate, ZonedDateTime toDate){

        History history = mt5Client.getHistory(symbol, Date.from(fromDate.toInstant()),Date.from(toDate.toInstant()),timeFrame);

        List<Candle> candleList = history.getCandles();
        BarSeries barSeries = new BaseBarSeriesBuilder().build();
        for (Candle candle : candleList) {
            barSeries.addBar(
                    Duration.ofMinutes(60),
                    candle.getOpenTime().plusMinutes(60),
                    candle.getOpenPrice(),
                    candle.getHighPrice(),
                    candle.getLowPrice(),
                    candle.getClosePrice(),
                    candle.getVolume()
                    );
        }
        return barSeries;
    }

}
