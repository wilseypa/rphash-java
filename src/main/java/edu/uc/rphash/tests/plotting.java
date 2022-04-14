package edu.uc.rphash.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

//import org.knowm.xchart.*;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.internal.Utils;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;

import edu.uc.rphash.tests.generators.GenerateData;

public class plotting {
	
	static Random random = new Random();
	
	private static List<Double> getGaussian(int number, double mean, double std) {
		 
	    List<Double> seriesData = new LinkedList<Double>();
	    for (int i = 0; i < number; i++) {
	      seriesData.add(mean + std * random.nextGaussian());
	    }
	 
	    return seriesData;
	 
	  }
	
public static void main(String[] args) {
double[] xData = new double[] { 0.0, 1.0, 2.0 };
double[] yData = new double[] { 2.0, 1.0, 0.0 };

// Create Chart
XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);

// Show it
new SwingWrapper(chart).displayChart();


//Create Chart2
XYChart chart2 = new XYChartBuilder().width(600).height(500).title("Gaussian Blobs").xAxisTitle("X").yAxisTitle("Y").build();

//Customize Chart2
chart2.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
//chart2.getStyler().setChartTitleVisible(false);
//chart2.getStyler().setLegendPosition(LegendPosition.InsideSW);
chart2.getStyler().setMarkerSize(16);

//Series

int k = 10;//6;
int d = 2;//16;
int n = 10000;
float var = 1.5f;
GenerateData gen = new GenerateData(k, n/k, d, var, true, .5f);

chart2.addSeries("Gaussian Blob 1", getGaussian(1000, 5, 1), getGaussian(1000, 5, 1));

XYSeries series2 = chart2.addSeries("Gaussian Blob 2", getGaussian(1000, 50, 1), getGaussian(1000, 50, 1));

XYSeries series3 = chart2.addSeries("Gaussian Blob 3", getGaussian(1000, 5, 1), getGaussian(1000, 50, 1));

XYSeries series4 = chart2.addSeries("Gaussian Blob 4", getGaussian(1000, 50, 1), getGaussian(1000, 5, 1));

XYSeries series5 = chart2.addSeries("Gaussian Blob 5", getGaussian(1000, 25, 1), getGaussian(1000, 25, 1));

//chart2.addSeries("Gaussian Blob 2", getDoubleArrayFromNumberList​(gen.getData()), getDoubleArrayFromNumberList​(gen.getData()));

//XYSeries series = chart2.addSeries("Gaussian Blob 2", getDoubleArrayFromNumberList​(gen.getData()), getDoubleArrayFromNumberList​(gen.getData()));

//series2.setMarker(SeriesMarkers.DIAMOND);


new SwingWrapper(chart2).displayChart();

	}



}