package com.auctus.core.domains;

import com.auctus.core.domains.enums.TimeFrame;
import com.auctus.core.utils.ZdtUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
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
    private int currentBarIndex = 1;

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

    public void tickForward(){
        currentBarIndex++;
        updateAllBarSeries();
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

    private void propagateBarSeriesByTick(TimeFrame timeFrame){
        if (timeFrame.getIndex() <= baseTimeFrame.getIndex()){
            throw new IllegalStateException("Wrong Timeframe given for chosen bar series");
        }
        BarSeries baseBarSeries = getBarSeries(baseTimeFrame);
        Bar bar = baseBarSeries.getBar(currentBarIndex);
        Bar previousBar = baseBarSeries.getBar(currentBarIndex-1);
        ZonedDateTime openTime = bar.getBeginTime();
        ZonedDateTime closeTime = bar.getEndTime();
        ZonedDateTime previousOpenTime = previousBar.getBeginTime();
        ZonedDateTime previousCloseTime = previousBar.getEndTime();

      switch (timeFrame){
          case M5:{
              if (
                      ZdtUtil.zonedDateTimeDifferenceInMinutes(previousOpenTime,openTime)>=5
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
          }
          case M15:{
              if (ZdtUtil.zonedDateTimeDifferenceInMinutes(previousOpenTime,openTime)>=15){
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
          }
          case M30:{
              if (ZdtUtil.zonedDateTimeDifferenceInMinutes(previousOpenTime,openTime)>=30){
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
          }
          case H1:{
              if (ZdtUtil.zonedDateTimeDifferenceInMinutes(previousOpenTime,openTime)>=60){
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
          }

          case H2:{
              if (ZdtUtil.zonedDateTimeDifferenceInMinutes(previousOpenTime,openTime)>=120 ){
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
          }

          case H4:{
              if ( ZdtUtil.zonedDateTimeDifferenceInMinutes(previousOpenTime,openTime)>=240 ){
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
          }

          case D1:{
              if ( openTime.getDayOfWeek().getValue()!=previousOpenTime.getDayOfWeek().getValue() ){
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
          }

          case W1:{
              if ( openTime.getDayOfWeek()==DayOfWeek.MONDAY && previousOpenTime.getDayOfWeek()!=DayOfWeek.MONDAY ){
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

          case Mo:{
              if (openTime.getDayOfMonth() == 1 && previousOpenTime.getDayOfMonth() != 1 ){
                  monthlyBarSeries.addBar(
                          Duration.ofDays(7),
                          closeTime.plusWeeks(1),
                          bar.getOpenPrice(),
                          bar.getHighPrice(),
                          bar.getLowPrice(),
                          bar.getClosePrice(),
                          bar.getVolume()
                  );
              } else {
                  monthlyBarSeries.addPrice(bar.getOpenPrice());
                  monthlyBarSeries.addPrice(bar.getHighPrice());
                  monthlyBarSeries.addPrice(bar.getLowPrice());
                  monthlyBarSeries.addPrice(bar.getClosePrice());
              }
          }

      }

    }

    private void updateAllBarSeries(){
        int indexOfTimeFrame = this.baseTimeFrame.getIndex();
        List<TimeFrame> timeFrames = Arrays.stream(TimeFrame.values()).collect(Collectors.toList());
        timeFrames.sort(Comparator.reverseOrder());
        for (int i=indexOfTimeFrame;i<TimeFrame.values().length;i++){
            TimeFrame selectedTimeFrame = timeFrames.get(i);
            propagateBarSeriesByTick(selectedTimeFrame);
        }
    }
}
