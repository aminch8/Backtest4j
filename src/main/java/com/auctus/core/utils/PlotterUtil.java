package com.auctus.core.utils;

import com.auctus.core.domains.BalanceSnapshot;
import com.auctus.core.domains.TradeLog;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;

public class PlotterUtil {

    private static final String DOWN_ARROW="/arrows/arrow-down.png";
    private static final String UP_ARROW="/arrows/arrow-up.png";

    private static final int BUFFER_OHLC_CHART=50;

    public static SwingWrapper swingWrapper;
    private static OHLCChart chart = new OHLCChartBuilder().width(1600).height(1200).title("OHLC Chart").build();


    public static void plotBalanceChartXY(String yTitle, String xTitle, String chartTitle, List<BalanceSnapshot> balanceSnapshots){
        XYChart chart = new XYChartBuilder().width(600).height(500).title(chartTitle).xAxisTitle(xTitle).yAxisTitle(yTitle).build();
        chart.getStyler().setZoomEnabled(true);
        chart.getStyler().setZoomResetByDoubleClick(false);
        chart.getStyler().setZoomResetByButton(true);
        chart.getStyler().setZoomSelectionColor(new Color(0,0 , 192, 128));
        SwingWrapper swingWrapper = new SwingWrapper(chart);
        swingWrapper.displayChart();
    }



    public static void plotOHLC(BarSeries barSeries, TradeLog fromTrade, TradeLog toTrade) {
        // Customize Chart
        chart.getStyler().
                setLegendPosition(Styler.LegendPosition.OutsideS);

        chart.getStyler().
                setLegendLayout(Styler.LegendLayout.Horizontal);

        List<Integer> xData = new ArrayList<>();
        List<Double> openData = new ArrayList<>();
        List<Double> highData = new ArrayList<>();
        List<Double> lowData = new ArrayList<>();
        List<Double> closeData = new ArrayList<>();

        for (
                int index = Math.max(fromTrade.getIndex()-BUFFER_OHLC_CHART,0)
                ; index <= Math.min(barSeries.getBarCount()-1,toTrade.getIndex()+BUFFER_OHLC_CHART*3)
                ; index++) {
            Bar currentBar = barSeries.getBar(index);
            xData.add(index);
            openData.add(currentBar.getOpenPrice().doubleValue());
            highData.add(currentBar.getHighPrice().doubleValue());
            lowData.add(currentBar.getLowPrice().doubleValue());
            closeData.add(currentBar.getClosePrice().doubleValue());
        }

        chart.addAnnotation(getAnnotationImage(UP_ARROW,fromTrade.getIndex()-1,fromTrade.getPrice().doubleValue()));
        chart.addAnnotation(getAnnotationImage(DOWN_ARROW,toTrade.getIndex()+1,toTrade.getPrice().doubleValue()));

        if (swingWrapper == null) {
            chart.removeSeries("Series");
            chart.addSeries("Series", xData, openData, highData, lowData, closeData);
            chart.getStyler().setToolTipsEnabled(true);
            swingWrapper = new SwingWrapper<>(chart);
            swingWrapper.displayChart();
        } else {
            chart.updateOHLCSeries("Series", xData, openData, highData, lowData, closeData);
            swingWrapper.repaintChart();
        }

    }

    private static AnnotationImage getAnnotationImage(String address,int x,double y){
        BufferedImage image = null;
        try {
            File file = new File("src/main/resources"+address);
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AnnotationImage(image, x, y, false);
    }

}
