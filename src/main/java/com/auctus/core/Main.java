package com.auctus.core;

import com.auctus.core.domains.enums.TimeFrame;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        List<TimeFrame> timeFrames = Arrays.stream(TimeFrame.values()).collect(Collectors.toList());
        timeFrames.sort(Comparator.reverseOrder());
//        for (int i=indexOfTimeFrame;i<TimeFrame.values().length;i++){
//
//        }

        for (TimeFrame timeFrame : timeFrames) {
            System.out.println(timeFrame.getIndex() + timeFrame.getValue());
        }
    }

}
