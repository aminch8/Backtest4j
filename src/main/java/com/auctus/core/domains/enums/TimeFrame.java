package com.auctus.core.domains.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
