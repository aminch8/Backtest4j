package com.auctus.core.utils;

import com.auctus.core.domains.BalanceSnapshot;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.List;

public class PlotterUtil {


    public static void plotBalanceChartXY(String yTitle, String xTitle, String chartTitle, List<BalanceSnapshot> balanceSnapshots){
        XYChart chart = new XYChartBuilder().width(600).height(500).title(chartTitle).xAxisTitle(xTitle).yAxisTitle(yTitle).build();
        chart.getStyler().setZoomEnabled(true);
        chart.getStyler().setZoomResetByDoubleClick(false);
        chart.getStyler().setZoomResetByButton(true);
        chart.getStyler().setZoomSelectionColor(new Color(0,0 , 192, 128));
        SwingWrapper swingWrapper = new SwingWrapper(chart);
        swingWrapper.displayChart();
    }


}
