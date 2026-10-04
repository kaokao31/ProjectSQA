package org.jfree.chart.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.junit.Before;
import org.junit.Test;

public class XYPlotTest implements PlotChangeListener {

    private XYPlot plot;
    private XYSeriesCollection dataset;
    private boolean plotChangedCalled;

    @Before
    public void setUp() {
        this.dataset = new XYSeriesCollection();
        this.plot = new XYPlot(this.dataset, new NumberAxis("X"), new NumberAxis("Y"), new StandardXYItemRenderer());
        this.plotChangedCalled = false;
        this.plot.addChangeListener(this);
    }

    @Override
    public void plotChanged(PlotChangeEvent event) {
        this.plotChangedCalled = true;
    }

    @Test
    public void testConstructorAndDefaults() {
        assertNotNull(plot.getDataset());
        assertNotNull(plot.getDomainAxis());
        assertNotNull(plot.getRangeAxis());
        assertNotNull(plot.getRenderer());
        assertEquals(Plot.DEFAULT_FOREGROUND_ALPHA, plot.getForegroundAlpha(), 0.001);
        assertNull(plot.getDomainAxisLocation());
        assertNull(plot.getRangeAxisLocation());
        assertTrue(plot.isDomainGridlinesVisible());
        assertTrue(plot.isRangeGridlinesVisible());
        assertFalse(plot.isDomainZeroBaselineVisible());
        assertFalse(plot.isRangeZeroBaselineVisible());
    }

    @Test
    public void testDatasetOperations() {
        XYDataset ds1 = new DefaultXYDataset();
        XYItemRenderer renderer1 = new StandardXYItemRenderer();
        
        plot.setDataset(ds1);
        assertSame(ds1, plot.getDataset());

        plot.setDataset(1, ds1);
        plot.setRenderer(1, renderer1);
        assertSame(ds1, plot.getDataset(1));
        assertSame(renderer1, plot.getRenderer(1));

        assertEquals(2, plot.getDatasetCount());
        
        plot.setDataset(null);
        assertNull(plot.getDataset(0));
    }

