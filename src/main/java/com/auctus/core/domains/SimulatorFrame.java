package com.auctus.core.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ta4j.core.BarSeriesManager;

import java.util.List;

@Data
public abstract class SimulatorFrame {
    List<Order> orders;
    private String symbol;
    private BarSeriesProvider barSeriesProvider;
}
