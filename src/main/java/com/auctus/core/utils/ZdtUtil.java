package com.auctus.core.utils;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class ZdtUtil {

    public static long zonedDateTimeDifferenceInMinutes(ZonedDateTime d1, ZonedDateTime d2){
        return ChronoUnit.MINUTES.between(d1, d2);
    }


}