    @Test
    public void testAxisOperations() {
        ValueAxis xAxis2 = new NumberAxis("X2");
        ValueAxis yAxis2 = new NumberAxis("Y2");

        plot.setDomainAxis(1, xAxis2);
        plot.setRangeAxis(1, yAxis2);

        assertSame(xAxis2, plot.getDomainAxis(1));
        assertSame(yAxis2, plot.getRangeAxis(1));
        assertEquals(2, plot.getDomainAxisCount());
        assertEquals(2, plot.getRangeAxisCount());

        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        assertNotNull(plot.getDomainAxisLocation());
        
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertNotNull(plot.getRangeAxisLocation());

        plot.setDomainAxisLocation(0, AxisLocation.TOP_OR_LEFT, true);
        plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT, true);
        assertTrue(plotChangedCalled);
    }

    @Test
    public void testRendererOperations() {
        XYItemRenderer r = new StandardXYItemRenderer();
        plot.setRenderer(r);
        assertSame(r, plot.getRenderer());

        plot.setRenderer(0, r, true);
        assertEquals(1, plot.getRendererCount());
    }

    @Test
    public void testOrientation() {
        plot.setOrientation(org.jfree.chart.plot.PlotOrientation.HORIZONTAL);
        assertEquals(org.jfree.chart.plot.PlotOrientation.HORIZONTAL, plot.getOrientation());
        
        plot.setOrientation(org.jfree.chart.plot.PlotOrientation.VERTICAL);
        assertEquals(org.jfree.chart.plot.PlotOrientation.VERTICAL, plot.getOrientation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientationNull() {
        plot.setOrientation(null);
    }

    @Test
    public void testGridlines() {
        plot.setDomainGridlinesVisible(true);
        assertTrue(plot.isDomainGridlinesVisible());
        plot.setDomainGridlineStroke(new BasicStroke(2.0f));
        assertNotNull(plot.getDomainGridlineStroke());
        plot.setDomainGridlinePaint(Color.RED);
        assertEquals(Color.RED, plot.getDomainGridlinePaint());

        plot.setRangeGridlinesVisible(true);
        assertTrue(plot.isRangeGridlinesVisible());
        plot.setRangeGridlineStroke(new BasicStroke(2.0f));
        assertNotNull(plot.getRangeGridlineStroke());
        plot.setRangeGridlinePaint(Color.BLUE);
        assertEquals(Color.BLUE, plot.getRangeGridlinePaint());
    }

    @Test
    public void testZeroBaselines() {
        plot.setDomainZeroBaselineVisible(true);
        assertTrue(plot.isDomainZeroBaselineVisible());
        plot.setDomainZeroBaselineStroke(new BasicStroke(1.5f));
        assertNotNull(plot.getDomainZeroBaselineStroke());
        plot.setDomainZeroBaselinePaint(Color.GREEN);
        assertEquals(Color.GREEN, plot.getDomainZeroBaselinePaint());

        plot.setRangeZeroBaselineVisible(true);
        assertTrue(plot.isRangeZeroBaselineVisible());
        plot.setRangeZeroBaselineStroke(new BasicStroke(1.5f));
        assertNotNull(plot.getRangeZeroBaselineStroke());
        plot.setRangeZeroBaselinePaint(Color.YELLOW);
        assertEquals(Color.YELLOW, plot.getRangeZeroBaselinePaint());
    }

    @Test
    public void testMarkers() {
        org.jfree.chart.plot.ValueMarker vMarker = new org.jfree.chart.plot.ValueMarker(10.0);
        plot.addDomainMarker(vMarker);
        assertTrue(plot.getDomainMarkers(org.jfree.ui.Layer.FOREGROUND).contains(vMarker));

        plot.removeDomainMarker(vMarker);

        org.jfree.chart.plot.IntervalMarker iMarker = new org.jfree.chart.plot.IntervalMarker(0.0, 5.0);
        plot.addRangeMarker(iMarker, org.jfree.ui.Layer.BACKGROUND);
        assertTrue(plot.getRangeMarkers(org.jfree.ui.Layer.BACKGROUND).contains(iMarker));
        
        plot.clearRangeMarkers();
        assertNull(plot.getRangeMarkers(org.jfree.ui.Layer.BACKGROUND));
    }

    @Test
    public void testAnnotations() {
        org.jfree.chart.annotations.XYTextAnnotation annotation = new org.jfree.chart.annotations.XYTextAnnotation("Test", 1.0, 1.0);
        plot.addAnnotation(annotation);
        assertTrue(plot.getAnnotations().contains(annotation));

        plot.removeAnnotation(annotation);
        assertFalse(plot.getAnnotations().contains(annotation));

        plot.clearAnnotations();
        assertEquals(0, plot.getAnnotations().size());
    }

    @Test
    public void testGetLegendItems() {
        XYSeries series = new XYSeries("Series 1");
        series.add(1.0, 1.0);
        dataset.addSeries(series);

        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
        assertTrue(items.getItemCount() > 0);
    }

    @Test
    public void testDatasetChanged() {
        XYSeries series = new XYSeries("S");
        dataset.addSeries(series);
        plot.datasetChanged(new DatasetChangeEvent(this, dataset));
        assertTrue(plotChangedCalled);
    }

    @Test
    public void testGetDataRange() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 2.0);
        series.add(3.0, 4.0);
        dataset.addSeries(series);

        Range domainRange = plot.getDataRange(plot.getDomainAxis());
        assertNotNull(domainRange);
        assertEquals(1.0, domainRange.getLowerBound(), 0.001);
        assertEquals(3.0, domainRange.getUpperBound(), 0.001);

        Range rangeRange = plot.getDataRange(plot.getRangeAxis());
        assertNotNull(rangeRange);
        assertEquals(2.0, rangeRange.getLowerBound(), 0.001);
        assertEquals(4.0, rangeRange.getUpperBound(), 0.001);
        
        assertNull(plot.getDataRange(null));
    }

    @Test
    public void testDrawWithoutErrors() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        ChartRenderingInfo info = new ChartRenderingInfo();
        
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        dataset.addSeries(series);

        plot.draw(g2, new Rectangle2D.Double(0, 0, 400, 300), null, null, info);
        assertNotNull(info.getDataArea());
    }

    @Test
    public void testEqualsAndClone() throws Exception {
        XYPlot p1 = new XYPlot();
        XYPlot p2 = new XYPlot();
        assertEquals(p1, p2);

        p1.setDomainGridlinePaint(Color.RED);
        assertFalse(p1.equals(p2));

        XYPlot cloned = (XYPlot) p1.clone();
        assertEquals(p1, cloned);
    }

    @Test
    public void testMapDatasetToDomainAndRangeAxis() {
        plot.setDataset(1, new DefaultXYDataset());
        plot.mapDatasetToDomainAxis(1, 1);
        plot.mapDatasetToRangeAxis(1, 1);
        
        assertEquals(1, plot.resolveDomainAxes(1).size());
        assertEquals(1, plot.resolveRangeAxes(1).size());
    }

    @Test
    public void testFixedLegendItems() {
        LegendItemCollection customItems = new LegendItemCollection();
        customItems.add(new LegendItem("Item"));
        plot.setFixedLegendItems(customItems);
        assertSame(customItems, plot.getFixedLegendItems());
        assertEquals(1, plot.getLegendItems().getItemCount());
    }
}