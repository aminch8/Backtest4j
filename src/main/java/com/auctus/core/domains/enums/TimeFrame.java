package com.auctus.core.domains.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TimeFrame {
    Mo("Mo",9),
    W1("W1",8),
    D1("D1",7),
    H4("H4",6),
    H2("H2",5),
    H1("H1",4),
    M30("M30",3),
    M15("M15",2),
    M5("M5",1),
    M1("M1",0);
    private String value;
    private int index;
}
