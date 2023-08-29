package com.auctus.core.barseriesprovider;

import com.auctus.core.domains.enums.TimeFrame;
import com.auctus.core.utils.NumUtil;
import com.auctus.core.utils.ZDTUtil;
import lombok.Getter;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.Num;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class BaseBarSeriesProvider {

    private BarSeries completeBaseBarSeriesHolder;

    private BarSeries oneMinuteBarSeries = new BaseBarSeriesBuilder().withName("M1").build();
    private BarSeries fiveMinuteBarSeries = new BaseBarSeriesBuilder().withName("M5").build();
    private BarSeries fifteenMinutesBarSeries = new BaseBarSeriesBuilder().withName("M15").build();
    private BarSeries thirtyMinutesBarSeries = new BaseBarSeriesBuilder().withName("M30").build();
    private BarSeries oneHourBarSeries = new BaseBarSeriesBuilder().withName("H1").build();
    private BarSeries twoHourBarSeries = new BaseBarSeriesBuilder().withName("H2").build();
    private BarSeries fourHourBarSeries = new BaseBarSeriesBuilder().withName("H4").build();
    private BarSeries dailyBarSeries = new BaseBarSeriesBuilder().withName("D1").build();
    private BarSeries weeklyBarSeries = new BaseBarSeriesBuilder().withName("W1").build();
    private BarSeries monthlyBarSeries = new BaseBarSeriesBuilder().withName("M1").build();

    private TimeFrame baseTimeFrame;
    private long maximumBars=0;
    private int currentBarIndex = 0;
    @Getter
    private String symbol;

    private List<TimeFrame> allowedTimeFrames = Arrays.stream(TimeFrame.values()).collect(Collectors.toList());

    BaseBarSeriesProvider(BarSeries barSeries, TimeFrame timeFrame, String symbol) {
        barSeries = initializeBarSeries(barSeries);
        this.baseTimeFrame = timeFrame;
        this.maximumBars = barSeries.getBarCount();
        allowedTimeFrames = allowedTimeFrames.subList(timeFrame.getIndex(),allowedTimeFrames.size());
        completeBaseBarSeriesHolder = barSeries;
        this.symbol=symbol;
    }

    private BarSeries initializeBarSeries(BarSeries barSeries) {
        for (int index = 1;index<barSeries.getBarCount();index++){
            if (barSeries.getBar(index).getBeginTime().getDayOfWeek()!=barSeries.getBar(index-1).getBeginTime().getDayOfWeek()){
                return barSeries.getSubSeries(index,barSeries.getBarCount());
            }
        }
        return barSeries;
    }

    public boolean tickForward(){
        updateAllBarSeries();
        return (currentBarIndex <= maximumBars-1);
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
        Bar bar = baseBarSeries.getBar(baseBarSeries.getBarCount()-1);
        Bar previousBar = baseBarSeries.getBar(baseBarSeries.getBarCount()-2);

        ZonedDateTime openTime = bar.getBeginTime();
        ZonedDateTime closeTime = bar.getEndTime();

        ZonedDateTime previousOpenTime = previousBar.getBeginTime();
        ZonedDateTime previousCloseTime = previousBar.getEndTime();

      switch (timeFrame){
          case M5:{
              if (
                      timeFrame.isNewBarTime(timeFrame,previousBar,bar)
              )
              {
                  fiveMinuteBarSeries.addBar(
                          Duration.ofMinutes(5),
                          openTime.plusMinutes(5),
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
          break;
          case M15:{
              if ( timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  fifteenMinutesBarSeries.addBar(
                          Duration.ofMinutes(15),
                          openTime.plusMinutes(15),
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
          break;
          case M30:{
              if ( timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  thirtyMinutesBarSeries.addBar(
                          Duration.ofMinutes(30),
                          openTime.plusMinutes(30),
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
          break;
          case H1:{
              if ( timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  oneHourBarSeries.addBar(
                          Duration.ofMinutes(60),
                          openTime.plusMinutes(60),
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
          break;
          case H2:{
              if (  timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  twoHourBarSeries.addBar(
                          Duration.ofHours(2),
                          openTime.plusHours(2),
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
          break;
          case H4:{
              if (  timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  fourHourBarSeries.addBar(
                          Duration.ofHours(4),
                          openTime.plusHours(4),
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
          break;
          case D1:{
              if (  timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  dailyBarSeries.addBar(
                          Duration.ofDays(1),
                          openTime.plusDays(1),
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
          break;
          case W1:{
              if (  timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  weeklyBarSeries.addBar(
                          Duration.ofDays(7),
                          openTime.plusWeeks(1),
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
          break;
          case Mo:{
              if ( timeFrame.isNewBarTime(timeFrame,previousBar,bar) ){
                  monthlyBarSeries.addBar(
                          Duration.ofDays(28),
                          openTime.plusWeeks(4),
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
          break;

      }

    }

    private void updateAllBarSeries(){
        updateBaseBarSeries();
        int indexOfTimeFrame = this.baseTimeFrame.getIndex();
        List<TimeFrame> timeFrames = Arrays.stream(TimeFrame.values()).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        for (int i=indexOfTimeFrame+1;i<TimeFrame.values().length;i++){
            TimeFrame selectedTimeFrame = timeFrames.get(i);
            propagateBarSeriesByTick(selectedTimeFrame);
        }
    }

    private void updateBaseBarSeries(){
        if (currentBarIndex<1){
            BarSeries barSeries = getBarSeries(this.baseTimeFrame);
            barSeries.addBar(completeBaseBarSeriesHolder.getBar(currentBarIndex));
            TimeFrame[] timeFrames = TimeFrame.values();
            for (TimeFrame timeFrame : timeFrames) {
                if (timeFrame.getIndex()>this.baseTimeFrame.getIndex()){
                    getBarSeries(timeFrame).addBar(completeBaseBarSeriesHolder.getBar(currentBarIndex));
                }
            }
            currentBarIndex++;
        }
        BarSeries barSeries = getBarSeries(this.baseTimeFrame);
        barSeries.addBar(completeBaseBarSeriesHolder.getBar(currentBarIndex));
        currentBarIndex++;
    }

    public Num getHighYesterday(){
        BarSeries barSeries = getBarSeries(TimeFrame.D1);
        if (barSeries.getBarCount()<2) return NumUtil.getNum(0);
        return barSeries.getBar(barSeries.getBarCount()-2).getHighPrice();
    }

    public Num getLowYesterday(){
        BarSeries barSeries = getBarSeries(TimeFrame.D1);
        if (barSeries.getBarCount()<2) return NumUtil.getNum(0);
        return barSeries.getBar(barSeries.getBarCount()-2).getLowPrice();
    }

    public Num getHighLastWeek(){
        BarSeries barSeries = getBarSeries(TimeFrame.W1);
        if (barSeries.getBarCount()<2) return NumUtil.getNum(0);
        return barSeries.getBar(barSeries.getBarCount()-2).getHighPrice();
    }

    public Num getLowLastWeek(){
        BarSeries barSeries = getBarSeries(TimeFrame.W1);
        if (barSeries.getBarCount()<2) return NumUtil.getNum(0);
        return barSeries.getBar(barSeries.getBarCount()-2).getLowPrice();
    }

    public TimeFrame getBaseTimeFrame() {
        return baseTimeFrame;
    }

    public BarSeries getBaseBarSeries(){
        return getBarSeries(this.baseTimeFrame);
    }
}
