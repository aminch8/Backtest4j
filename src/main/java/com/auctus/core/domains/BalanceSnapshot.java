package com.auctus.core.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSnapshot {
    private ZonedDateTime time;
    private Num balance;
    private Num UPNL;
}
