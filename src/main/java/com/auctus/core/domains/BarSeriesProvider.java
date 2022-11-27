package com.auctus.core.domains;

import com.auctus.core.domains.enums.TimeFrame;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BarSeriesProvider {

    private BarSeries oneMinuteBarSeries;
    private BarSeries fiveMinuteBarSeries;
    private BarSeries fifteenMinutesBarSeries;
    private BarSeries thirtyMinutesBarSeries;
    private BarSeries oneHourBarSeries;
    private BarSeries twoHourBarSeries;
    private BarSeries fourHourBarSeries;
    private BarSeries dailyBarSeries;
    private BarSeries weeklyBarSeries;
    private BarSeries monthlyBarSeries;

    private TimeFrame baseTimeFrame;
    private long maximumBars=0;
    private int currentBarIndex = 0;

    private List<TimeFrame> allowedTimeFrames = Arrays.stream(TimeFrame.values()).collect(Collectors.toList());

    public BarSeriesProvider(BarSeries barSeries, TimeFrame timeFrame) {
        this.baseTimeFrame = timeFrame;
        this.maximumBars = barSeries.getMaximumBarCount();
        allowedTimeFrames = allowedTimeFrames.subList(timeFrame.getIndex(),allowedTimeFrames.size());
        switch (timeFrame){
            case M1:
                this.oneMinuteBarSeries = barSeries;
            case M5:
                this.fiveMinuteBarSeries = barSeries;
            case M15:
                this.fifteenMinutesBarSeries = barSeries;
            case M30:
                this.thirtyMinutesBarSeries = barSeries;
            case H1:
                this.oneHourBarSeries = barSeries;
            case H2:
                this.twoHourBarSeries = barSeries;
            case H4:
                this.fourHourBarSeries = barSeries;
            case D1:
                this.dailyBarSeries = barSeries;
            case W1:
                this.weeklyBarSeries = barSeries;
            case Mo:
                this.monthlyBarSeries = barSeries;
            default:
                throw new IllegalStateException("Wrong Timeframe given for chosen bar series");
        }
    }

    public void tick(){














        currentBarIndex++;
    }


    public BarSeries getBarSeries(TimeFrame timeFrame){
        if (timeFrame.getIndex()<this.baseTimeFrame.getIndex()){
            throw new IllegalStateException("Accessing lower time frame than the main bar series is not possible");
        }
        switch (timeFrame){
            case M1:
                return this.oneMinuteBarSeries;
            case M5:
                return this.fiveMinuteBarSeries;
            case M15:
                return this.fifteenMinutesBarSeries;
            case M30:
                return this.thirtyMinutesBarSeries;
            case H1:
                return this.oneHourBarSeries;
            case H2:
                return this.twoHourBarSeries;
            case H4:
                return this.fourHourBarSeries;
            case D1:
                return this.dailyBarSeries;
            case W1:
                return this.weeklyBarSeries;
            case Mo:
                return this.monthlyBarSeries;
            default:
                throw new IllegalStateException("Wrong Timeframe given for chosen bar series");
        }

    }
    private void updateBarSeries(BarSeries barSeries,TimeFrame timeFrame){
        switch (timeFrame){
            case M1:
                this.oneMinuteBarSeries = barSeries;
            case M5:
                this.fiveMinuteBarSeries = barSeries;
            case M15:
                this.fifteenMinutesBarSeries = barSeries;
            case M30:
                this.thirtyMinutesBarSeries = barSeries;
            case H1:
                this.oneHourBarSeries = barSeries;
            case H2:
                this.twoHourBarSeries = barSeries;
            case H4:
                this.fourHourBarSeries = barSeries;
            case D1:
                this.dailyBarSeries = barSeries;
            case W1:
                this.weeklyBarSeries = barSeries;
            case Mo:
                this.monthlyBarSeries = barSeries;
            default:
                throw new IllegalStateException("Wrong Timeframe given for chosen bar series");
        }
    }

    private void updateFiveMinuteBarSeries(Bar bar,TimeFrame timeFrame){
        BarSeries fiveMinuteBarSeries = getBarSeries(timeFrame);


    }

    private void updateFifteenMinuteBarSeries(Bar bar,TimeFrame timeFrame){
        BarSeries fifteenMinutes = getBarSeries(timeFrame);
        ZonedDateTime openTime = bar.getBeginTime();
        ZonedDateTime closeTime = bar.getEndTime();

        if (openTime.getMinute()%15==0){
            fiveMinuteBarSeries.addBar(
                    Duration.ofMinutes(15),
                    closeTime.plusMinutes(5),
                    bar.getOpenPrice(),
                    bar.getHighPrice(),
                    bar.getLowPrice(),
                    bar.getClosePrice(),
                    bar.getVolume()
            );
        }else{
            fiveMinuteBarSeries.addPrice(bar.getOpenPrice());
            fiveMinuteBarSeries.addPrice(bar.getHighPrice());
            fiveMinuteBarSeries.addPrice(bar.getLowPrice());
            fiveMinuteBarSeries.addPrice(bar.getClosePrice());
        }

    }

    private void updateBarSeriesByTick(Bar bar,TimeFrame timeFrame){
        if (timeFrame.getIndex() <= baseTimeFrame.getIndex()){
            throw new IllegalStateException("Wrong Timeframe given for chosen bar series");
        }
        ZonedDateTime openTime = bar.getBeginTime();
        ZonedDateTime closeTime = bar.getEndTime();


      switch (timeFrame){
          case M5:
              ZonedDateTime previousOpenTime = fiveMinuteBarSeries.getLastBar().getBeginTime();
              ZonedDateTime previousCloseTime = fiveMinuteBarSeries.getLastBar().getEndTime();
              if (
                       openTime.getMinute() % 5==0
              )
              {
                  fiveMinuteBarSeries.addBar(
                          Duration.ofMinutes(5),
                          closeTime.plusMinutes(5),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  fiveMinuteBarSeries.addPrice(bar.getOpenPrice());
                  fiveMinuteBarSeries.addPrice(bar.getHighPrice());
                  fiveMinuteBarSeries.addPrice(bar.getLowPrice());
                  fiveMinuteBarSeries.addPrice(bar.getClosePrice());
              }
          case M15:
              ZonedDateTime previousOpenTime = fiveMinuteBarSeries.getLastBar().getBeginTime();
              ZonedDateTime previousCloseTime = fiveMinuteBarSeries.getLastBar().getEndTime();
              if (openTime.getMinute() % 15==0){
                  fifteenMinutesBarSeries.addBar(
                          Duration.ofMinutes(15),
                          closeTime.plusMinutes(15),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  fifteenMinutesBarSeries.addPrice(bar.getOpenPrice());
                  fifteenMinutesBarSeries.addPrice(bar.getHighPrice());
                  fifteenMinutesBarSeries.addPrice(bar.getLowPrice());
                  fifteenMinutesBarSeries.addPrice(bar.getClosePrice());
              }
          case M30:
              if (openTime.getMinute() % 30==0){
                  thirtyMinutesBarSeries.addBar(
                          Duration.ofMinutes(30),
                          closeTime.plusMinutes(30),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  thirtyMinutesBarSeries.addPrice(bar.getOpenPrice());
                  thirtyMinutesBarSeries.addPrice(bar.getHighPrice());
                  thirtyMinutesBarSeries.addPrice(bar.getLowPrice());
                  thirtyMinutesBarSeries.addPrice(bar.getClosePrice());
              }
          case H1:
              if (openTime.getMinute() == 0){
                  oneHourBarSeries.addBar(
                          Duration.ofMinutes(60),
                          closeTime.plusMinutes(60),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  oneHourBarSeries.addPrice(bar.getOpenPrice());
                  oneHourBarSeries.addPrice(bar.getHighPrice());
                  oneHourBarSeries.addPrice(bar.getLowPrice());
                  oneHourBarSeries.addPrice(bar.getClosePrice());
              }
          case H2:
              if (openTime.getHour() % 2 == 0 && openTime.getMinute()==0 ){
                  twoHourBarSeries.addBar(
                          Duration.ofHours(2),
                          closeTime.plusHours(2),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  twoHourBarSeries.addPrice(bar.getOpenPrice());
                  twoHourBarSeries.addPrice(bar.getHighPrice());
                  twoHourBarSeries.addPrice(bar.getLowPrice());
                  twoHourBarSeries.addPrice(bar.getClosePrice());
              }
          case H4:
              if (openTime.getHour() % 4 == 0 && openTime.getMinute()==0 ){
                  fourHourBarSeries.addBar(
                          Duration.ofHours(4),
                          closeTime.plusHours(4),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  fourHourBarSeries.addPrice(bar.getOpenPrice());
                  fourHourBarSeries.addPrice(bar.getHighPrice());
                  fourHourBarSeries.addPrice(bar.getLowPrice());
                  fourHourBarSeries.addPrice(bar.getClosePrice());
              }
          case D1:
              if (openTime.getHour() == 0 && openTime.getMinute()==0 ){
                  dailyBarSeries.addBar(
                          Duration.ofDays(1),
                          closeTime.plusDays(1),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  dailyBarSeries.addPrice(bar.getOpenPrice());
                  dailyBarSeries.addPrice(bar.getHighPrice());
                  dailyBarSeries.addPrice(bar.getLowPrice());
                  dailyBarSeries.addPrice(bar.getClosePrice());
              }
          case W1:
              if (openTime.getDayOfWeek() == DayOfWeek.MONDAY && openTime.getHour() == 0 && openTime.getMinute()==0 ){
                  weeklyBarSeries.addBar(
                          Duration.ofDays(7),
                          closeTime.plusWeeks(1),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  weeklyBarSeries.addPrice(bar.getOpenPrice());
                  weeklyBarSeries.addPrice(bar.getHighPrice());
                  weeklyBarSeries.addPrice(bar.getLowPrice());
                  weeklyBarSeries.addPrice(bar.getClosePrice());
              }
          case Mo:
              if (openTime.getDayOfMonth() == 1 && openTime.getHour() == 0 && openTime.getMinute()==0 ){
                  weeklyBarSeries.addBar(
                          Duration.ofDays(7),
                          closeTime.plusWeeks(1),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  weeklyBarSeries.addPrice(bar.getOpenPrice());
                  weeklyBarSeries.addPrice(bar.getHighPrice());
                  weeklyBarSeries.addPrice(bar.getLowPrice());
                  weeklyBarSeries.addPrice(bar.getClosePrice());
              }
      }

    }
}
