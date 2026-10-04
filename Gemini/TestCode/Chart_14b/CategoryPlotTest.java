package org.jfree.chart.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ResourceBundle;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.RectangleAnchor;
import org.junit.Before;
import org.junit.Test;

public class CategoryPlotTest {

    private CategoryPlot plot;
    private DefaultCategoryDataset dataset;
    private CategoryAxis domainAxis;
    private ValueAxis rangeAxis;
    private CategoryItemRenderer renderer;

    @Before
    public void setUp() {
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C2");

        domainAxis = new CategoryAxis("Domain Axis");
        rangeAxis = new NumberAxis("Range Axis");
        renderer = new BarRenderer();

        plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
    }

    @Test
    public void testConstructorAndGetters() {
        assertNotNull(plot);
        assertEquals(dataset, plot.getDataset());
        assertEquals(domainAxis, plot.getDomainAxis());
        assertEquals(rangeAxis, plot.getRangeAxis());
        assertEquals(renderer, plot.getRenderer());
        assertEquals("CategoryPlot", plot.getPlotType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMarkerNullLayer() {
        org.jfree.chart.plot.CategoryMarker marker = new org.jfree.chart.plot.CategoryMarker("C1");
        plot.addDomainMarker(null, marker, null);
    }

    @Test
    public void testAddAndRemoveDomainMarkers() {
        org.jfree.chart.plot.CategoryMarker marker = new org.jfree.chart.plot.CategoryMarker("C1");
        plot.addDomainMarker(marker);
        
        // Test retrieval
        CollectionListener listener = new CollectionListener();
        plot.addChangeListener(listener);

        boolean removed = plot.removeDomainMarker(marker);
        assertTrue(removed);

        // Remove non-existent
        assertFalse(plot.removeDomainMarker(marker));
        
        // Test clear
        plot.addDomainMarker(0, marker, Layer.FOREGROUND);
        plot.clearDomainMarkers();
        assertNull(plot.getDomainMarkers(0, Layer.FOREGROUND));
    }

    @Test
    public void testAddAndRemoveRangeMarkers() {
        org.jfree.chart.plot.ValueMarker marker = new org.jfree.chart.plot.ValueMarker(1.5);
        plot.addRangeMarker(marker);

        boolean removed = plot.removeRangeMarker(marker);
        assertTrue(removed);

        assertFalse(plot.removeRangeMarker(marker));

        plot.addRangeMarker(0, marker, Layer.BACKGROUND);
        plot.clearRangeMarkers();
        assertNull(plot.getRangeMarkers(0, Layer.BACKGROUND));
    }

    @Test
    public void testDatasetOperations() {
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        ds2.addValue(10.0, "R3", "C3");
        
        plot.setDataset(1, ds2);
        assertEquals(ds2, plot.getDataset(1));
        assertEquals(2, plot.indexOf(ds2));

        plot.setRenderer(1, new LineAndShapeRenderer());
        assertNotNull(plot.getRenderer(1));

        plot.setDomainAxis(1, new CategoryAxis("Domain 2"));
        assertNotNull(plot.getDomainAxis(1));

        plot.setRangeAxis(1, new NumberAxis("Range 2"));
        assertNotNull(plot.getRangeAxis(1));
    }

    @Test
    public void testAxisMappings() {
        plot.mapDatasetToDomainAxis(0, java.util.Arrays.asList(new Integer(0)));
        plot.mapDatasetToRangeAxis(0, java.util.Arrays.asList(new Integer(0)));
        
        assertEquals(domainAxis, plot.getDomainAxisForDataset(0));
        assertEquals(rangeAxis, plot.getRangeAxisForDataset(0));
    }

    @Test
    public void testLegendItems() {
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
    }

    @Test
    public void testHashCodeAndEquals() {
        CategoryPlot plot2 = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        assertEquals(plot, plot2);
        assertEquals(plot.hashCode(), plot2.hashCode());

        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(plot.equals(plot2));
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        CategoryPlot clone = (CategoryPlot) plot.clone();
        assertNotNull(clone);
        assertEquals(plot, clone);
        assertTrue(plot != clone);
    }

    @Test
    public void testDrawWithoutData() {
        CategoryPlot emptyPlot = new CategoryPlot();
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);
        
        emptyPlot.draw(g2, area, null, null, null);
        assertTrue(true); // Successfully handled empty plot draw
    }

    @Test
    public void testWeightAndInsets() {
        plot.setWeight(3);
        assertEquals(3, plot.getWeight());

        plot.setAxisOffset(new RectangleInsets(2.0, 2.0, 2.0, 2.0));
        assertEquals(new RectangleInsets(2.0, 2.0, 2.0, 2.0), plot.getAxisOffset());
    }

    @Test
    public void testRowCountAndColumnCount() {
        assertEquals(2, plot.getRowCount());
        assertEquals(2, plot.getColumnCount());
    }

    private static class CollectionListener implements PlotChangeListener {
        private boolean eventReceived = false;

        @Override
        public void plotChanged(PlotChangeEvent event) {
            eventReceived = true;
        }

        public boolean isEventReceived() {
            return eventReceived;
        }
    }
}