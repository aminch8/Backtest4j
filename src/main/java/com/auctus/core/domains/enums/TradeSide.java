package com.auctus.core.domains.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeSide {
    BUY("BUY"),SELL("SELL");
    private String value;
}
