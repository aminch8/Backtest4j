package com.auctus.core;

import com.auctus.core.utils.ZdtUtil;

import java.time.ZonedDateTime;

public class Main {

    public static void main(String[] args) {


        System.out.println(ZdtUtil.zonedDateTimeDifference(ZonedDateTime.now(),ZonedDateTime.now().plusMinutes(100)));
    }

}
