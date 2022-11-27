package com.auctus.core.domains.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TimeFrame {
    TICK("TICK"),Mo("Mo"),W1("W1"),D1("D1"),H4("H4"),H2("H2"),H1("H1"),M30("M30"),M15("M15"),M5("M5"),M1("M1");
    private String value;
}
