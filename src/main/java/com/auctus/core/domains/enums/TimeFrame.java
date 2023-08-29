package com.auctus.core.domains.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ta4j.core.Bar;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public enum TimeFrame {
    M1("M1",0),
    M5("M5",1),
    M15("M15",2),
    M30("M30",3),
    H1("H1",4),
    H2("H2",5),
    H4("H4",6),
    D1("D1",7),
    W1("W1",8),
    Mo("Mo",9);
    private String value;
    private int index;

    public boolean isNewBarTime(TimeFrame timeFrame, Bar previousBar, Bar currentBar) {

        ZonedDateTime previousCloseTime = previousBar.getEndTime();
        ZonedDateTime previousOpenTime = previousBar.getBeginTime();
        ZonedDateTime closeTime = currentBar.getEndTime();
        ZonedDateTime openTime = currentBar.getBeginTime();

        switch (timeFrame){

            case M1:return true;

            case M5: {
                return checkIfBreakPoints(5, previousCloseTime, closeTime);
            }

            case M15: {
                return checkIfBreakPoints(15, previousCloseTime, closeTime);
            }

            case M30: {
                return checkIfBreakPoints(30, previousCloseTime, closeTime);
            }

            case H1: {
                return checkIfBreakPoints(60, previousCloseTime, closeTime);
            }

            case H2: {
                return checkIfBreakPoints(120, previousCloseTime, closeTime);
            }

            case H4: {
                return checkIfBreakPoints(240, previousCloseTime, closeTime);
            }

            case D1: {
                return previousOpenTime.getDayOfWeek().getValue() != openTime.getDayOfWeek().getValue();
            }

            case W1: {
                return previousOpenTime.getDayOfWeek() != DayOfWeek.MONDAY && openTime.getDayOfWeek() == DayOfWeek.MONDAY;
            }

            case Mo: {
                return previousOpenTime.getMonth() != openTime.getMonth();
            }


        }


        return false;
    }

    private boolean checkIfBreakPoints(int minutes,ZonedDateTime previousCloseTime, ZonedDateTime closeTime) {
        ZonedDateTime breakPoints = previousCloseTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (breakPoints.isBefore(closeTime) || breakPoints.isEqual(closeTime)){
            if (
                    (breakPoints.isAfter(previousCloseTime) || breakPoints.isEqual(previousCloseTime))
                    &&
                            breakPoints.isBefore(closeTime)
            ){
                return true;
            }
            breakPoints = breakPoints.plusMinutes(minutes);
        }
        return false;
    }
}
