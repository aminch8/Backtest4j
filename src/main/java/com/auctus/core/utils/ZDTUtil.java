package com.auctus.core.utils;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class ZDTUtil {

    public static long zonedDateTimeDifferenceInMinutes(ZonedDateTime d1, ZonedDateTime d2){
        return ChronoUnit.MINUTES.between(d1, d2);
    }

    public static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2, ChronoUnit unit){
        return unit.between(d1, d2);
    }


}
